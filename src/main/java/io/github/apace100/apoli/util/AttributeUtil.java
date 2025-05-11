package io.github.apace100.apoli.util;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Comparator;
import java.util.List;

public final class AttributeUtil {

    public static void sortModifiers(List<AttributeModifier> modifiers) {
        modifiers.sort(Comparator.comparing(e -> e.getOperation().toValue()));
    }

    public static double sortAndApplyModifiers(List<AttributeModifier> modifiers, double baseValue) {
        sortModifiers(modifiers);
        return applyModifiers(modifiers, baseValue);
    }

    public static double applyModifiers(List<AttributeModifier> modifiers, double baseValue) {
        double currentValue = baseValue;
        if(modifiers != null) {
            for(AttributeModifier modifier : modifiers) {
                switch(modifier.getOperation()) {
                    case ADDITION:
                        currentValue += modifier.getAmount();
                        break;
                    case MULTIPLY_BASE:
                        currentValue += baseValue * modifier.getAmount();
                        break;
                    case MULTIPLY_TOTAL:
                        currentValue *= (1 + modifier.getAmount());
                        break;
                }
            }
        }
        return currentValue;
    }
}
