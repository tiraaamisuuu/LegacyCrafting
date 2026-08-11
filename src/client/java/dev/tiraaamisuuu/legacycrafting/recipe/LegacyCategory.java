package dev.tiraaamisuuu.legacycrafting.recipe;

import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public enum LegacyCategory {
    BUILDING("building", "structures"),
    TOOLS("tools", "tools"),
    FOOD("food", "food"),
    ARMOR("armor", "armour"),
    REDSTONE("redstone", "mechanisms"),
    TRANSPORTATION("transportation", "transport"),
    DECORATIONS("decorations", "decoration");

    private final String translationSuffix;
    private final String iconPath;

    LegacyCategory(String translationSuffix, String iconPath) {
        this.translationSuffix = translationSuffix;
        this.iconPath = iconPath;
    }

    public Component title() {
        return Component.translatable("legacycrafting.category." + this.translationSuffix);
    }

    public Identifier icon() {
        return Identifier.fromNamespaceAndPath("legacycrafting", "icon/" + this.iconPath);
    }

    public static List<LegacyCategory> forGrid(int gridWidth) {
        return gridWidth == 2
            ? Arrays.stream(values()).filter(category -> category != ARMOR).toList()
            : List.of(values());
    }
}
