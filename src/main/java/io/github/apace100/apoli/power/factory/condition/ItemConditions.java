package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.item.EnchantmentCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class ItemConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, stack) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.ITEM_CONDITIONS),
            (data, stack) -> ((List<ConditionFactory<ItemStack>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(stack)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.ITEM_CONDITIONS),
            (data, stack) -> ((List<ConditionFactory<ItemStack>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(stack)
            )));
        register(new ConditionFactory<>(Apoli.identifier("food"), new SerializableData(),
            (data, stack) -> stack.isEdible()));
        register(new ConditionFactory<>(Apoli.identifier("ingredient"), new SerializableData()
            .add("ingredient", SerializableDataTypes.INGREDIENT),
            (data, stack) -> ((Ingredient)data.get("ingredient")).test(stack)));
        register(new ConditionFactory<>(Apoli.identifier("armor_value"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> {
                int armor = 0;
                if(stack.getItem() instanceof ArmorItem) {
                    ArmorItem item = (ArmorItem)stack.getItem();
                    armor = item.getDefense();
                }
                return ((Comparison)data.get("comparison")).compare(armor, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("harvest_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> {
                int harvestLevel = 0;
                if(stack.getItem() instanceof TieredItem) {
                    TieredItem item = (TieredItem)stack.getItem();
                    harvestLevel = item.getTier().getLevel();
                }
                return ((Comparison)data.get("comparison")).compare(harvestLevel, data.getInt("compare_to"));
            }));
        register(EnchantmentCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("meat"), new SerializableData(),
            (data, stack) -> stack.isEdible() && stack.getItem().getFoodProperties().isMeat()));
        register(new ConditionFactory<>(Apoli.identifier("nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.NBT), (data, stack) -> NbtUtils.compareNbt(data.get("nbt"), stack.getTag(), true)));
        register(new ConditionFactory<>(Apoli.identifier("fireproof"), new SerializableData(),
            (data, stack) -> stack.getItem().isFireResistant()));
        register(new ConditionFactory<>(Apoli.identifier("enchantable"), new SerializableData(),
            (data, stack) -> !stack.isEnchantable()));
        register(new ConditionFactory<>(Apoli.identifier("power_count"), new SerializableData()
            .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, null)
            .add("compare_to", SerializableDataTypes.INT)
            .add("comparison", ApoliDataTypes.COMPARISON),
            (data, stack) -> {
                int totalCount = 0;
                if(data.isPresent("slot")) {
                    totalCount = StackPowerUtil.getPowers(stack, data.get("slot")).size();
                } else {
                    for (EquipmentSlot slot :
                        EquipmentSlot.values()) {
                        totalCount += StackPowerUtil.getPowers(stack, slot).size();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(totalCount, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("has_power"), new SerializableData()
            .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, null)
            .add("power", SerializableDataTypes.IDENTIFIER),
            (data, stack) -> {
                ResourceLocation power = data.getId("power");
                if(data.isPresent("slot")) {
                    return StackPowerUtil.getPowers(stack, data.get("slot")).stream().anyMatch(p -> p.powerId.equals(power));
                } else {
                    for (EquipmentSlot slot :
                        EquipmentSlot.values()) {
                        if(StackPowerUtil.getPowers(stack, slot).stream().anyMatch(p -> p.powerId.equals(power))) {
                            return true;
                        }
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("empty"), new SerializableData(),
            (data, stack) -> stack.isEmpty()));
        register(new ConditionFactory<>(Apoli.identifier("amount"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare(stack.getCount(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("is_damageable"), new SerializableData(),
            (data, stack) -> stack.isDamageableItem()));
        register(new ConditionFactory<>(Apoli.identifier("durability"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare(stack.getMaxDamage() - stack.getDamageValue(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("relative_durability"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare((float)(stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("is_equippable"), new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT),
            (data, stack) -> Mob.getEquipmentSlotForItem(stack) == data.get("equipment_slot")));
    }

    private static void register(ConditionFactory<ItemStack> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
