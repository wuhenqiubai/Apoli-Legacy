package io.github.apace100.apoli.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface ApoliPlayerEvent {
    Event<ApoliPlayerEvent> POWERS_SYNCED = EventFactory.createArrayBacked(ApoliPlayerEvent.class, callbacks -> player -> {
        for (ApoliPlayerEvent callback : callbacks) {
            callback.onApoliPlayerEvent(player);
        }
    });

    void onApoliPlayerEvent(Player player);
}
