package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
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
    private static final int TAB_SIZE = 25;
    private final Consumer<LegacyCategory> onSelected;
    private LegacyCategory selected = LegacyCategory.BUILDING;
    private int hoveredIndex = -1;

    public CategoryTabsWidget(int x, int y, Consumer<LegacyCategory> onSelected) {
        super(x, y, LegacyCategory.values().length * TAB_SIZE, 23, CommonComponents.EMPTY);
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
            int tabX = this.getX() + index * TAB_SIZE;
            boolean hovered = mouseX >= tabX && mouseX < tabX + TAB_SIZE && mouseY >= this.getY() && mouseY < this.getBottom();
            if (hovered) {
                this.hoveredIndex = index;
            }
            int background = categories[index] == this.selected ? 0xFFE7B85A : hovered ? 0xFF505A70 : 0xFF252A35;
            int border = categories[index] == this.selected ? 0xFFFFE09A : 0xFF596276;
            graphics.fill(tabX + 1, this.getY() + 1, tabX + TAB_SIZE - 1, this.getBottom() - 1, background);
            graphics.outline(tabX, this.getY(), TAB_SIZE, this.getHeight(), border);
            graphics.item(categories[index].icon(), tabX + 5, this.getY() + 3, index);
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
        int index = ((int)event.x() - this.getX()) / TAB_SIZE;
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
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.selected.title());
        output.add(NarratedElementType.USAGE, net.minecraft.network.chat.Component.translatable("legacycrafting.category.navigation"));
    }
}

