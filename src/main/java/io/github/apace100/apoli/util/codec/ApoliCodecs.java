package io.github.apace100.apoli.util.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public class ApoliCodecs {
    public static final Codec<Vec3> VECTOR = Codec.withAlternative(
        RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf("x", 0.0)
                    .forGetter(Vec3::x),
                Codec.DOUBLE.optionalFieldOf("y", 0.0)
                    .forGetter(Vec3::y),
                Codec.DOUBLE.optionalFieldOf("z", 0.0)
                    .forGetter(Vec3::z)
            )
                .apply(instance, Vec3::new)
        ),
        Vec3.CODEC
    );

    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumClass) {
        return Codec.STRING.xmap(String::toUpperCase, String::toLowerCase)
            .xmap(string -> Enum.valueOf(enumClass, string), Enum::name);
    }
}
