package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.InvisibilityPower;
import io.github.apace100.apoli.power.ModelColorPower;
import io.github.apace100.apoli.power.PreventFeatureRenderPower;
import io.github.apace100.apoli.power.ShakingPower;
import io.github.apace100.apoli.util.ApoliLivingEntityRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<S>> extends EntityRenderer<T, S> {

    @Shadow public abstract ResourceLocation getTextureLocation(S renderState);

    protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(method = "isShaking", at = @At("HEAD"), cancellable = true)
    private void letPlayersShakeTheirBodies(S renderState, CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower(renderState, ShakingPower.class)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean preventOutlineRendering(boolean original, LivingEntity livingEntity) {
        List<InvisibilityPower> invisibilityPowers = PowerHolderComponent.getPowers(livingEntity, InvisibilityPower.class);
        if(invisibilityPowers.size() > 0 && invisibilityPowers.stream().noneMatch(InvisibilityPower::shouldRenderOutline)) {
            return false;
        }
        return original;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void apoli$storePowersInState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        ((ApoliLivingEntityRenderState) livingEntityRenderState).apoli$setPowerHolder(PowerHolderComponent.KEY.getNullable(livingEntity));
    }

    @ModifyArg(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType changeRenderLayerWhenTranslucent(RenderType renderType, @Local(argsOnly = true) S renderState) {
        if(PowerHolderComponent.getPowers(renderState, ModelColorPower.class).stream().anyMatch(ModelColorPower::isTranslucent)) {
            return RenderType.itemEntityTranslucentCull(getTextureLocation(renderState));
        }
        return renderType;
    }

    @WrapOperation(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V"))
    private void preventFeatureRendering(RenderLayer featureRenderer, PoseStack poseStack, MultiBufferSource bufferSource, int i, EntityRenderState renderState, float yRot, float xRot, Operation<Void> original) {
        List<InvisibilityPower> invisibilityPowers = PowerHolderComponent.getPowers((S) renderState, InvisibilityPower.class);
        if(invisibilityPowers.size() > 0 && invisibilityPowers.stream().noneMatch(InvisibilityPower::shouldRenderArmor)) {
            return;
        }
        Class cls = featureRenderer.getClass();
        if(!PowerHolderComponent.getPowers((S) renderState, PreventFeatureRenderPower.class).stream().anyMatch(p -> p.doesApply(cls))) {
            original.call(featureRenderer, poseStack, bufferSource, i, renderState, yRot, xRot);
        }
    }

    @Environment(EnvType.CLIENT)
    @ModifyArg(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"), index = 4)
    private int renderColorChangedModel(int rgb, @Local(argsOnly = true) LivingEntityRenderState renderState) {
        List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(renderState, ModelColorPower.class);
        if (modelColorPowers.size() > 0) {
            float r = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).get();
            float g = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).get();
            float b = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, c) -> a * c).get();
            float a = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).get();

            return ARGB.colorFromFloat(ARGB.alphaFloat(rgb) * a, ARGB.redFloat(rgb) * r, ARGB.greenFloat(rgb) * g, ARGB.blueFloat(rgb) * b);
        }

        return rgb;
    }
}
