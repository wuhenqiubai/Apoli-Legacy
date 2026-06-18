package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class BlockEntityBlockCondition implements BlockCondition {
    public static final BlockEntityBlockCondition INSTANCE = new BlockEntityBlockCondition();
    public static final MapCodec<BlockEntityBlockCondition> CODEC = MapCodec.unit(INSTANCE);

    private BlockEntityBlockCondition() {}

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        return blockInWorld.getEntity() != null;
    }
}
