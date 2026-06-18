package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class WaterLoggableBlockCondition implements BlockCondition {
    public static final WaterLoggableBlockCondition INSTANCE = new WaterLoggableBlockCondition();
    public static final MapCodec<WaterLoggableBlockCondition> CODEC = MapCodec.unit(INSTANCE);

    private WaterLoggableBlockCondition() {}

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return blockInWorld.getState().getBlock() instanceof LiquidBlockContainer;
    }
}
