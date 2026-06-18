package io.github.apace100.apoli.power.factory.condition.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ItemCondition extends Predicate<ItemStack> {
    Codec<ItemCondition> CODEC = ApoliRegistries.ITEM_CONDITION.byNameCodec()
        .dispatch("type", ItemCondition::codec, Function.identity());

    MapCodec<? extends ItemCondition> codec();
}
