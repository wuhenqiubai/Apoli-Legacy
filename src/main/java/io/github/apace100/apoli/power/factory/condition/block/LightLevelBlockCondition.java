package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.codec.ApoliCodecs;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.Optional;

public record LightLevelBlockCondition(
    Comparison comparison, int compareTo,
    Optional<LightLayer> lightType
) implements BlockCondition {
    public static final MapCodec<LightLevelBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Comparison.CODEC.fieldOf("comparison")
                .forGetter(LightLevelBlockCondition::comparison),
            Codec.INT.fieldOf("compare_to")
                .forGetter(LightLevelBlockCondition::compareTo),
            ApoliCodecs.enumCodec(LightLayer.class).optionalFieldOf("light_type")
                .forGetter(LightLevelBlockCondition::lightType)
        )
            .apply(instance, LightLevelBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        int value;
        if (this.lightType().isPresent()) {
            value = blockInWorld.getLevel().getBrightness(this.lightType().orElseThrow(), blockInWorld.getPos());
        } else {
            value = blockInWorld.getLevel().getMaxLocalRawBrightness(blockInWorld.getPos());
        }

        return this.comparison().compare(value, this.compareTo());
    }
}
