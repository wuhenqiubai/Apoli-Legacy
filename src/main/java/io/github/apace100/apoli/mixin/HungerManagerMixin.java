package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.ApoliSharedMixinValues;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FoodData.class)
public class HungerManagerMixin {

    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Unique
    private Player player;

    @Unique
    private boolean apoli$ShouldUpdateManually = false;

    @ModifyArgs(method = "eat(IF)V", at = @At("HEAD"))
    private void modifyHunger(Args args) {
        apoli$ShouldUpdateManually = false;

        if (player == null) return;
        var stack = ApoliSharedMixinValues.CURRENT_STACK.get();
        if (stack == null) return;

        var modifiers = ((ModifiableFoodEntity) player).getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack));

        var foodModifiers = modifiers.flatMap(p -> p.getFoodModifiers().stream()).toList();
        var saturationModifiers = modifiers.flatMap(p -> p.getSaturationModifiers().stream()).toList();

        int newFood = (int) ModifierUtil.applyModifiers(player, foodModifiers, args.get(0));
        if (newFood != (int) args.get(0) && newFood == 0) apoli$ShouldUpdateManually = true;

        float newSat = (float) ModifierUtil.applyModifiers(player, saturationModifiers, args.get(1));
        if (newSat != (float) args.get(1) && newSat == 0) apoli$ShouldUpdateManually = true;

        args.set(0, newFood);
        args.set(1, newSat);
    }

    @Inject(method = "eat(IF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;add(IF)V", shift = At.Shift.AFTER))
    private void executeAdditionalEatAction(int foodLevelModifier, float saturationLevelModifier, CallbackInfo ci) {

        if (player == null || player.level().isClientSide) return;
        var stack = ApoliSharedMixinValues.CURRENT_STACK.get();
        if (stack == null) return;

        ((ModifiableFoodEntity) player).getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .forEach(ModifyFoodPower::eat);

        if (apoli$ShouldUpdateManually) ((ServerPlayer) player).connection.send(new ClientboundSetHealthPacket(player.getHealth(), foodLevel, saturationLevel));

    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void cachePlayer(ServerPlayer player, CallbackInfo ci) {
        this.player = player;
    }
}
