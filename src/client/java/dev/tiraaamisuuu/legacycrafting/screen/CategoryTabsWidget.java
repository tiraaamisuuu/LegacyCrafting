package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tiraaamisuuu.legacycrafting.client.LegacyUiSounds;
import dev.tiraaamisuuu.legacycrafting.recipe.LegacyCategory;
import java.util.List;
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

public final class CategoryTabsWidget extends AbstractWidget {
    static final int TAB_WIDTH = 51;
    private static final int TAB_HEIGHT = 43;
    private final Consumer<LegacyCategory> onSelected;
    private final int visibleTabs;
    private final float tabStep;
    private final List<LegacyCategory> categories;
    private LegacyCategory selected = LegacyCategory.BUILDING;
    private int firstVisible;
    private int hoveredIndex = -1;

    public CategoryTabsWidget(
        int x,
        int y,
        int totalWidth,
        int visibleTabs,
        List<LegacyCategory> categories,
        Consumer<LegacyCategory> onSelected
    ) {
        super(x, y, totalWidth, TAB_HEIGHT, CommonComponents.EMPTY);
        this.visibleTabs = visibleTabs;
        this.tabStep = visibleTabs <= 1 ? TAB_WIDTH : (totalWidth - TAB_WIDTH) / (float)(visibleTabs - 1);
        this.categories = List.copyOf(categories);
        this.onSelected = onSelected;
    }

    public LegacyCategory selected() {
        return this.selected;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredIndex = -1;
        for (int visibleIndex = 0; visibleIndex < this.visibleTabs; visibleIndex++) {
            int index = this.firstVisible + visibleIndex;
            if (index >= this.categories.size()) {
                break;
            }
            LegacyCategory category = this.categories.get(index);
            int tabX = this.getX() + Math.round(visibleIndex * this.tabStep);
            boolean selected = category == this.selected;
            int tabY = this.getY() + (selected ? 0 : 4);
            int tabHeight = TAB_HEIGHT - (selected ? 0 : 4);
            boolean hovered = mouseX >= tabX && mouseX < tabX + TAB_WIDTH
                && mouseY >= tabY && mouseY < tabY + tabHeight;
            if (hovered) {
                this.hoveredIndex = index;
            }
            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                selected ? this.selectedSprite(visibleIndex) : LegacySprites.LOW_TAB,
                tabX,
                tabY,
                TAB_WIDTH,
                tabHeight
            );
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, category.icon(), tabX + 13, tabY + 8, 24, 24);
        }
        if (this.hoveredIndex >= 0) {
            graphics.setTooltipForNextFrame(
                Minecraft.getInstance().font,
                this.categories.get(this.hoveredIndex).title(),
                mouseX,
                mouseY
            );
        }
    }

    private net.minecraft.resources.Identifier selectedSprite(int visibleIndex) {
        if (visibleIndex == 0) {
            return LegacySprites.HIGH_TAB_LEFT;
        }
        if (visibleIndex == this.visibleTabs - 1) {
            return LegacySprites.HIGH_TAB_RIGHT;
        }
        return LegacySprites.HIGH_TAB_MIDDLE;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        for (int visibleIndex = this.visibleTabs - 1; visibleIndex >= 0; visibleIndex--) {
            int tabX = this.getX() + Math.round(visibleIndex * this.tabStep);
            if (event.x() >= tabX && event.x() < tabX + TAB_WIDTH) {
                int index = this.firstVisible + visibleIndex;
                if (index < this.categories.size()) {
                    this.select(this.categories.get(index));
                }
                return;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isMouseOver(mouseX, mouseY) || scrollY == 0.0) {
            return false;
        }
        int selectedIndex = this.categories.indexOf(this.selected);
        int index = Math.floorMod(selectedIndex - (int)Math.signum(scrollY), this.categories.size());
        this.select(this.categories.get(index));
        return true;
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
        int selectedIndex = this.categories.indexOf(this.selected);
        int index = Math.floorMod(selectedIndex + delta, this.categories.size());
        this.select(this.categories.get(index));
        return true;
    }

    private void select(LegacyCategory category) {
        if (category != this.selected) {
            this.selected = category;
            this.ensureSelectedVisible();
            this.onSelected.accept(category);
            LegacyUiSounds.play(LegacyUiSounds.Cue.FOCUS);
        }
    }

    private void ensureSelectedVisible() {
        int selectedIndex = this.categories.indexOf(this.selected);
        if (selectedIndex < this.firstVisible) {
            this.firstVisible = selectedIndex;
        } else if (selectedIndex >= this.firstVisible + this.visibleTabs) {
            this.firstVisible = selectedIndex - this.visibleTabs + 1;
        }
        this.firstVisible = Math.max(
            0,
            Math.min(this.firstVisible, Math.max(0, this.categories.size() - this.visibleTabs))
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.selected.title());
        output.add(NarratedElementType.USAGE, net.minecraft.network.chat.Component.translatable("legacycrafting.category.navigation"));
    }
}
