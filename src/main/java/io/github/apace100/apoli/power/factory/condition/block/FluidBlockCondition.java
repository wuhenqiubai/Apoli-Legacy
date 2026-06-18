package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.power.factory.condition.fluid.FluidCondition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record FluidBlockCondition(
    FluidCondition fluidCondition
) implements BlockCondition {
    public static final MapCodec<FluidBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            FluidCondition.CODEC.fieldOf("fluid_condition")
                .forGetter(FluidBlockCondition::fluidCondition)
        )
            .apply(instance, FluidBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return this.fluidCondition().test(blockInWorld.getLevel().getFluidState(blockInWorld.getPos()));
    }
}
