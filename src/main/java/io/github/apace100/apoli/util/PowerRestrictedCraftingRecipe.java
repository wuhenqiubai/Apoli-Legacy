package io.github.apace100.apoli.util;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.mixin.PlayerScreenHandlerAccessor;
import io.github.apace100.apoli.power.RecipePower;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PowerRestrictedCraftingRecipe extends CustomRecipe {

    public static final RecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<PowerRestrictedCraftingRecipe>(PowerRestrictedCraftingRecipe::new);

    public PowerRestrictedCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world) {
        if (inventory instanceof TransientCraftingContainer craftingInventory)
        {
            return getRecipes(craftingInventory).stream().anyMatch(r -> r.matches(craftingInventory, world));
        }

        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess registryManager) {
        if (inventory instanceof TransientCraftingContainer craftingInventory)
        {
            Player player = getPlayerFromInventory(craftingInventory);
            if (player != null)
            {
                Optional<Recipe<TransientCraftingContainer>> optional = getRecipes(craftingInventory).stream().filter(r -> r.matches(craftingInventory, player.level())).findFirst();
                if (optional.isPresent())
                {
                    Recipe<TransientCraftingContainer> recipe = optional.get();
                    return recipe.assemble(craftingInventory, registryManager);
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

    private Player getPlayerFromInventory(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        return getPlayerFromHandler(handler);
    }

    private List<Recipe<TransientCraftingContainer>> getRecipes(TransientCraftingContainer inv) {
        AbstractContainerMenu handler = ((CraftingInventoryAccessor)inv).getMenu();
        Player player = getPlayerFromHandler(handler);
        if(player != null) {
            return PowerHolderComponent.getPowers(player, RecipePower.class).stream().map(RecipePower::getRecipe).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    private Player getPlayerFromHandler(AbstractContainerMenu screenHandler) {
        if(screenHandler instanceof CraftingMenu) {
            return ((CraftingScreenHandlerAccessor)screenHandler).getPlayer();
        }
        if(screenHandler instanceof InventoryMenu) {
            return ((PlayerScreenHandlerAccessor)screenHandler).getOwner();
        }
        return null;
    }
}
