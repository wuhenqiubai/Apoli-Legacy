package io.github.apace100.apoli.power.factory.condition.block.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.power.factory.condition.block.BlockCondition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;

public record OrBlockCondition(List<BlockCondition> conditions) implements BlockCondition {
    public static final MapCodec<OrBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BlockCondition.CODEC.listOf().fieldOf("conditions")
                .forGetter(OrBlockCondition::conditions)
        )
            .apply(instance, OrBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        for (BlockCondition condition : this.conditions()) {
            if (condition.test(blockInWorld))
                return true;
        }

        return false;
    }
}
