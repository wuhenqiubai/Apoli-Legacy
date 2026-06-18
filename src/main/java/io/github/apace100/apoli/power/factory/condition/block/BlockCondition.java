package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.power.factory.condition.block.meta.InvertableBlockCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.function.Function;
import java.util.function.Predicate;

public interface BlockCondition extends Predicate<BlockInWorld> {
    MapCodec<BlockCondition> MAP_CODEC = ApoliRegistries.BLOCK_CONDITION.byNameCodec()
        .dispatchMap("type", BlockCondition::codec, Function.identity());

    Codec<BlockCondition> DIRECT_CODEC = MAP_CODEC.codec();
    Codec<BlockCondition> CODEC = (Codec<BlockCondition>) (Object) InvertableBlockCondition.createInvertable(MAP_CODEC).codec();

    MapCodec<? extends BlockCondition> codec();
}
