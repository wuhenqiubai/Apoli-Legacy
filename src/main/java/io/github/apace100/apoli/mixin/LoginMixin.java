package io.github.apace100.apoli.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
@Mixin(value = PlayerList.class, priority = 800)
public abstract class LoginMixin {

	@Shadow public abstract List<ServerPlayer> getPlayers();

	@Inject(at = @At("TAIL"), method = "placeNewPlayer")
	private void syncPowerTypes(Connection connection, ServerPlayer player, CallbackInfo info) {
		FriendlyByteBuf powerListData = new FriendlyByteBuf(Unpooled.buffer());
		powerListData.writeInt(PowerTypeRegistry.size());
		PowerTypeRegistry.entries().forEach((entry) -> {
			PowerType<?> type = entry.getValue();
			PowerFactory.Instance factory = type.getFactory();
			if(factory != null) {
				powerListData.writeResourceLocation(entry.getKey());
				factory.write(powerListData);
				if(type instanceof MultiplePowerType) {
					powerListData.writeBoolean(true);
					ImmutableList<ResourceLocation> subPowers = ((MultiplePowerType<?>)type).getSubPowers();
					powerListData.writeVarInt(subPowers.size());
					subPowers.forEach(powerListData::writeResourceLocation);
				} else {
					powerListData.writeBoolean(false);
				}
				powerListData.writeUtf(type.getOrCreateNameTranslationKey());
				powerListData.writeUtf(type.getOrCreateDescriptionTranslationKey());
				powerListData.writeBoolean(type.isHidden());
			}
		});

		ServerPlayNetworking.send(player, ModPackets.POWER_LIST, powerListData);

		List<ServerPlayer> playerList = getPlayers();
		playerList.forEach(spe -> {
			PowerHolderComponent.KEY.syncWith(spe, (ComponentProvider) player);
			PowerHolderComponent.KEY.syncWith(player, (ComponentProvider) spe);
		});
		PowerHolderComponent.sync(player);
	}

	@WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
	private void preventEndExitSpawnPointSetting(ServerPlayer playerEntity, ResourceKey<Level> dimension, BlockPos position, float angle, boolean forced, boolean sendMessage, Operation<Void> original) {
		EndRespawningEntity ere = (EndRespawningEntity) playerEntity;
		// Prevent setting the spawn point if the player has a "fake" respawn point
		if(ere.hasRealRespawnPoint()) {
			original.call(playerEntity, dimension, position, angle, forced, sendMessage);
		}
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void invokeOnRemovedCallback(ServerPlayer player, CallbackInfo ci) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(player);
		component.getPowers().forEach(Power::onRemoved);
	}

	@WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;findRespawnPositionAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;"))
	private Optional<Vec3> retryObstructedSpawnpointIfFailed(ServerLevel world, BlockPos pos, float f, boolean bl, boolean bl2, Operation<Optional<Vec3>> operation, ServerPlayer player, boolean alive) {
		Optional<Vec3> original = operation.call(world, pos, f, bl, bl2);
		if(!original.isPresent()) {
			if(PowerHolderComponent.hasPower(player, ModifyPlayerSpawnPower.class)) {
				return Optional.ofNullable(DismountHelper.findSafeDismountLocation(EntityType.PLAYER, world, pos, bl));
			}
		}
		return original;
	}

	@Inject(method = "respawn", at = @At("HEAD"))
	private void invokePowerRemovedCallback(ServerPlayer player, boolean alive, CallbackInfoReturnable<ServerPlayer> cir) {
		List<Power> powers = PowerHolderComponent.KEY.get(player).getPowers();
		powers.forEach(Power::onRemoved);
	}

	@Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;initInventoryMenu()V"))
	private void invokePowerRespawnCallback(ServerPlayer player, boolean alive, CallbackInfoReturnable<ServerPlayer> cir, @Local(argsOnly = true) ServerPlayer serverPlayerEntity) {
		if(!alive) {
			List<Power> powers = PowerHolderComponent.KEY.get(serverPlayerEntity).getPowers();
			powers.forEach(Power::onRespawn);
		}
	}
}
