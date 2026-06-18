package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.util.Comparison;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record SlipperinessBlockCondition(
    Comparison comparison,
    float compareTo
) implements BlockCondition {
    public static final MapCodec<SlipperinessBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Comparison.CODEC.fieldOf("comparison")
                .forGetter(SlipperinessBlockCondition::comparison),
            Codec.FLOAT.fieldOf("compare_to")
                .forGetter(SlipperinessBlockCondition::compareTo)
        )
            .apply(instance, SlipperinessBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return this.comparison().compare(blockInWorld.getState().getBlock().getFriction(), this.compareTo());
    }
}
