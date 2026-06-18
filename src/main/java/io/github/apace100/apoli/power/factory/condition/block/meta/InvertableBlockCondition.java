package io.github.apace100.apoli.power.factory.condition.block.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.power.factory.condition.block.BlockCondition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class InvertableBlockCondition<T extends BlockCondition> implements BlockCondition {
    private final T condition;
    private final boolean inverted;
    private final MapCodec<T> originalCodec;

    private final MapCodec<InvertableBlockCondition<T>> invertableCodec = createInvertable(this.originalCodec());

    public InvertableBlockCondition(T condition, boolean inverted, MapCodec<T> originalCodec) {
        this.condition = condition;
        this.inverted = inverted;
        this.originalCodec = originalCodec;
    }

    public static <T extends BlockCondition> MapCodec<InvertableBlockCondition<T>> createInvertable(MapCodec<T> original) {
        return RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                original.forGetter(InvertableBlockCondition<T>::condition),
                Codec.BOOL.optionalFieldOf("inverted", false)
                    .forGetter(InvertableBlockCondition::inverted)
            )
                .apply(instance, (condition, inverted) -> new InvertableBlockCondition<>(condition, inverted, original))
        );
    }

    public T condition() {
        return this.condition;
    }

    public MapCodec<T> originalCodec() {
        return this.originalCodec;
    }

    public boolean inverted() {
        return this.inverted;
    }

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return this.invertableCodec;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        var fulfilled = this.condition().test(blockInWorld);

        if (this.inverted())
            return !fulfilled;
        else
            return fulfilled;
    }
}
