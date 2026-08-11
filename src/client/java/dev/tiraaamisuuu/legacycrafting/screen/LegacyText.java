package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.client.LegacyCraftingClient;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

final class LegacyText {
    private static final FontDescription FONT = new FontDescription.Resource(
        Identifier.fromNamespaceAndPath(LegacyCraftingClient.MOD_ID, "default_11")
    );

    private LegacyText() {
    }

    static Component component(Component text) {
        return text.copy().withStyle(style -> style.withFont(FONT));
    }

    static int width(Font font, Component text) {
        return font.width(component(text));
    }

    static void text(
        GuiGraphicsExtractor graphics,
        Font font,
        Component text,
        int x,
        int y,
        int color,
        boolean shadow
    ) {
        graphics.text(font, component(text), x, y, color, shadow);
    }

    static void centered(
        GuiGraphicsExtractor graphics,
        Font font,
        Component text,
        int centerX,
        int y,
        int color
    ) {
        FormattedCharSequence visualText = component(text).getVisualOrderText();
        graphics.text(font, visualText, centerX - font.width(visualText) / 2, y, color, false);
    }
}
