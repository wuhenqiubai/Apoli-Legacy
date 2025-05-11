package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;

public class SetEntityGroupPower extends Power {

    public final MobType group;

    public SetEntityGroupPower(PowerType<?> type, LivingEntity entity, MobType group) {
        super(type, entity);
        this.group = group;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("entity_group"),
            new SerializableData()
                .add("group", SerializableDataTypes.ENTITY_GROUP),
            data ->
                (type, player) -> new SetEntityGroupPower(type, player, (MobType)data.get("group")))
            .allowCondition();
    }
}
