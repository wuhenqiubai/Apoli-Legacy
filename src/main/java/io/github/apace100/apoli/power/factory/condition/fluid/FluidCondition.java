package io.github.apace100.apoli.power.factory.condition.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.world.level.material.FluidState;

import java.util.function.Function;
import java.util.function.Predicate;

public interface FluidCondition extends Predicate<FluidState> {
    Codec<FluidCondition> CODEC = ApoliRegistries.FLUID_CONDITION.byNameCodec()
        .dispatch("type", FluidCondition::codec, Function.identity());

    MapCodec<? extends FluidCondition> codec();
}
