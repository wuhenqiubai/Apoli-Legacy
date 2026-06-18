package io.github.apace100.apoli.util.datafix;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

public class DataFixingRecipeCodec<R extends RecipeInput> implements Codec<Recipe<R>> {
    @Override
    public <T> DataResult<Pair<Recipe<R>, T>> decode(DynamicOps<T> ops, T input) {
        var dynamic = new Dynamic<>(ops, input);
        return DataResult.error(() -> "fuck");
    }

    @Override
    public <T> DataResult<T> encode(Recipe<R> input, DynamicOps<T> ops, T prefix) {
        return DataResult.error(() -> "fuck");
    }
}
