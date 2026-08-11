package dev.tiraaamisuuu.legacycrafting.recipe;

import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

public final class RecipeCategoryResolver {
    public LegacyCategory resolve(RecipeDisplayEntry entry, ItemStack output) {
        if (entry.category() == RecipeBookCategories.CRAFTING_REDSTONE) {
            return LegacyCategory.REDSTONE;
        }

        String path = BuiltInRegistries.ITEM.getKey(output.getItem()).getPath().toLowerCase(Locale.ROOT);
        if (output.has(DataComponents.FOOD) || output.has(DataComponents.CONSUMABLE)) {
            return LegacyCategory.FOOD;
        }
        if (containsAny(path, "boat", "minecart", "rail", "saddle", "elytra")) {
            return LegacyCategory.TRANSPORTATION;
        }
        if (containsAny(path, "helmet", "chestplate", "leggings", "boots")) {
            return LegacyCategory.ARMOR;
        }
        if (output.has(DataComponents.WEAPON)
            || output.has(DataComponents.PIERCING_WEAPON)
            || output.has(DataComponents.KINETIC_WEAPON)
            || output.has(DataComponents.TOOL)
            || containsAny(
                path,
                "sword", "bow", "arrow", "shield", "mace", "trident", "pickaxe", "axe", "shovel", "hoe", "shears",
                "fishing_rod", "flint_and_steel", "brush", "clock", "compass"
            )) {
            return LegacyCategory.TOOLS;
        }
        if (containsAny(
            path,
            "banner", "bed", "candle", "carpet", "flower", "painting", "item_frame", "lantern", "pot", "sign", "torch", "skull", "head", "bookshelf"
        )) {
            return LegacyCategory.DECORATIONS;
        }
        if (entry.category() == RecipeBookCategories.CRAFTING_BUILDING_BLOCKS) {
            return LegacyCategory.BUILDING;
        }
        if (entry.category() == RecipeBookCategories.CRAFTING_EQUIPMENT) {
            return LegacyCategory.TOOLS;
        }
        return LegacyCategory.DECORATIONS;
    }

    private static boolean containsAny(String value, String... terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
