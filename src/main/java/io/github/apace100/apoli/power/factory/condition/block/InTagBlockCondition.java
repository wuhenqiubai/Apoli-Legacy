package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record InTagBlockCondition(
    TagKey<Block> tag
) implements BlockCondition {
    public static final MapCodec<InTagBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            TagKey.codec(Registries.BLOCK).fieldOf("tag")
                .forGetter(InTagBlockCondition::tag)
        )
            .apply(instance, InTagBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return blockInWorld.getState().is(this.tag());
    }
}
