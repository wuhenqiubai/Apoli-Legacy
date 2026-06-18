package io.github.apace100.apoli.power.factory.condition.entity;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public interface EntityCondition extends Predicate<Entity> {
    MapCodec<? extends EntityCondition> codec();
}
