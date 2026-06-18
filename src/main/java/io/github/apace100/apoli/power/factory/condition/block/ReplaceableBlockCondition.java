package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class ReplaceableBlockCondition implements BlockCondition {
    public static final ReplaceableBlockCondition INSTANCE = new ReplaceableBlockCondition();
    public static final MapCodec<ReplaceableBlockCondition> CODEC = MapCodec.unit(INSTANCE);

    private ReplaceableBlockCondition() {}

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return blockInWorld.getState().canBeReplaced();
    }
}
