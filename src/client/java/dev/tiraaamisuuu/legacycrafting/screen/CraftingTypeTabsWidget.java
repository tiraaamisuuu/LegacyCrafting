package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tiraaamisuuu.legacycrafting.client.LegacyUiSounds;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;

final class CraftingTypeTabsWidget extends AbstractWidget {
    static final int TAB_SIZE = 42;
    private final Consumer<CraftingType> onChanged;
    private CraftingType selected = CraftingType.CRAFTING;
    private int hoveredIndex = -1;

    CraftingTypeTabsWidget(int x, int y, Consumer<CraftingType> onChanged) {
        super(x, y, TAB_SIZE, TAB_SIZE * CraftingType.values().length, CommonComponents.EMPTY);
        this.onChanged = onChanged;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredIndex = -1;
        CraftingType[] types = CraftingType.values();
        for (int index = 0; index < types.length; index++) {
            CraftingType type = types[index];
            boolean selected = type == this.selected;
            int tabX = this.getX() + (selected ? 0 : 3);
            int tabY = this.getY() + index * TAB_SIZE;
            if (mouseX >= tabX && mouseX < tabX + TAB_SIZE && mouseY >= tabY && mouseY < tabY + TAB_SIZE) {
                this.hoveredIndex = index;
            }
            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                selected ? selectedSprite(index, types.length) : LegacySprites.LOW_VERT_TAB,
                tabX,
                tabY,
                TAB_SIZE,
                TAB_SIZE
            );
            graphics.pose().pushMatrix();
            graphics.pose().translate(tabX + 9, tabY + 9);
            graphics.pose().scale(1.5F, 1.5F);
            graphics.item(type.icon(), 0, 0, index);
            graphics.pose().popMatrix();
        }

        if (this.hoveredIndex >= 0) {
            graphics.setTooltipForNextFrame(
                Minecraft.getInstance().font,
                LegacyText.component(types[this.hoveredIndex].title()),
                mouseX,
                mouseY
            );
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int index = ((int)event.y() - this.getY()) / TAB_SIZE;
        if (index >= 0 && index < CraftingType.values().length) {
            this.select(CraftingType.values()[index]);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int delta = switch (event.key()) {
            case InputConstants.KEY_UP -> -1;
            case InputConstants.KEY_DOWN -> 1;
            default -> 0;
        };
        if (delta == 0) {
            return false;
        }
        CraftingType[] types = CraftingType.values();
        this.select(types[Math.floorMod(this.selected.ordinal() + delta, types.length)]);
        return true;
    }

    private void select(CraftingType type) {
        if (type != this.selected) {
            this.selected = type;
            this.onChanged.accept(type);
            LegacyUiSounds.play(LegacyUiSounds.Cue.FOCUS);
        }
    }

    private static net.minecraft.resources.Identifier selectedSprite(int index, int size) {
        if (index == 0) {
            return LegacySprites.HIGH_VERT_TAB_DOWN;
        }
        if (index == size - 1) {
            return LegacySprites.HIGH_VERT_TAB_UP;
        }
        return LegacySprites.HIGH_VERT_TAB_MIDDLE;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.selected.title());
    }
}
