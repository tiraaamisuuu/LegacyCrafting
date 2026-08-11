package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.client.LegacyCraftingClient;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class LegacyControlHint {
    private static final float ICON_SCALE = 2.0F / 9.0F;

    private LegacyControlHint() {
    }

    static int draw(GuiGraphicsExtractor graphics, Font font, int x, int y, Button button, Component label) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y - 1);
        graphics.pose().scale(ICON_SCALE, ICON_SCALE);
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            button.texture,
            0,
            0,
            0.0F,
            0.0F,
            button.textureWidth,
            button.textureHeight,
            button.textureWidth,
            button.textureHeight
        );
        graphics.pose().popMatrix();

        int iconWidth = Math.round(button.textureWidth * ICON_SCALE);
        graphics.text(font, label, x + iconWidth + 2, y, 0xFFE8E8E8, true);
        return iconWidth + 2 + font.width(label);
    }

    enum Button {
        A("a", 48, 45),
        B("b", 48, 45),
        X("x", 48, 45),
        Y("y", 48, 45),
        LEFT_BUMPER("lb", 49, 45);

        private final Identifier texture;
        private final int textureWidth;
        private final int textureHeight;

        Button(String path, int textureWidth, int textureHeight) {
            this.texture = Identifier.fromNamespaceAndPath(
                LegacyCraftingClient.MOD_ID,
                "textures/gui/controller/" + path + ".png"
            );
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }
    }
}
