package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tiraaamisuuu.legacycrafting.client.LegacyUiSounds;
import dev.tiraaamisuuu.legacycrafting.recipe.LegacyCategory;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;

public final class CategoryTabsWidget extends AbstractWidget {
    private static final int TAB_WIDTH = 43;
    private static final int TAB_HEIGHT = 29;
    private final Consumer<LegacyCategory> onSelected;
    private LegacyCategory selected = LegacyCategory.BUILDING;
    private int hoveredIndex = -1;

    public CategoryTabsWidget(int x, int y, Consumer<LegacyCategory> onSelected) {
        super(x, y, LegacyCategory.values().length * TAB_WIDTH, TAB_HEIGHT, CommonComponents.EMPTY);
        this.onSelected = onSelected;
    }

    public LegacyCategory selected() {
        return this.selected;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredIndex = -1;
        LegacyCategory[] categories = LegacyCategory.values();
        for (int index = 0; index < categories.length; index++) {
            int tabX = this.getX() + index * TAB_WIDTH;
            boolean hovered = mouseX >= tabX && mouseX < tabX + TAB_WIDTH && mouseY >= this.getY() && mouseY < this.getBottom();
            if (hovered) {
                this.hoveredIndex = index;
            }
            boolean selected = categories[index] == this.selected;
            LegacyUiStyle.raisedPanel(
                graphics,
                tabX,
                this.getY(),
                TAB_WIDTH - 2,
                TAB_HEIGHT,
                selected ? LegacyUiStyle.PANEL_LIGHT : hovered ? 0xFFC6C6C6 : LegacyUiStyle.PANEL_DARK
            );
            graphics.pose().pushMatrix();
            graphics.pose().translate(tabX + 10, this.getY() + 5);
            graphics.pose().scale(1.25F, 1.25F);
            graphics.item(categories[index].icon(), 0, 0, index);
            graphics.pose().popMatrix();
            if (selected) {
                graphics.fill(tabX + 2, this.getBottom() - 3, tabX + TAB_WIDTH - 4, this.getBottom(), LegacyUiStyle.PANEL_LIGHT);
            }
        }
        if (this.hoveredIndex >= 0) {
            graphics.setTooltipForNextFrame(
                Minecraft.getInstance().font,
                categories[this.hoveredIndex].title(),
                mouseX,
                mouseY
            );
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int index = ((int)event.x() - this.getX()) / TAB_WIDTH;
        if (index >= 0 && index < LegacyCategory.values().length) {
            this.select(LegacyCategory.values()[index]);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int delta = switch (event.key()) {
            case InputConstants.KEY_LEFT -> -1;
            case InputConstants.KEY_RIGHT -> 1;
            default -> 0;
        };
        if (delta == 0) {
            return false;
        }
        LegacyCategory[] categories = LegacyCategory.values();
        int index = Math.floorMod(this.selected.ordinal() + delta, categories.length);
        this.select(categories[index]);
        return true;
    }

    private void select(LegacyCategory category) {
        if (category != this.selected) {
            this.selected = category;
            this.onSelected.accept(category);
            LegacyUiSounds.play(LegacyUiSounds.Cue.FOCUS);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.selected.title());
        output.add(NarratedElementType.USAGE, net.minecraft.network.chat.Component.translatable("legacycrafting.category.navigation"));
    }
}
