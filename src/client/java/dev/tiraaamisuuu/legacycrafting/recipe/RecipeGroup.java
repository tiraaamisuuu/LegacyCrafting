package dev.tiraaamisuuu.legacycrafting.recipe;

import java.util.List;

public record RecipeGroup(String key, List<RecipeView> variants) {
    public RecipeGroup {
        variants = List.copyOf(variants);
    }
}
