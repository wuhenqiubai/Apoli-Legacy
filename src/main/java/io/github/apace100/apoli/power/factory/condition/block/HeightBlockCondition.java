package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.util.Comparison;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record HeightBlockCondition(
    Comparison comparison,
    int compareTo
) implements BlockCondition {
    public static final MapCodec<HeightBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Comparison.CODEC.fieldOf("comparison")
                .forGetter(HeightBlockCondition::comparison),
            Codec.INT.fieldOf("compare_to")
                .forGetter(HeightBlockCondition::compareTo)
        )
            .apply(instance, HeightBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return this.comparison().compare(blockInWorld.getPos().getY(), this.compareTo());
    }
}
