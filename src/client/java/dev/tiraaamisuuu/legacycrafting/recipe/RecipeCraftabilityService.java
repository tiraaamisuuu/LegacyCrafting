package dev.tiraaamisuuu.legacycrafting.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class RecipeCraftabilityService {
    public List<RecipeView> evaluate(List<BrowserRecipe> recipes, LocalPlayer player, AbstractCraftingMenu menu) {
        StackedItemContents contents = new StackedItemContents();
        player.getInventory().fillStackedContents(contents);
        menu.fillCraftSlotsStackedContents(contents);
        List<ItemStack> availableStacks = snapshotAvailableStacks(player, menu);

        return recipes.stream()
            .map(recipe -> new RecipeView(recipe, recipe.entry().canCraft(contents), summarize(recipe, availableStacks)))
            .toList();
    }

    private static List<RecipeView.IngredientSummary> summarize(BrowserRecipe recipe, List<ItemStack> availableStacks) {
        Map<Ingredient, Integer> required = new LinkedHashMap<>();
        recipe.entry().craftingRequirements().orElse(List.of()).forEach(ingredient -> required.merge(ingredient, 1, Integer::sum));
        return required.entrySet().stream()
            .map(entry -> new RecipeView.IngredientSummary(
                entry.getKey(),
                entry.getValue(),
                availableStacks.stream().filter(entry.getKey()).mapToInt(ItemStack::getCount).sum()
            ))
            .toList();
    }

    private static List<ItemStack> snapshotAvailableStacks(LocalPlayer player, AbstractCraftingMenu menu) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (Inventory.isUsableForCrafting(stack)) {
                stacks.add(stack.copy());
            }
        }
        for (Slot slot : menu.getInputGridSlots()) {
            ItemStack stack = slot.getItem();
            if (Inventory.isUsableForCrafting(stack)) {
                stacks.add(stack.copy());
            }
        }
        return List.copyOf(stacks);
    }
}

