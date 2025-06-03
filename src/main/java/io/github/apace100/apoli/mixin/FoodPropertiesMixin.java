package io.github.apace100.apoli.mixin;

import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin {
    /*@WrapOperation(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void apoli$storeSharedStack(FoodData instance, FoodProperties foodProperties, Operation<Void> original, @Local(argsOnly = true) ItemStack stack) {
        ApoliSharedMixinValues.CURRENT_STACK.set(stack);
        original.call(instance, foodProperties);
        ApoliSharedMixinValues.CURRENT_STACK.remove();
    }*/
}
