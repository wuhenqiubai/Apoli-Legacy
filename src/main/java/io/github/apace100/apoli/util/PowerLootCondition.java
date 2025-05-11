package io.github.apace100.apoli.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Optional;

public class PowerLootCondition implements LootItemCondition {

    public static final LootItemConditionType TYPE = new LootItemConditionType(new PowerLootCondition.Serializer());

    private final ResourceLocation powerId;
    private final ResourceLocation powerSourceId;

    private PowerLootCondition(ResourceLocation powerId) {
        this.powerId = powerId;
        this.powerSourceId = null;
    }

    private PowerLootCondition(ResourceLocation powerId, ResourceLocation powerSourceId) {
        this.powerId = powerId;
        this.powerSourceId = powerSourceId;
    }

    public LootItemConditionType getType() {
        return TYPE;
    }

    public boolean test(LootContext lootContext) {

        Optional<PowerHolderComponent> optionalPowerHolderComponent = PowerHolderComponent.KEY.maybeGet(
            lootContext.getParamOrNull(LootContextParams.THIS_ENTITY)
        );

        if (optionalPowerHolderComponent.isPresent()) {

            PowerHolderComponent powerHolderComponent = optionalPowerHolderComponent.get();
            PowerType<?> powerType = PowerTypeRegistry.get(powerId);

            if (powerSourceId != null) return powerHolderComponent.hasPower(powerType, powerSourceId);
            else return powerHolderComponent.hasPower(powerType);

        }

        return false;

    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<PowerLootCondition> {
        @Override
        public void serialize(JsonObject jsonObject, PowerLootCondition powerLootCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("power", powerLootCondition.powerId.toString());
            if (powerLootCondition.powerSourceId != null) jsonObject.addProperty("source", powerLootCondition.powerSourceId.toString());
        }

        @Override
        public PowerLootCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ResourceLocation power = new ResourceLocation(GsonHelper.getAsString(jsonObject, "power"));
            if (jsonObject.has("source")) {
                ResourceLocation source = new ResourceLocation(GsonHelper.getAsString(jsonObject, "source"));
                return new PowerLootCondition(power, source);
            }
            return new PowerLootCondition(power);
        }

    }

}
