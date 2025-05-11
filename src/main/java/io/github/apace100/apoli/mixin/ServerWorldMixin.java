package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PreventGameEventPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    @Inject(method = "gameEvent", at = @At("HEAD"), cancellable = true)
    private void preventGameEventEmission(GameEvent event, Vec3 emitterPos, GameEvent.Context emitter, CallbackInfo ci) {
        if(emitter.sourceEntity() != null) {
            Entity entity = emitter.sourceEntity();
            List<PreventGameEventPower> preventingPowers = PowerHolderComponent.getPowers(entity, PreventGameEventPower.class).stream().filter(p -> p.doesPrevent(event)).toList();
            if(preventingPowers.size() > 0) {
                preventingPowers.forEach(p -> p.executeAction(entity));
                ci.cancel();
            }
        }
    }
}
