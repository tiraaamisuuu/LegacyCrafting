package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class RecipeFilterTabsWidget extends AbstractWidget {
    private static final int TAB_WIDTH = 37;
    private static final int TAB_HEIGHT = 38;
    private final Consumer<Boolean> onChanged;
    private boolean craftableOnly;
    private int hoveredIndex = -1;

    public RecipeFilterTabsWidget(int x, int y, Consumer<Boolean> onChanged) {
        super(x, y, TAB_WIDTH, TAB_HEIGHT * 2, CommonComponents.EMPTY);
        this.onChanged = onChanged;
    }

    public void toggle() {
        this.select(!this.craftableOnly);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredIndex = -1;
        for (int index = 0; index < 2; index++) {
            int tabY = this.getY() + index * TAB_HEIGHT;
            boolean selected = index == (this.craftableOnly ? 1 : 0);
            boolean hovered = mouseX >= this.getX() && mouseX < this.getRight()
                && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT;
            if (hovered) {
                this.hoveredIndex = index;
            }

            LegacyUiStyle.raisedPanel(
                graphics,
                this.getX(),
                tabY,
                TAB_WIDTH,
                TAB_HEIGHT - 2,
                selected ? LegacyUiStyle.PANEL_LIGHT : hovered ? 0xFFC6C6C6 : LegacyUiStyle.PANEL_DARK
            );
            ItemStack icon = new ItemStack(index == 0 ? Items.CRAFTING_TABLE : Items.EMERALD);
            graphics.pose().pushMatrix();
            graphics.pose().translate(this.getX() + 8, tabY + 7);
            graphics.pose().scale(1.35F, 1.35F);
            graphics.item(icon, 0, 0, index);
            graphics.pose().popMatrix();
            if (selected) {
                graphics.fill(this.getX() + TAB_WIDTH - 3, tabY + 3, this.getX() + TAB_WIDTH, tabY + TAB_HEIGHT - 5, LegacyUiStyle.PANEL_LIGHT);
            }
        }

        if (this.hoveredIndex >= 0) {
            graphics.setTooltipForNextFrame(
                Minecraft.getInstance().font,
                Component.translatable(this.hoveredIndex == 0 ? "legacycrafting.filter.all" : "legacycrafting.filter.craftable"),
                mouseX,
                mouseY
            );
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int index = ((int)event.y() - this.getY()) / TAB_HEIGHT;
        if (index >= 0 && index < 2) {
            this.select(index == 1);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_UP || event.key() == InputConstants.KEY_DOWN || event.isSelection()) {
            this.select(!this.craftableOnly);
            return true;
        }
        return false;
    }

    private void select(boolean craftableOnly) {
        if (this.craftableOnly != craftableOnly) {
            this.craftableOnly = craftableOnly;
            this.onChanged.accept(craftableOnly);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(
            NarratedElementType.TITLE,
            Component.translatable(this.craftableOnly ? "legacycrafting.filter.craftable" : "legacycrafting.filter.all")
        );
    }
}
