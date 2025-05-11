package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import io.github.apace100.calio.SerializationHelper;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(ModPackets.POWER_LIST, ModPacketsS2C::receivePowerList);
            ClientPlayNetworking.registerReceiver(ModPackets.SYNC_POWER, ModPacketsS2C::onPowerSync);
            ClientPlayNetworking.registerReceiver(ModPackets.PLAYER_MOUNT, ModPacketsS2C::onPlayerMount);
            ClientPlayNetworking.registerReceiver(ModPackets.PLAYER_DISMOUNT, ModPacketsS2C::onPlayerDismount);
            ClientPlayNetworking.registerReceiver(ModPackets.SET_ATTACKER, ModPacketsS2C::onSetAttacker);
            ClientPlayNetworking.registerReceiver(ModPackets.SYNC_STATUS_EFFECT, ModPacketsS2C::onStatusEffectSync);
        }));
    }

    private static void onStatusEffectSync(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        int targetId = packetByteBuf.readInt();
        SyncStatusEffectsUtil.UpdateType updateType = SyncStatusEffectsUtil.UpdateType.values()[packetByteBuf.readByte()];
        MobEffectInstance instance = null;
        if(updateType != SyncStatusEffectsUtil.UpdateType.CLEAR) {
            instance = SerializationHelper.readStatusEffect(packetByteBuf);
        }
        MobEffectInstance finalInstance = instance;
        minecraftClient.execute(() -> {
            Entity target = clientPlayNetworkHandler.getLevel().getEntity(targetId);
            if (!(target instanceof LivingEntity living)) {
                Apoli.LOGGER.warn("Received unknown target for status effect synchronization");
            } else {
                switch(updateType) {
                    case CLEAR -> living.getActiveEffectsMap().clear();
                    case APPLY, UPGRADE -> living.getActiveEffectsMap().put(finalInstance.getEffect(), finalInstance);
                    case REMOVE -> living.getActiveEffectsMap().remove(finalInstance.getEffect());
                }
            }
        });
    }

    private static void onSetAttacker(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        int targetId = packetByteBuf.readInt();
        boolean hasAttacker = packetByteBuf.readBoolean();
        int attackerId = 0;
        if(hasAttacker) {
            attackerId = packetByteBuf.readInt();
        }
        int finalAttackerId = attackerId;
        minecraftClient.execute(() -> {
            Entity target = clientPlayNetworkHandler.getLevel().getEntity(targetId);
            Entity attacker = null;
            if(hasAttacker) {
                attacker = clientPlayNetworkHandler.getLevel().getEntity(finalAttackerId);
            }
            if (!(target instanceof LivingEntity)) {
                Apoli.LOGGER.warn("Received unknown target");
            } else if(hasAttacker && !(attacker instanceof LivingEntity)) {
                Apoli.LOGGER.warn("Received unknown attacker");
            } else {
                if(hasAttacker) {
                    ((LivingEntity)target).setLastHurtByMob((LivingEntity)attacker);
                } else {
                    ((LivingEntity)target).setLastHurtByMob(null);
                }
            }
        });
    }


    @Environment(EnvType.CLIENT)
    private static CompletableFuture<FriendlyByteBuf> handleHandshake(Minecraft minecraftClient, ClientHandshakePacketListenerImpl clientLoginNetworkHandler, FriendlyByteBuf packetByteBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(Apoli.SEMVER.length);
        for(int i = 0; i < Apoli.SEMVER.length; i++) {
            buf.writeInt(Apoli.SEMVER[i]);
        }
        return CompletableFuture.completedFuture(buf);
    }

    @Environment(EnvType.CLIENT)
    private static void receivePowerList(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        int powerCount = packetByteBuf.readInt();
        HashMap<ResourceLocation, PowerType> factories = new HashMap<>();
        for(int i = 0; i < powerCount; i++) {
            ResourceLocation powerId = packetByteBuf.readResourceLocation();
            ResourceLocation factoryId = packetByteBuf.readResourceLocation();
            try {
                PowerFactory factory = ApoliRegistries.POWER_FACTORY.get(factoryId);
                PowerFactory.Instance factoryInstance = factory.read(packetByteBuf);
                PowerType type;
                if(packetByteBuf.readBoolean()) {
                    type = new MultiplePowerType(powerId, factoryInstance);
                    int subPowerCount = packetByteBuf.readVarInt();
                    List<ResourceLocation> subPowers = new ArrayList<>(subPowerCount);
                    for(int j = 0; j < subPowerCount; j++) {
                        subPowers.add(packetByteBuf.readResourceLocation());
                    }
                    ((MultiplePowerType)type).setSubPowers(subPowers);
                } else {
                    type = new PowerType(powerId, factoryInstance);
                }
                type.setTranslationKeys(packetByteBuf.readUtf(), packetByteBuf.readUtf());
                if (packetByteBuf.readBoolean()) {
                    type.setHidden();
                }
                factories.put(powerId, type);
            } catch(Exception e) {
                Apoli.LOGGER.error("Error while receiving \"" + powerId + "\" (factory: \"" + factoryId + "\"): " + e.getMessage());
                e.printStackTrace();
            }
        }
        minecraftClient.execute(() -> {
            PowerTypeRegistry.clear();
            factories.forEach(PowerTypeRegistry::register);
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onPlayerMount(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        int mountingPlayerId = packetByteBuf.readInt();
        int mountedPlayerId = packetByteBuf.readInt();
        minecraftClient.execute(() -> {
            Entity mountingPlayer = clientPlayNetworkHandler.getLevel().getEntity(mountingPlayerId);
            Entity mountedPlayer = clientPlayNetworkHandler.getLevel().getEntity(mountedPlayerId);
            if (mountedPlayer == null) {
                Apoli.LOGGER.warn("Received passenger for unknown player");
            } else if(mountingPlayer == null) {
                Apoli.LOGGER.warn("Received unknown passenger for player");
            } else {
                boolean result = mountingPlayer.startRiding(mountedPlayer, true);
                if(result) {
                    Apoli.LOGGER.info(mountingPlayer.getDisplayName().getString() + " started riding " + mountedPlayer.getDisplayName().getString());
                } else {
                    Apoli.LOGGER.warn(mountingPlayer.getDisplayName().getString() + " failed to start riding " + mountedPlayer.getDisplayName().getString());
                }
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onPlayerDismount(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        int dismountingPlayerId = packetByteBuf.readInt();
        minecraftClient.execute(() -> {
            Entity dismountingPlayer = clientPlayNetworkHandler.getLevel().getEntity(dismountingPlayerId);
            if (dismountingPlayer == null) {
                Apoli.LOGGER.warn("Unknown player tried to dismount");
            } else {
                if(dismountingPlayer.getVehicle() instanceof Player) {
                    dismountingPlayer.removeVehicle();
                }
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onPowerSync(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        int entityId = packetByteBuf.readInt();
        ResourceLocation powerId = packetByteBuf.readResourceLocation();
        CompoundTag powerNbtContainer = packetByteBuf.readNbt();
        Tag powerNbt = powerNbtContainer.get("Data");
        minecraftClient.execute(() -> {
            if(!PowerTypeRegistry.contains(powerId)) {
                Apoli.LOGGER.warn("Received sync packet for unknown power type: " + powerId);
                return;
            }
            Entity entity = clientPlayNetworkHandler.getLevel().getEntity(entityId);
            if (entity == null) {
                Apoli.LOGGER.warn("Received sync packet for unknown power holder.");
                return;
            }
            PowerType<?> powerType = PowerTypeRegistry.get(powerId);
            PowerHolderComponent.KEY.maybeGet(entity).ifPresentOrElse(phc -> {
                Power power = phc.getPower(powerType);
                power.fromTag(powerNbt);
            }, () -> Apoli.LOGGER.warn("Received sync packet for entity without power holder."));
        });
    }
}
