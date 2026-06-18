package io.github.apace100.apoli.power.factory.condition.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Function;
import java.util.function.Predicate;

public interface BiomeCondition extends Predicate<Holder<Biome>> {
    Codec<BiomeCondition> CODEC = ApoliRegistries.BIOME_CONDITION.byNameCodec()
        .dispatch("type", BiomeCondition::codec, Function.identity());

    MapCodec<? extends BiomeCondition> codec();
}
