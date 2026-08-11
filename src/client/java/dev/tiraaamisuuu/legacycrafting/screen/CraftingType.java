package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

enum CraftingType {
    CRAFTING("crafting", Items.CRAFTING_TABLE),
    BANNER("banner", Items.BANNER.white()),
    FIREWORK("firework", Items.FIREWORK_ROCKET),
    DYEING("dyeing", Items.DYE.cyan());

    private final String translationSuffix;
    private final Item icon;

    CraftingType(String translationSuffix, Item icon) {
        this.translationSuffix = translationSuffix;
        this.icon = icon;
    }

    ItemStack icon() {
        return new ItemStack(this.icon);
    }

    Component title() {
        return Component.translatable("legacycrafting.type." + this.translationSuffix);
    }

    boolean accepts(RecipeView recipe) {
        if (this == CRAFTING) {
            return true;
        }
        String path = BuiltInRegistries.ITEM.getKey(recipe.recipe().output().getItem()).getPath().toLowerCase(Locale.ROOT);
        return switch (this) {
            case BANNER -> path.contains("banner") || path.contains("shield");
            case FIREWORK -> path.contains("firework");
            case DYEING -> path.contains("dye") || path.contains("stained") || path.contains("terracotta")
                || path.contains("concrete") || path.contains("wool") || path.contains("carpet")
                || path.contains("candle") || path.contains("bed");
            default -> true;
        };
    }
}
