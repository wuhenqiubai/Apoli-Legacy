package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.util.Comparison;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

public record BlockStateBlockCondition(
    String propertyName,
    Optional<Comparison> comparison,
    Optional<Integer> compareTo,
    Optional<Boolean> value,
    Optional<String> enumValue
) implements BlockCondition {
    public static final MapCodec<BlockStateBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.fieldOf("property")
                .forGetter(BlockStateBlockCondition::propertyName),
            Comparison.CODEC.optionalFieldOf("comparison")
                .forGetter(BlockStateBlockCondition::comparison),
            Codec.INT.optionalFieldOf("compare_to")
                .forGetter(BlockStateBlockCondition::compareTo),
            Codec.BOOL.optionalFieldOf("value")
                .forGetter(BlockStateBlockCondition::value),
            Codec.STRING.optionalFieldOf("enum")
                .forGetter(BlockStateBlockCondition::enumValue)
        )
            .apply(instance, BlockStateBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        var state = blockInWorld.getState();
        var properties = state.getProperties();

        Property<?> property = null;
        for (Property<?> p : properties) {
            if (p.getName().equals(this.propertyName())) {
                property = p;
                break;
            }
        }

        if (property != null) {
            var value = state.getValue(property);

            return switch (value) {
                case Enum<?> val when this.enumValue().isPresent() ->
                    val.name().equalsIgnoreCase(this.enumValue().orElseThrow());

                case Boolean bool when this.value().isPresent() ->
                    bool == this.value().orElseThrow();

                case Integer integer when this.comparison().isPresent() && this.compareTo().isPresent() ->
                    this.comparison().orElseThrow().compare(integer, this.compareTo().orElseThrow());

                default -> true;
            };
        }

        return false;
    }
}
