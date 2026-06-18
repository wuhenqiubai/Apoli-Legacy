package io.github.apace100.apoli.power.factory.condition.block.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.power.factory.condition.block.BlockCondition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record ConstantBlockCondition(boolean value) implements BlockCondition {
    public static final MapCodec<ConstantBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.BOOL.fieldOf("value")
                .forGetter(ConstantBlockCondition::value)
        )
            .apply(instance, ConstantBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return this.value();
    }
}
