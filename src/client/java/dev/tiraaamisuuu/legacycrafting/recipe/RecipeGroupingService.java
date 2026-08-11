package dev.tiraaamisuuu.legacycrafting.recipe;

import dev.tiraaamisuuu.legacycrafting.recipe.LegacyRecipeCatalog.Listing;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;

public final class RecipeGroupingService {
    private final LegacyRecipeCatalog catalog = LegacyRecipeCatalog.instance();

    public List<RecipeGroup> group(List<RecipeView> recipes) {
        List<RecipeView> ordered = new ArrayList<>(recipes);
        ordered.sort(Comparator
            .comparingInt(this::groupOrder)
            .thenComparingInt(this::variantOrder)
            .thenComparing(view -> view.recipe().output().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER));

        Map<String, List<RecipeView>> groups = new LinkedHashMap<>();
        for (RecipeView recipe : ordered) {
            groups.computeIfAbsent(this.groupKey(recipe), ignored -> new ArrayList<>()).add(recipe);
        }
        return groups.entrySet().stream().map(entry -> new RecipeGroup(entry.getKey(), entry.getValue())).toList();
    }

    private String groupKey(RecipeView view) {
        return this.listing(view).map(Listing::group).orElseGet(() -> view.recipe().entry().group().isPresent()
            ? view.recipe().category().name() + ":display_" + view.recipe().entry().group().getAsInt()
            : view.recipe().category().name() + ":recipe_" + view.recipe().entry().id().index());
    }

    private int groupOrder(RecipeView view) {
        return this.listing(view).map(Listing::groupOrder).orElse(10_000);
    }

    private int variantOrder(RecipeView view) {
        return this.listing(view).map(Listing::variantOrder).orElse(10_000);
    }

    private java.util.Optional<Listing> listing(RecipeView view) {
        String path = BuiltInRegistries.ITEM.getKey(view.recipe().output().getItem()).getPath();
        return this.catalog.find(path);
    }
}
