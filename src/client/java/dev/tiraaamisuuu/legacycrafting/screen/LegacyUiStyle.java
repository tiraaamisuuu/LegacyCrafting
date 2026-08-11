package dev.tiraaamisuuu.legacycrafting.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

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
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LegacySprites.SMALL_PANEL, x, y, width, height);
    }

    static void insetPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LegacySprites.SQUARE_RECESSED_PANEL, x, y, width, height);
    }

    static void slot(GuiGraphicsExtractor graphics, int x, int y, int size, boolean selected, boolean missing) {
        float cornerOffset = size < 18 ? -1.0F : -size / 20.0F;
        graphics.pose().pushMatrix();
        graphics.pose().translate(cornerOffset, cornerOffset);
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            missing ? LegacySprites.RED_ICON_HOLDER : LegacySprites.ICON_HOLDER,
            x,
            y,
            size,
            size
        );
        graphics.pose().popMatrix();
        if (selected) {
            int highlightSize = 36;
            float offset = cornerOffset + (size - highlightSize) / 2.0F;
            graphics.pose().pushMatrix();
            graphics.pose().translate(offset, offset);
            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                LegacySprites.SELECT_ICON_HIGHLIGHT,
                x,
                y,
                highlightSize,
                highlightSize
            );
            graphics.pose().popMatrix();
        }
    }

    static void recipeSelection(GuiGraphicsExtractor graphics, int x, int y, int variantCount) {
        boolean twoSlots = variantCount == 2;
        graphics.pose().pushMatrix();
        graphics.pose().translate(-27.0F / 20.0F - 4.5F, -27.0F / 20.0F + (twoSlots ? -12.0F : -39.0F));
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            twoSlots ? LegacySprites.CRAFTING_TWO_SLOT_SELECTION : LegacySprites.CRAFTING_SELECTION,
            x,
            y,
            36,
            twoSlots ? 78 : 105
        );
        graphics.pose().popMatrix();
    }

    static void arrow(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LegacySprites.SMALL_ARROW, x, y, 16, 13);
    }

    static void warning(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LegacySprites.ICON_WARNING, x, y, 8, 8);
    }
}
