package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.util.Comparison;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record BlastResistanceBlockCondition(
    Comparison comparison,
    float compareTo
) implements BlockCondition {
    public static final MapCodec<BlastResistanceBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Comparison.CODEC.fieldOf("comparison")
                .forGetter(BlastResistanceBlockCondition::comparison),
            Codec.FLOAT.fieldOf("compare_to")
                .forGetter(BlastResistanceBlockCondition::compareTo)
        )
            .apply(instance, BlastResistanceBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return this.comparison().compare(blockInWorld.getState().getBlock().getExplosionResistance(), this.compareTo());
    }
}
