package dev.tiraaamisuuu.legacycrafting.recipe;

import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe.IngredientSlot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public final class RecipeBrowser {
    private final RecipeCategoryResolver categoryResolver = new RecipeCategoryResolver();

    public List<BrowserRecipe> loadKnownRecipes(LocalPlayer player, AbstractCraftingMenu menu) {
        ContextMap context = SlotDisplayContext.fromLevel(player.level());
        Map<Integer, RecipeDisplayEntry> knownEntries = new LinkedHashMap<>();

        player.getRecipeBook().getCollections().forEach(collection -> collection.getRecipes().forEach(entry ->
            knownEntries.putIfAbsent(entry.id().index(), entry)
        ));

        return knownEntries.values().stream()
            .filter(entry -> fitsGrid(entry.display(), menu.getGridWidth(), menu.getGridHeight()))
            .map(entry -> createBrowserRecipe(entry, context))
            .filter(recipe -> !recipe.output().isEmpty())
            .sorted(Comparator.comparing(recipe -> recipe.output().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private static boolean fitsGrid(RecipeDisplay display, int gridWidth, int gridHeight) {
        return switch (display) {
            case ShapedCraftingRecipeDisplay shaped -> shaped.width() <= gridWidth && shaped.height() <= gridHeight;
            case ShapelessCraftingRecipeDisplay shapeless -> shapeless.ingredients().size() <= gridWidth * gridHeight;
            default -> false;
        };
    }

    private BrowserRecipe createBrowserRecipe(RecipeDisplayEntry entry, ContextMap context) {
        ItemStack output = entry.resultItems(context).stream().findFirst().orElse(ItemStack.EMPTY).copy();
        List<Ingredient> requirements = entry.craftingRequirements().orElse(List.of());
        List<IngredientSlot> ingredientSlots = new ArrayList<>();

        switch (entry.display()) {
            case ShapedCraftingRecipeDisplay shaped -> {
                int requirementIndex = 0;
                for (int index = 0; index < shaped.ingredients().size(); index++) {
                    SlotDisplay display = shaped.ingredients().get(index);
                    if (display instanceof SlotDisplay.Empty) {
                        continue;
                    }
                    Ingredient ingredient = requirementIndex < requirements.size() ? requirements.get(requirementIndex++) : null;
                    ingredientSlots.add(new IngredientSlot(index % shaped.width(), index / shaped.width(), display, ingredient));
                }
                return new BrowserRecipe(
                    entry, output, this.categoryResolver.resolve(entry, output), shaped.width(), shaped.height(), List.copyOf(ingredientSlots)
                );
            }
            case ShapelessCraftingRecipeDisplay shapeless -> {
                for (int index = 0; index < shapeless.ingredients().size(); index++) {
                    Ingredient ingredient = index < requirements.size() ? requirements.get(index) : null;
                    ingredientSlots.add(new IngredientSlot(index % 3, index / 3, shapeless.ingredients().get(index), ingredient));
                }
                int width = Math.min(3, Math.max(1, shapeless.ingredients().size()));
                int height = Math.max(1, (shapeless.ingredients().size() + width - 1) / width);
                return new BrowserRecipe(entry, output, this.categoryResolver.resolve(entry, output), width, height, List.copyOf(ingredientSlots));
            }
            default -> throw new IllegalArgumentException("Unsupported crafting display: " + entry.display().getClass().getName());
        }
    }
}
