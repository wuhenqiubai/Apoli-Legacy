package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCameraSubmersionTypePower;
import io.github.apace100.apoli.power.NightVisionPower;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FogRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class BackgroundRendererMixin {

    @Shadow private static int targetBiomeFog;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 0), method = "computeFogColor")
    private static boolean hasStatusEffectProxy(LivingEntity instance, Holder<MobEffect> effect, Operation<Boolean> original) {
        if(instance instanceof Player player && effect == MobEffects.NIGHT_VISION && !player.hasEffect(MobEffects.NIGHT_VISION)) {
            return PowerHolderComponent.KEY.get(player).getPowers(NightVisionPower.class).stream().anyMatch(NightVisionPower::isActive);
        }
        return original.call(instance, effect);
    }

    @ModifyVariable(method = "computeFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getEntity()Lnet/minecraft/world/entity/Entity;", ordinal = 0), ordinal = 0)
    private static FogType modifyCameraSubmersionTypeRender(FogType original, Camera camera) {
        if(camera.getEntity() instanceof LivingEntity) {
            for(ModifyCameraSubmersionTypePower p : PowerHolderComponent.getPowers(camera.getEntity(), ModifyCameraSubmersionTypePower.class)) {
                if(p.doesModify(original)) {
                    return p.getNewType();
                }
            }
        }
        return original;
    }

    @ModifyVariable(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getEntity()Lnet/minecraft/world/entity/Entity;", ordinal = 0), ordinal = 0)
    private static FogType modifyCameraSubmersionTypeFog(FogType original, @Local(argsOnly = true) Camera camera) {
        if(camera.getEntity() instanceof LivingEntity) {
            for(ModifyCameraSubmersionTypePower p : PowerHolderComponent.getPowers(camera.getEntity(), ModifyCameraSubmersionTypePower.class)) {
                if(p.doesModify(original)) {
                    return p.getNewType();
                }
            }
        }
        return original;
    }

    @Inject(method = "computeFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;getPriorityFogFunction(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/FogRenderer$MobEffectFogFunction;"))
    private static void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, CallbackInfoReturnable<Vector4f> cir) {
        if(camera.getEntity() instanceof LivingEntity) {
            if(PowerHolderComponent.getPowers(camera.getEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((Player)camera.getEntity()) != null) {
                    targetBiomeFog = 0;
                }
            }
        }
    }

    @Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogParameters;<init>(FFLcom/mojang/blaze3d/shaders/FogShape;FFFF)V", shift = At.Shift.BEFORE))
    private static void modifyFogData(Camera camera, FogRenderer.FogMode fogMode, Vector4f fogColor, float renderDistance, boolean isFoggy, float partialTick, CallbackInfoReturnable<FogParameters> cir, @Local FogRenderer.FogData fogData) {
        if(camera.getEntity() instanceof LivingEntity) {
            List<PhasingPower> phasings = PowerHolderComponent.getPowers(camera.getEntity(), PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((LivingEntity)camera.getEntity()) != null) {
                    float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
                    if (fogData.mode == FogRenderer.FogMode.FOG_SKY) {
                        fogData.start = 0.0f;
                        fogData.end = view * 0.8f;
                    } else {
                        fogData.start = view * 0.25f;
                        fogData.end = view;
                    }
                }
            }
        }
    }
}
