package io.github.apace100.apoli.power.factory.condition;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.block.*;
import io.github.apace100.apoli.power.factory.condition.block.meta.AndBlockCondition;
import io.github.apace100.apoli.power.factory.condition.block.meta.ConstantBlockCondition;
import io.github.apace100.apoli.power.factory.condition.block.meta.OffsetBlockCondition;
import io.github.apace100.apoli.power.factory.condition.block.meta.OrBlockCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class BlockConditions {

    public static void register() {
        register(Apoli.identifier("constant"), ConstantBlockCondition.CODEC);
        register(Apoli.identifier("and"), AndBlockCondition.CODEC);
        register(Apoli.identifier("or"), OrBlockCondition.CODEC);
        register(Apoli.identifier("offset"), OffsetBlockCondition.CODEC);
        register(Apoli.identifier("height"), HeightBlockCondition.CODEC);
        DistanceFromCoordinatesConditionRegistry.registerBlockCondition(BlockConditions::register);
        register(Apoli.identifier("block"), MatchingBlockCondition.CODEC);
        register(Apoli.identifier("in_tag"), InTagBlockCondition.CODEC);
        register(Apoli.identifier("adjacent"), AdjacentBlockCondition.CODEC);
        register(Apoli.identifier("replacable"), ReplaceableBlockCondition.CODEC);
        register(Apoli.identifier("attachable"), AttachableBlockCondition.CODEC);
        register(Apoli.identifier("fluid"), FluidBlockCondition.CODEC);
        register(Apoli.identifier("movement_blocking"), MovementBlockingBlockCondition.CODEC);
        register(Apoli.identifier("light_blocking"), LightBlockingBlockCondition.CODEC);
        register(Apoli.identifier("water_loggable"), WaterLoggableBlockCondition.CODEC);
        register(Apoli.identifier("exposed_to_sky"), ExposedToSkyBlockCondition.CODEC);
        register(Apoli.identifier("light_level"), LightLevelBlockCondition.CODEC);
        register(Apoli.identifier("block_state"), BlockStateBlockCondition.CODEC);
        register(Apoli.identifier("block_entity"), BlockEntityBlockCondition.CODEC);
        register(Apoli.identifier("nbt"), NbtBlockCondition.CODEC);
        register(Apoli.identifier("slipperiness"), SlipperinessBlockCondition.CODEC);
        register(Apoli.identifier("blast_resistance"), BlastResistanceBlockCondition.CODEC);
        register(Apoli.identifier("hardness"), HardnessBlockCondition.CODEC);
        register(Apoli.identifier("material"), MaterialBlockCondition.CODEC);
    }

    private static <T extends BlockCondition> void register(Identifier id, MapCodec<T> conditionFactory) {
        Registry.register(ApoliRegistries.BLOCK_CONDITION, id, conditionFactory);
    }
}
