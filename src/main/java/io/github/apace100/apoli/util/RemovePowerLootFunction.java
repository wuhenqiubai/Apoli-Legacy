package io.github.apace100.apoli.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RemovePowerLootFunction extends LootItemConditionalFunction {

    public static final LootItemFunctionType TYPE = new LootItemFunctionType(new io.github.apace100.apoli.util.RemovePowerLootFunction.Serializer());

    private final EquipmentSlot slot;
    private final ResourceLocation powerId;

    private RemovePowerLootFunction(LootItemCondition[] conditions, EquipmentSlot slot, ResourceLocation powerId) {
        super(conditions);
        this.slot = slot;
        this.powerId = powerId;
    }

    public LootItemFunctionType getType() {
        return TYPE;
    }

    public ItemStack run(ItemStack stack, LootContext context) {
        StackPowerUtil.removePower(stack, slot, powerId);
        return stack;
    }

    public static net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder<?> builder(EquipmentSlot slot, ResourceLocation powerId) {
        return simpleBuilder((conditions) -> new RemovePowerLootFunction(conditions, slot, powerId));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RemovePowerLootFunction> {
        public void toJson(JsonObject jsonObject, RemovePowerLootFunction addPowerLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, addPowerLootFunction, jsonSerializationContext);
            jsonObject.addProperty("slot", addPowerLootFunction.slot.getName());
            jsonObject.addProperty("power", addPowerLootFunction.powerId.toString());
        }

        public RemovePowerLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
            EquipmentSlot slot = SerializableDataTypes.EQUIPMENT_SLOT.read(jsonObject.get("slot"));
            ResourceLocation powerId = SerializableDataTypes.IDENTIFIER.read(jsonObject.get("power"));
            return new RemovePowerLootFunction(lootConditions, slot, powerId);
        }
    }
}
