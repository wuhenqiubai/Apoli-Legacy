package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class LightBlockingBlockCondition implements BlockCondition {
    public static final LightBlockingBlockCondition INSTANCE = new LightBlockingBlockCondition();
    public static final MapCodec<LightBlockingBlockCondition> CODEC = MapCodec.unit(INSTANCE);

    private LightBlockingBlockCondition() {}

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return blockInWorld.getState().canOcclude();
    }
}
