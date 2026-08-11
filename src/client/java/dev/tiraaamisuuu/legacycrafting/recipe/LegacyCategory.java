package dev.tiraaamisuuu.legacycrafting.recipe;

import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum LegacyCategory {
    BUILDING("building", Items.OAK_PLANKS),
    TOOLS("tools", Items.IRON_PICKAXE),
    FOOD("food", Items.APPLE),
    ARMOR("armor", Items.DIAMOND_CHESTPLATE),
    REDSTONE("redstone", Items.LEVER),
    TRANSPORTATION("transportation", Items.RAIL),
    DECORATIONS("decorations", Items.PAINTING);

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

    public static List<LegacyCategory> forGrid(int gridWidth) {
        return gridWidth == 2
            ? Arrays.stream(values()).filter(category -> category != ARMOR).toList()
            : List.of(values());
    }
}
