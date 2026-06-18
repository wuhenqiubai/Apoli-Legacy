package io.github.apace100.apoli.registry;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.bientity.BiEntityCondition;
import io.github.apace100.apoli.power.factory.condition.biome.BiomeCondition;
import io.github.apace100.apoli.power.factory.condition.block.BlockCondition;
import io.github.apace100.apoli.power.factory.condition.damage.DamageCondition;
import io.github.apace100.apoli.power.factory.condition.entity.EntityCondition;
import io.github.apace100.apoli.power.factory.condition.fluid.FluidCondition;
import io.github.apace100.apoli.power.factory.condition.item.ItemCondition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class ApoliRegistryKeys {
    public static final ResourceKey<Registry<MapCodec<? extends EntityCondition>>> ENTITY_CONDITION = key("entity_condition");
    public static final ResourceKey<Registry<MapCodec<? extends BiEntityCondition>>> BIENTITY_CONDITION = key("bientity_condition");
    public static final ResourceKey<Registry<MapCodec<? extends ItemCondition>>> ITEM_CONDITION = key("item_condition");
    public static final ResourceKey<Registry<MapCodec<? extends BlockCondition>>> BLOCK_CONDITION = key("block_condition");
    public static final ResourceKey<Registry<MapCodec<? extends DamageCondition>>> DAMAGE_CONDITION = key("damage_condition");
    public static final ResourceKey<Registry<MapCodec<? extends FluidCondition>>> FLUID_CONDITION = key("fluid_condition");
    public static final ResourceKey<Registry<MapCodec<? extends BiomeCondition>>> BIOME_CONDITION = key("biome_condition");

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(Apoli.identifier(name));
    }
}
