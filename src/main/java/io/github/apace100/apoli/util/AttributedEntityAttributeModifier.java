package io.github.apace100.apoli.util;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributedEntityAttributeModifier {

    private final Attribute attribute;
    private final AttributeModifier modifier;

    public AttributedEntityAttributeModifier(Attribute attribute, AttributeModifier modifier) {
        this.attribute = attribute;
        this.modifier = modifier;
    }

    public AttributeModifier getModifier() {
        return modifier;
    }

    public Attribute getAttribute() {
        return attribute;
    }
}
