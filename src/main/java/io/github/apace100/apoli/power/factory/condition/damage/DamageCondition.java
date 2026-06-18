package io.github.apace100.apoli.power.factory.condition.damage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;

import java.util.function.Function;
import java.util.function.Predicate;

public interface DamageCondition extends Predicate<Tuple<DamageSource, Float>> {
    Codec<DamageCondition> CODEC = ApoliRegistries.DAMAGE_CONDITION.byNameCodec()
        .dispatch("type", DamageCondition::codec, Function.identity());

    MapCodec<? extends DamageCondition> codec();
}
