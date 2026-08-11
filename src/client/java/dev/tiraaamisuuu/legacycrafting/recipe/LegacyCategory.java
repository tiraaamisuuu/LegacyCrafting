package dev.tiraaamisuuu.legacycrafting.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum LegacyCategory {
    BUILDING("building", Items.BRICKS),
    DECORATIONS("decorations", Items.FLOWER_POT),
    REDSTONE("redstone", Items.REDSTONE),
    TRANSPORTATION("transportation", Items.MINECART),
    TOOLS("tools", Items.IRON_PICKAXE),
    COMBAT("combat", Items.IRON_SWORD),
    FOOD("food", Items.APPLE),
    MISCELLANEOUS("miscellaneous", Items.CHEST);

    private final String translationSuffix;
    private final Item icon;

    LegacyCategory(String translationSuffix, Item icon) {
        this.translationSuffix = translationSuffix;
        this.icon = icon;
    }

    public Component title() {
        return Component.translatable("legacycrafting.category." + this.translationSuffix);
    }

    public ItemStack icon() {
        return new ItemStack(this.icon);
    }
}

