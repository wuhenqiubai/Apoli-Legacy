package io.github.apace100.apoli.util.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class MapCodecUtil {
    public static <T> MapCodec<T> withAlternative(
        MapCodec<T> primary,
        MapCodec<? extends T> alternative
    ) {
        return Codec.mapEither(primary, alternative)
            .xmap(Either::unwrap, Either::left);
    }
}
