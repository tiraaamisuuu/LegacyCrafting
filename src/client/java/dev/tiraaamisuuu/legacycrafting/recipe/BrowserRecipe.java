package dev.tiraaamisuuu.legacycrafting.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public record BrowserRecipe(
    RecipeDisplayEntry entry,
    ItemStack output,
    int recipeWidth,
    int recipeHeight,
    List<IngredientSlot> ingredientSlots
) {
    public record IngredientSlot(int x, int y, SlotDisplay display, @Nullable Ingredient ingredient) {
    }
}

