package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.LegacyMaterial;
import io.github.apace100.apoli.util.codec.MapCodecUtil;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.List;

public record MaterialBlockCondition(
    List<LegacyMaterial> materials
) implements BlockCondition {
    public static final MapCodec<MaterialBlockCondition> CODEC = MapCodecUtil.withAlternative(
        RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ApoliDataTypes.LEGACY_MATERIAL.listOf().fieldOf("materials")
                        .forGetter(MaterialBlockCondition::materials)
                )
                .apply(instance, MaterialBlockCondition::new)
        ),
        RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ApoliDataTypes.LEGACY_MATERIAL.fieldOf("material")
                        .forGetter(condition -> condition.materials().getFirst())
                )
                .apply(instance, MaterialBlockCondition::new)
        )
    );

    public MaterialBlockCondition(LegacyMaterial material) {
        this(List.of(material));
    }

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        var state = blockInWorld.getState();
        for (LegacyMaterial material : this.materials()) {
            if (material.blockStateIsOfMaterial(state)) {
                return true;
            }
        }

        return false;
    }
}
