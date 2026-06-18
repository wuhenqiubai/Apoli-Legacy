package io.github.apace100.apoli.power.factory.condition.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public record NbtBlockCondition(CompoundTag nbt) implements BlockCondition {
    public static final MapCodec<NbtBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            CompoundTag.CODEC.fieldOf("nbt")
                .forGetter(NbtBlockCondition::nbt)
        )
            .apply(instance, NbtBlockCondition::new)
    );

    @Override
    public MapCodec<? extends BlockCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(BlockInWorld blockInWorld) {
        CompoundTag nbt = new CompoundTag();
        if (blockInWorld.getEntity() != null) {
            nbt = blockInWorld.getEntity().saveWithFullMetadata(blockInWorld.getLevel().registryAccess());
        }

        return NbtUtils.compareNbt(this.nbt(), nbt, true);
    }
}
