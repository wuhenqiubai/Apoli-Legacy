package io.github.apace100.apoli.power.factory.condition.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;
import java.util.function.Predicate;

public interface BiEntityCondition extends Predicate<Tuple<Entity, Entity>> {
    Codec<BiEntityCondition> CODEC = ApoliRegistries.BIENTITY_CONDITION.byNameCodec()
        .dispatch("type", BiEntityCondition::codec, Function.identity());

    MapCodec<? extends BiEntityCondition> codec();
}
