package io.github.apace100.apoli.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ElytraFlightPower;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public class CapeFeatureRendererMixin {

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", cancellable = true)
    private void preventCapeRendering(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState playerRenderState, float f, float g, CallbackInfo ci) {
        if(PowerHolderComponent.getPowers(playerRenderState, ElytraFlightPower.class).stream().anyMatch(ElytraFlightPower::shouldRenderElytra)) {
            ci.cancel();
        }
    }
}