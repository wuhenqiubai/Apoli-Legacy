package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import net.minecraft.resources.ResourceLocation;

public class ModPackets {

    public static final ResourceLocation HANDSHAKE = Apoli.identifier("handshake");

    public static final ResourceLocation USE_ACTIVE_POWERS = Apoli.identifier("use_active_powers");
    public static final ResourceLocation POWER_LIST = Apoli.identifier("power_list");
    public static final ResourceLocation SYNC_POWER = Apoli.identifier("sync_power");

    public static final ResourceLocation PLAYER_LANDED = Apoli.identifier("player_landed");

    public static final ResourceLocation PLAYER_MOUNT = Apoli.identifier("player_mount");
    public static final ResourceLocation PLAYER_DISMOUNT = Apoli.identifier("player_dismount");

    public static final ResourceLocation PREVENTED_ENTITY_USE = Apoli.identifier("prevented_entity_use");

    public static final ResourceLocation SET_ATTACKER = Apoli.identifier("set_attacker");

    public static final ResourceLocation SYNC_STATUS_EFFECT = Apoli.identifier("sync_status_effect");
}
