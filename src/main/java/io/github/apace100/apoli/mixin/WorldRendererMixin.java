package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private Entity renderEntity;

    @Shadow public abstract void allChanged();

    @Inject(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSkyDisc(FFF)V"), cancellable = true)
    private void skipSkyRenderingForPhasingBlindness(GpuBufferSlice gpuBufferSlice, DimensionSpecialEffects.SkyType skyType, float f, DimensionSpecialEffects dimensionSpecialEffects, CallbackInfo ci) {
        if(Minecraft.getInstance().cameraEntity instanceof LivingEntity) {
            List<PhasingPower> phasings = PowerHolderComponent.getPowers(Minecraft.getInstance().cameraEntity, PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((LivingEntity)Minecraft.getInstance().cameraEntity) != null) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void updateChunksIfRenderChanged(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        if(ApoliClient.shouldReloadWorldRenderer) {
            allChanged();
            ApoliClient.shouldReloadWorldRenderer = false;
        }
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
    private void getEntity(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, DeltaTracker deltaTracker, List<Entity> entities, CallbackInfo ci, @Local Entity entity) {
        this.renderEntity = entity;
    }

    @ModifyArgs(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V"))
    private void setColors(Args args) {
        for (EntityGlowPower power : PowerHolderComponent.getPowers(minecraft.getCameraEntity(), EntityGlowPower.class)) {
            if (power.doesApply(renderEntity)) {
                if (!power.usesTeams()) {
                    args.set(0, (int)(power.getRed() * 255.0F));
                    args.set(1, (int)(power.getGreen() * 255.0F));
                    args.set(2, (int)(power.getBlue() * 255.0F));
                }
            }
        }
        for (SelfGlowPower power : PowerHolderComponent.getPowers(renderEntity, SelfGlowPower.class)) {
            if (!power.usesTeams()) {
                args.set(0, (int)(power.getRed() * 255.0F));
                args.set(1, (int)(power.getGreen() * 255.0F));
                args.set(2, (int)(power.getBlue() * 255.0F));
            }
        }
    }
}
