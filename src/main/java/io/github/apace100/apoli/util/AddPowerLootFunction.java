package io.github.apace100.apoli.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AddPowerLootFunction extends LootItemConditionalFunction {

    public static final LootItemFunctionType TYPE = new LootItemFunctionType(new AddPowerLootFunction.Serializer());

    private final EquipmentSlot slot;
    private final ResourceLocation powerId;
    private final boolean hidden;
    private final boolean negative;

    private AddPowerLootFunction(LootItemCondition[] conditions, EquipmentSlot slot, ResourceLocation powerId, boolean hidden, boolean negative) {
        super(conditions);
        this.slot = slot;
        this.powerId = powerId;
        this.hidden = hidden;
        this.negative = negative;
    }

    public LootItemFunctionType getType() {
        return TYPE;
    }

    public ItemStack run(ItemStack stack, LootContext context) {
        StackPowerUtil.addPower(stack, slot, powerId, hidden, negative);
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder(EquipmentSlot slot, ResourceLocation powerId, boolean hidden, boolean negative) {
        return simpleBuilder((conditions) -> new AddPowerLootFunction(conditions, slot, powerId, hidden, negative));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AddPowerLootFunction> {
        public void toJson(JsonObject jsonObject, AddPowerLootFunction addPowerLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, addPowerLootFunction, jsonSerializationContext);
            jsonObject.addProperty("slot", addPowerLootFunction.slot.getName());
            jsonObject.addProperty("power", addPowerLootFunction.powerId.toString());
        }

        public AddPowerLootFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootConditions) {
            EquipmentSlot slot = SerializableDataTypes.EQUIPMENT_SLOT.read(jsonObject.get("slot"));
            ResourceLocation powerId = SerializableDataTypes.IDENTIFIER.read(jsonObject.get("power"));
            boolean hidden = GsonHelper.getAsBoolean(jsonObject, "hidden", false);
            boolean negative = GsonHelper.getAsBoolean(jsonObject, "negative", false);
            return new AddPowerLootFunction(lootConditions, slot, powerId, hidden, negative);
        }
    }
}
