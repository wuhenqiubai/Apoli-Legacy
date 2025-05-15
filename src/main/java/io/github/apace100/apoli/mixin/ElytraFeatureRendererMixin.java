package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ElytraFlightPower;
import io.github.apace100.apoli.util.ApoliLivingEntityRenderState;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceKey;
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

                    if (cached == null || (cached.assetId().isPresent() && !cached.assetId().orElseThrow().location().equals(power.getTextureLocation()))) {
                        var equippable = Equippable.builder(EquipmentSlot.CHEST)
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                            .setAsset(
                                power.getTextureLocation() == null ? EquipmentAssets.ELYTRA : ResourceKey.create(EquipmentAssets.ROOT_ID, power.getTextureLocation())
                            )
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
}
