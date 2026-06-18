package io.github.apace100.apoli.power.factory.condition;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.Apoli;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public class DistanceFromCoordinatesConditionRegistry {
    /**
     * Returns an array of aliases for the condition.
     * */
    private static String[] getAliases(){
        return new String[]{"distance_from_spawn", "distance_from_coordinates"};
    }

    public static void registerBlockCondition(BiConsumer<Identifier, MapCodec<DistanceFromCoordinatesCondition.BlockCond>> registryFunction) {
        for (String alias : getAliases())
            registryFunction.accept(Apoli.identifier(alias),
                alias.equals("distance_from_coordinates") ? DistanceFromCoordinatesCondition.BlockCond.ORIGIN_CODEC : DistanceFromCoordinatesCondition.BlockCond.SPAWN_CODEC
            );
    }

    public static void registerEntityCondition(BiConsumer<Identifier, MapCodec<DistanceFromCoordinatesCondition.EntityCond>> registryFunction) {
        for (String alias : getAliases())
            registryFunction.accept(Apoli.identifier(alias),
                alias.equals("distance_from_coordinates") ? DistanceFromCoordinatesCondition.EntityCond.ORIGIN_CODEC : DistanceFromCoordinatesCondition.EntityCond.SPAWN_CODEC
            );
    }
}
