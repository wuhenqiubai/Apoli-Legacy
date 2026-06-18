package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AttachableBlockCondition implements BlockCondition {
    public static final AttachableBlockCondition INSTANCE = new AttachableBlockCondition();
    public static final MapCodec<AttachableBlockCondition> CODEC = MapCodec.unit(INSTANCE);

    private AttachableBlockCondition() {}

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos originalPos = blockInWorld.getPos();
        for (Direction direction : Direction.values()) {
            if (blockInWorld.getLevel().getBlockState(pos.set(originalPos).relative(direction))
                .isFaceSturdy(blockInWorld.getLevel(), originalPos, direction.getOpposite())
            )
                return true;
        }

        return false;
    }
}
