package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tiraaamisuuu.legacycrafting.client.LegacyUiSounds;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class RecipeGridWidget extends AbstractWidget {
    private static final int CELL_SIZE = 29;
    private static final int UNCRAFTABLE_OVERLAY = 0x77707070;

    private final int visibleColumns;
    private final Consumer<RecipeView> onSelected;
    private final BiConsumer<RecipeView, Boolean> onActivated;
    private List<RecipeView> recipes = List.of();
    private int selectedIndex = -1;
    private int scrollIndex;
    private int hoveredIndex = -1;

    public RecipeGridWidget(
        int x,
        int y,
        int visibleColumns,
        Consumer<RecipeView> onSelected,
        BiConsumer<RecipeView, Boolean> onActivated
    ) {
        super(x, y, visibleColumns * CELL_SIZE, CELL_SIZE, CommonComponents.EMPTY);
        this.visibleColumns = visibleColumns;
        this.onSelected = onSelected;
        this.onActivated = onActivated;
    }

    public void setRecipes(List<RecipeView> recipes) {
        int previousId = this.selectedRecipe() == null ? -1 : this.selectedRecipe().recipe().entry().id().index();
        this.recipes = List.copyOf(recipes);
        this.selectedIndex = -1;
        for (int index = 0; index < this.recipes.size(); index++) {
            if (this.recipes.get(index).recipe().entry().id().index() == previousId) {
                this.selectedIndex = index;
                break;
            }
        }
        if (this.selectedIndex < 0 && !this.recipes.isEmpty()) {
            this.selectedIndex = 0;
        }
        if (this.selectedIndex >= 0) {
            this.ensureSelectedVisible();
        } else {
            this.clampScroll();
        }
        this.onSelected.accept(this.selectedRecipe());
    }

    public RecipeView selectedRecipe() {
        return this.selectedIndex >= 0 && this.selectedIndex < this.recipes.size() ? this.recipes.get(this.selectedIndex) : null;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredIndex = -1;
        for (int visibleIndex = 0; visibleIndex < this.visibleColumns; visibleIndex++) {
            int recipeIndex = this.scrollIndex + visibleIndex;
            int cellX = this.getX() + visibleIndex * CELL_SIZE;
            int cellY = this.getY();
            boolean hovered = mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE;
            if (hovered && recipeIndex < this.recipes.size()) {
                this.hoveredIndex = recipeIndex;
            }

            LegacyUiStyle.slot(graphics, cellX, cellY, CELL_SIZE, recipeIndex == this.selectedIndex, false);
            if (recipeIndex >= this.recipes.size()) {
                continue;
            }
            RecipeView recipe = this.recipes.get(recipeIndex);
            ItemStack output = recipe.recipe().output();
            graphics.pose().pushMatrix();
            graphics.pose().translate(cellX + 5, cellY + 5);
            graphics.pose().scale(1.25F, 1.25F);
            graphics.item(output, 0, 0, recipeIndex);
            graphics.pose().popMatrix();
            graphics.itemDecorations(net.minecraft.client.Minecraft.getInstance().font, output, cellX + 6, cellY + 6);
            if (!recipe.craftable()) {
                graphics.fill(cellX + 2, cellY + 2, cellX + CELL_SIZE - 2, cellY + CELL_SIZE - 2, UNCRAFTABLE_OVERLAY);
            }
        }

        if (this.hoveredIndex >= 0) {
            ItemStack output = this.recipes.get(this.hoveredIndex).recipe().output();
            graphics.setTooltipForNextFrame(net.minecraft.client.Minecraft.getInstance().font, output, mouseX, mouseY);
        }
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == 0 || buttonInfo.button() == 1;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return;
        }
        int index = this.indexAt(event.x(), event.y());
        if (index >= 0) {
            this.select(index);
            this.onActivated.accept(this.recipes.get(index), event.hasShiftDown());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isMouseOver(mouseX, mouseY) || scrollY == 0.0) {
            return false;
        }
        int previousScroll = this.scrollIndex;
        this.scrollIndex -= (int)Math.signum(scrollY);
        this.clampScroll();
        if (previousScroll != this.scrollIndex) {
            LegacyUiSounds.play(LegacyUiSounds.Cue.SCROLL);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int delta = switch (event.key()) {
            case InputConstants.KEY_LEFT -> -1;
            case InputConstants.KEY_RIGHT -> 1;
            case InputConstants.KEY_PAGEUP -> -this.visibleColumns;
            case InputConstants.KEY_PAGEDOWN -> this.visibleColumns;
            default -> 0;
        };
        if (delta != 0 && !this.recipes.isEmpty()) {
            this.select(Math.max(0, Math.min(this.recipes.size() - 1, this.selectedIndex + delta)));
            return true;
        }
        if (event.isSelection() && this.selectedRecipe() != null) {
            this.onActivated.accept(this.selectedRecipe(), event.hasShiftDown());
            return true;
        }
        return super.keyPressed(event);
    }

    private int indexAt(double mouseX, double mouseY) {
        int localX = (int)mouseX - this.getX();
        int localY = (int)mouseY - this.getY();
        if (localX < 0 || localY < 0 || localX >= this.getWidth() || localY >= this.getHeight()) {
            return -1;
        }
        int index = this.scrollIndex + localX / CELL_SIZE;
        return index < this.recipes.size() ? index : -1;
    }

    private void select(int index) {
        boolean changed = this.selectedIndex != index;
        this.selectedIndex = index;
        this.ensureSelectedVisible();
        this.onSelected.accept(this.recipes.get(index));
        if (changed) {
            LegacyUiSounds.play(LegacyUiSounds.Cue.FOCUS);
        }
    }

    private void ensureSelectedVisible() {
        if (this.selectedIndex < this.scrollIndex) {
            this.scrollIndex = this.selectedIndex;
        } else if (this.selectedIndex >= this.scrollIndex + this.visibleColumns) {
            this.scrollIndex = this.selectedIndex - this.visibleColumns + 1;
        }
        this.clampScroll();
    }

    private void clampScroll() {
        this.scrollIndex = Math.max(0, Math.min(this.scrollIndex, Math.max(0, this.recipes.size() - this.visibleColumns)));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        RecipeView selected = this.selectedRecipe();
        Component name = selected == null ? Component.translatable("legacycrafting.recipe.none") : selected.recipe().output().getHoverName();
        output.add(NarratedElementType.TITLE, Component.translatable("legacycrafting.recipe.selected", name));
        output.add(NarratedElementType.USAGE, Component.translatable("legacycrafting.recipe.navigation"));
    }
}
