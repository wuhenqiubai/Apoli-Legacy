package io.github.apace100.apoli.util;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.mixin.PlayerScreenHandlerAccessor;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class ModifiedCraftingRecipe extends CustomRecipe {

    public static final RecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<>(ModifiedCraftingRecipe::new);

    public ModifiedCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world)
    {
        if (inventory instanceof TransientCraftingContainer craftingInventory)
        {
            Optional<CraftingRecipe> original = getOriginalMatch(craftingInventory);
            if (original.isEmpty())
            {
                return false;
            }
            return getRecipes(craftingInventory).stream().anyMatch(r -> r.doesApply(craftingInventory, original.get()));
        }

        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess registryManager)
    {
        if (inventory instanceof TransientCraftingContainer craftingInventory)
        {
            Player player = getPlayerFromInventory(craftingInventory);
            if (player != null)
            {
                Optional<CraftingRecipe> original = getOriginalMatch(craftingInventory);
                if (original.isPresent())
                {
                    Optional<ModifyCraftingPower> optional = getRecipes(craftingInventory).stream().filter(r -> r.doesApply(craftingInventory, original.get())).findFirst();
                    if (optional.isPresent())
                    {
                        ItemStack result = optional.get().getNewResult(craftingInventory, original.get());
                        ((PowerCraftingInventory) craftingInventory).setPower(optional.get());
                        return result;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static Player getPlayerFromInventory(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        return getPlayerFromHandler(handler);
    }

    public static Optional<BlockPos> getBlockFromInventory(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        if(handler instanceof CraftingMenu) {
            return ((CraftingScreenHandlerAccessor)handler).getAccess().evaluate((world, blockPos) -> blockPos);
        }
        return Optional.empty();
    }

    private List<ModifyCraftingPower> getRecipes(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        Player player = getPlayerFromHandler(handler);
        if(player != null) {
            return PowerHolderComponent.getPowers(player, ModifyCraftingPower.class);
        }
        return Lists.newArrayList();
    }

    private Optional<CraftingRecipe> getOriginalMatch(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        Player player = getPlayerFromHandler(handler);
        if(player != null && player.getServer() != null) {
            List<CraftingRecipe> recipes = player.getServer().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING);
            return recipes.stream()
                .filter(cr -> !(cr instanceof ModifiedCraftingRecipe)
                    && cr.matches(inv, player.level()))
                .findFirst();
        }
        return Optional.empty();
    }

    private static Player getPlayerFromHandler(AbstractContainerMenu screenHandler) {
        if(screenHandler instanceof CraftingMenu) {
            return ((CraftingScreenHandlerAccessor)screenHandler).getPlayer();
        }
        if(screenHandler instanceof InventoryMenu) {
            return ((PlayerScreenHandlerAccessor)screenHandler).getOwner();
        }
        return null;
    }
}
