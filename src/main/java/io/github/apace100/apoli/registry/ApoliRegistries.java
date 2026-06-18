package io.github.apace100.apoli.registry;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.bientity.BiEntityCondition;
import io.github.apace100.apoli.power.factory.condition.biome.BiomeCondition;
import io.github.apace100.apoli.power.factory.condition.block.BlockCondition;
import io.github.apace100.apoli.power.factory.condition.damage.DamageCondition;
import io.github.apace100.apoli.power.factory.condition.entity.EntityCondition;
import io.github.apace100.apoli.power.factory.condition.fluid.FluidCondition;
import io.github.apace100.apoli.power.factory.condition.item.ItemCondition;
import io.github.apace100.apoli.util.modifier.IModifierOperation;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;

public class ApoliRegistries {

    public static final Registry<PowerFactory> POWER_FACTORY;
    public static final Registry<MapCodec<? extends EntityCondition>> ENTITY_CONDITION = create(ApoliRegistryKeys.ENTITY_CONDITION);
    public static final Registry<MapCodec<? extends BiEntityCondition>> BIENTITY_CONDITION = create(ApoliRegistryKeys.BIENTITY_CONDITION);
    public static final Registry<MapCodec<? extends ItemCondition>> ITEM_CONDITION = create(ApoliRegistryKeys.ITEM_CONDITION);
    public static final Registry<MapCodec<? extends BlockCondition>> BLOCK_CONDITION = create(ApoliRegistryKeys.BLOCK_CONDITION);
    public static final Registry<MapCodec<? extends DamageCondition>> DAMAGE_CONDITION = create(ApoliRegistryKeys.DAMAGE_CONDITION);
    public static final Registry<MapCodec<? extends FluidCondition>> FLUID_CONDITION = create(ApoliRegistryKeys.FLUID_CONDITION);
    public static final Registry<MapCodec<? extends BiomeCondition>> BIOME_CONDITION = create(ApoliRegistryKeys.BIOME_CONDITION);
    public static final Registry<ActionFactory<Entity>> ENTITY_ACTION;
    public static final Registry<ActionFactory<Tuple<Level, ItemStack>>> ITEM_ACTION;
    public static final Registry<ActionFactory<Triple<Level, BlockPos, Direction>>> BLOCK_ACTION;
    public static final Registry<ActionFactory<Tuple<Entity, Entity>>> BIENTITY_ACTION;
    public static final Registry<IModifierOperation> MODIFIER_OPERATION;

    private static <T> Registry<T> create(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.create(key)
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    }

    static {
        POWER_FACTORY = FabricRegistryBuilder.create(PowerFactory.class, Apoli.identifier("power_factory")).buildAndRegister();
        ENTITY_ACTION = FabricRegistryBuilder.create(ClassUtil.<ActionFactory<Entity>>castClass(ActionFactory.class), Apoli.identifier("entity_action")).buildAndRegister();
        ITEM_ACTION = FabricRegistryBuilder.create(ClassUtil.<ActionFactory<Tuple<Level, ItemStack>>>castClass(ActionFactory.class), Apoli.identifier("item_action")).buildAndRegister();
        BLOCK_ACTION = FabricRegistryBuilder.create(ClassUtil.<ActionFactory<Triple<Level, BlockPos, Direction>>>castClass(ActionFactory.class), Apoli.identifier("block_action")).buildAndRegister();
        BIENTITY_ACTION = FabricRegistryBuilder.create(ClassUtil.<ActionFactory<Tuple<Entity, Entity>>>castClass(ActionFactory.class), Apoli.identifier("bientity_action")).buildAndRegister();
        MODIFIER_OPERATION = FabricRegistryBuilder.create(IModifierOperation.class, Apoli.identifier("modifier_operation")).buildAndRegister();
    }
}
