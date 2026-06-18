package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.util.Comparison;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record AdjacentBlockCondition(
    Comparison comparison, int compareTo,
    BlockCondition adjacentCondition
) implements BlockCondition {
    public static final MapCodec<AdjacentBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Comparison.CODEC.fieldOf("comparison")
                .forGetter(AdjacentBlockCondition::comparison),
            Codec.INT.fieldOf("compare_to")
                .forGetter(AdjacentBlockCondition::compareTo),
            BlockCondition.CODEC.fieldOf("adjacent_condition")
                .forGetter(AdjacentBlockCondition::adjacentCondition)
        )
            .apply(instance, AdjacentBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        int adjacent = 0;
        LevelReader level = blockInWorld.getLevel();
        BlockPos originalPos = blockInWorld.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            if (this.adjacentCondition().test(new BlockInWorld(level, pos.set(originalPos).relative(direction), true))) {
                adjacent++;
            }
        }

        return this.comparison().compare(adjacent, this.compareTo());
    }
}
