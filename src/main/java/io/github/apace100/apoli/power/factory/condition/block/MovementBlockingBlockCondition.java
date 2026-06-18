package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class MovementBlockingBlockCondition implements BlockCondition {
    public static final MovementBlockingBlockCondition INSTANCE = new MovementBlockingBlockCondition();
    public static final MapCodec<MovementBlockingBlockCondition> CODEC = MapCodec.unit(INSTANCE);

    private MovementBlockingBlockCondition() {}

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return blockInWorld.getState().blocksMotion() && !blockInWorld.getState().getCollisionShape(blockInWorld.getLevel(), blockInWorld.getPos()).isEmpty();
    }
}
