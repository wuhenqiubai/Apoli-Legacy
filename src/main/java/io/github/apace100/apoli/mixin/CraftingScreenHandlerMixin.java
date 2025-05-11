package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CraftingMenu.class)
public class CraftingScreenHandlerMixin {

    @Shadow @Final private ContainerLevelAccess access;

    @Shadow @Final private CraftingContainer craftSlots;

    @Inject(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private static void clearPowerCraftingInventory(AbstractContainerMenu handler, Level world, Player player, CraftingContainer inventory, ResultContainer resultInventory, CallbackInfo ci) {
        if (inventory instanceof TransientCraftingContainer craftingInventory) ((PowerCraftingInventory)craftingInventory).setPower(null);
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void allowUsingViaPower(Player player, CallbackInfoReturnable<Boolean> cir) {
        if(access.evaluate((world, pos) -> pos.equals(player.blockPosition()), false)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyOutputItems(Player player, int index, CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack, Slot slot, ItemStack itemStack2) {
        if(craftSlots instanceof PowerCraftingInventory pci) {
            if(pci.getPower() instanceof ModifyCraftingPower mcp) {
                mcp.applyAfterCraftingItemAction(itemStack2);
            }
        }
    }
}
