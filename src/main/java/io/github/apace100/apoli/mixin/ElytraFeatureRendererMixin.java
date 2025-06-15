package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ElytraFlightPower;
import io.github.apace100.apoli.util.ApoliLivingEntityRenderState;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WingsLayer.class)
public class ElytraFeatureRendererMixin {
    @ModifyExpressionValue(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object modifyEquippedStackToElytra(Object original, @Local(argsOnly = true) HumanoidRenderState renderState) {
        if (!renderState.isInvisible) {
            for (ElytraFlightPower power : PowerHolderComponent.getPowers(renderState, ElytraFlightPower.class)) {
                if (power.shouldRenderElytra()) {
                    var cached = ((ApoliLivingEntityRenderState) renderState).apoli$getCachedEquippable();

                    if (cached == null || (cached.assetId().isPresent() && !cached.assetId().orElseThrow().equals(EquipmentAssets.ELYTRA))) {
                        var equippable = Equippable.builder(EquipmentSlot.CHEST)
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                            .setAsset(EquipmentAssets.ELYTRA)
                            .setDamageOnHurt(false)
                            .build();

                        ((ApoliLivingEntityRenderState) renderState).apoli$setCachedEquippable(equippable);
                        return equippable;
                    } else {
                        return cached;
                    }
                }
            }
        }

        return original;
    }

    @ModifyExpressionValue(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/WingsLayer;getPlayerElytraTexture(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation modifyEntityElytraTextureToPower(ResourceLocation original, @Local(argsOnly = true) HumanoidRenderState renderState) {
        if (!renderState.isInvisible) {
            for (ElytraFlightPower power : PowerHolderComponent.getPowers(renderState, ElytraFlightPower.class)) {
                if (power.shouldRenderElytra() && power.getTextureLocation() != null) {
                    return power.getTextureLocation();
                }
            }
        }

        return original;
    }
}
