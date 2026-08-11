package dev.tiraaamisuuu.legacycrafting.recipe;

import java.util.List;
import net.minecraft.world.item.crafting.Ingredient;

public record RecipeView(BrowserRecipe recipe, boolean craftable, List<IngredientSummary> ingredientSummaries) {
    public record IngredientSummary(Ingredient ingredient, int required, int available) {
    }
}

