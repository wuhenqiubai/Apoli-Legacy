package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record MatchingBlockCondition(
    Block block
) implements BlockCondition {
    public static final MapCodec<MatchingBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block")
                .forGetter(MatchingBlockCondition::block)
        )
            .apply(instance, MatchingBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return blockInWorld.getState().is(this.block());
    }
}
