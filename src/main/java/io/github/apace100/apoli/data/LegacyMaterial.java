package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record LegacyMaterial(
    TagKey<Block> materialTagKey,
    String material
) {
    public LegacyMaterial(String material) {
        this(TagKey.create(Registries.BLOCK, Apoli.identifier("material/" + material)), material);
    }

    public boolean blockStateIsOfMaterial(BlockState blockState) {
        return blockState.is(materialTagKey);
    }
}
