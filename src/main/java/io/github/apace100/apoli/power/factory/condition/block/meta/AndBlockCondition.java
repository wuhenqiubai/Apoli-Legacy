package io.github.apace100.apoli.power.factory.condition.block.meta;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.power.factory.condition.block.BlockCondition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;

public record AndBlockCondition(List<BlockCondition> conditions) implements BlockCondition {
    public static final MapCodec<AndBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BlockCondition.CODEC.listOf().fieldOf("conditions")
                .forGetter(AndBlockCondition::conditions)
        )
            .apply(instance, AndBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        for (BlockCondition condition : this.conditions()) {
            if (!condition.test(blockInWorld))
                return false;
        }

        return true;
    }
}
