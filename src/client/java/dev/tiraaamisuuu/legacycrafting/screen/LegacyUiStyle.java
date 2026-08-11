package dev.tiraaamisuuu.legacycrafting.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;

final class LegacyUiStyle {
    static final int PANEL = 0xFFD0D0D0;
    static final int PANEL_LIGHT = 0xFFE1E1E1;
    static final int PANEL_DARK = 0xFFA9A9A9;
    static final int SLOT = 0xFFB8B8B8;
    static final int TEXT = 0xFF353535;
    static final int MUTED_TEXT = 0xFF666666;
    static final int HIGHLIGHT = 0xFFF4F4F4;
    static final int SHADOW = 0xFF555555;
    static final int DEEP_SHADOW = 0xAA111111;
    static final int SELECTED = 0xFF45634C;
    static final int MISSING = 0xFFD24B4B;

    private LegacyUiStyle() {
    }

    static void raisedPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x + 3, y + 3, x + width + 3, y + height + 3, DEEP_SHADOW);
        graphics.fill(x, y, x + width, y + height, color);
        graphics.fill(x, y, x + width, y + 2, HIGHLIGHT);
        graphics.fill(x, y, x + 2, y + height, HIGHLIGHT);
        graphics.fill(x, y + height - 2, x + width, y + height, SHADOW);
        graphics.fill(x + width - 2, y, x + width, y + height, SHADOW);
    }

    static void insetPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
        graphics.fill(x, y, x + width, y + 1, SHADOW);
        graphics.fill(x, y, x + 1, y + height, SHADOW);
        graphics.fill(x, y + height - 1, x + width, y + height, HIGHLIGHT);
        graphics.fill(x + width - 1, y, x + width, y + height, HIGHLIGHT);
    }

    static void slot(GuiGraphicsExtractor graphics, int x, int y, int size, boolean selected, boolean missing) {
        int color = missing ? MISSING : SLOT;
        graphics.fill(x, y, x + size, y + size, color);
        graphics.fill(x, y, x + size, y + 1, SHADOW);
        graphics.fill(x, y, x + 1, y + size, SHADOW);
        graphics.fill(x, y + size - 1, x + size, y + size, HIGHLIGHT);
        graphics.fill(x + size - 1, y, x + size, y + size, HIGHLIGHT);
        if (selected) {
            graphics.outline(x - 1, y - 1, size + 2, size + 2, SELECTED);
            graphics.outline(x - 2, y - 2, size + 4, size + 4, 0xFF263C2C);
        }
    }

    static void arrow(GuiGraphicsExtractor graphics, int x, int y) {
        int color = 0xFF858585;
        graphics.fill(x, y + 5, x + 10, y + 9, color);
        graphics.fill(x + 8, y + 2, x + 12, y + 12, color);
        graphics.fill(x + 10, y + 4, x + 14, y + 10, color);
        graphics.fill(x + 12, y + 6, x + 16, y + 8, color);
        graphics.horizontalLine(x, x + 8, y + 5, HIGHLIGHT);
        graphics.horizontalLine(x, x + 8, y + 8, SHADOW);
    }
}
