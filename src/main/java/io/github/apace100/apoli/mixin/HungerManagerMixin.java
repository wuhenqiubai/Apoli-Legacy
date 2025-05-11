package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FoodData.class)
public class HungerManagerMixin {

    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Unique
    private Player player;

    @Unique
    private boolean apoli$ShouldUpdateManually = false;

    @ModifyExpressionValue(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodProperties;getNutrition()I"))
    private int modifyHunger(int baseValue, @Local(argsOnly = true) ItemStack stack) {
        apoli$ShouldUpdateManually = false;

        if (player == null) return baseValue;

        List<Modifier> modifiers = ((ModifiableFoodEntity) player).getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getFoodModifiers().stream())
            .toList();

        int newFood = (int) ModifierUtil.applyModifiers(player, modifiers, baseValue);
        if (newFood != baseValue && newFood == 0) apoli$ShouldUpdateManually = true;

        return newFood;

    }

    @ModifyExpressionValue(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodProperties;getSaturationModifier()F"))
    private float modifySaturation(float baseValue, @Local(argsOnly = true) ItemStack stack) {
        if (player == null) return baseValue;

        List<Modifier> modifiers = ((ModifiableFoodEntity) player).getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getSaturationModifiers().stream())
            .toList();

        float newSaturation = (float) ModifierUtil.applyModifiers(player, modifiers, baseValue);
        if (newSaturation != baseValue && newSaturation == 0) apoli$ShouldUpdateManually = true;

        return newSaturation;

    }

    @Inject(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V", shift = At.Shift.AFTER))
    private void executeAdditionalEatAction(Item item, ItemStack stack, CallbackInfo ci) {

        if (player == null || player.level().isClientSide) return;

        ((ModifiableFoodEntity) player).getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .forEach(ModifyFoodPower::eat);

        if (apoli$ShouldUpdateManually) ((ServerPlayer) player).connection.send(new ClientboundSetHealthPacket(player.getHealth(), foodLevel, saturationLevel));

    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void cachePlayer(Player player, CallbackInfo ci) {
        this.player = player;
    }
}
