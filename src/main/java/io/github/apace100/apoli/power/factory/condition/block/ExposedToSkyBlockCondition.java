package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class ExposedToSkyBlockCondition implements BlockCondition {
    public static final ExposedToSkyBlockCondition INSTANCE = new ExposedToSkyBlockCondition();
    public static final MapCodec<ExposedToSkyBlockCondition> CODEC = MapCodec.unit(INSTANCE);

    private ExposedToSkyBlockCondition() {}

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return blockInWorld.getLevel().canSeeSky(blockInWorld.getPos());
    }
}
