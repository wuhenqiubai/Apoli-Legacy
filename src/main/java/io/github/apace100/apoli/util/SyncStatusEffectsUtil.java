package io.github.apace100.apoli.util;

import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.calio.SerializationHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class SyncStatusEffectsUtil {

    public static void sendStatusEffectUpdatePacket(LivingEntity living, UpdateType type, MobEffectInstance instance) {
        if (living.level().isClientSide()) return;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(living.getId());
        buf.writeByte(type.ordinal());
        if(type != UpdateType.CLEAR) {
            SerializationHelper.writeStatusEffect(buf, instance);
        }
        for (ServerPlayer player : PlayerLookup.tracking(living)) {
            ServerPlayNetworking.send(player, ModPackets.SYNC_STATUS_EFFECT, buf);
        }
    }

    public enum UpdateType {
        CLEAR, APPLY, UPGRADE, REMOVE
    }
}