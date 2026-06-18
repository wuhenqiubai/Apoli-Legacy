package io.github.apace100.apoli.power.factory.condition.block.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.power.factory.condition.block.BlockCondition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record OffsetBlockCondition(
    BlockCondition condition,
    int x, int y, int z
) implements BlockCondition {
    public static final MapCodec<OffsetBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BlockCondition.CODEC.fieldOf("condition")
                .forGetter(OffsetBlockCondition::condition),
            Codec.INT.optionalFieldOf("x", 0)
                .forGetter(OffsetBlockCondition::x),
            Codec.INT.optionalFieldOf("y", 0)
                .forGetter(OffsetBlockCondition::y),
            Codec.INT.optionalFieldOf("z", 0)
                .forGetter(OffsetBlockCondition::z)
        )
            .apply(instance, OffsetBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return this.condition().test(new BlockInWorld(blockInWorld.getLevel(), blockInWorld.getPos().offset(x, y, z), true));
    }
}
