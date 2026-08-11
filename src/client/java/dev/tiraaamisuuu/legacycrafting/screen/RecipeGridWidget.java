package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe;
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
    private static final int CELL_SIZE = 30;
    private static final int BACKGROUND = 0xFF252A35;
    private static final int BORDER = 0xFF596276;
    private static final int HOVERED = 0xFF505A70;
    private static final int SELECTED = 0xFFE7B85A;
    private static final int UNCRAFTABLE_OVERLAY = 0x99101319;

    private final int columns;
    private final int visibleRows;
    private final Consumer<RecipeView> onSelected;
    private final BiConsumer<RecipeView, Boolean> onActivated;
    private List<RecipeView> recipes = List.of();
    private int selectedIndex = -1;
    private int scrollRow;
    private int hoveredIndex = -1;

    public RecipeGridWidget(
        int x,
        int y,
        int columns,
        int visibleRows,
        Consumer<RecipeView> onSelected,
        BiConsumer<RecipeView, Boolean> onActivated
    ) {
        super(x, y, columns * CELL_SIZE, visibleRows * CELL_SIZE, CommonComponents.EMPTY);
        this.columns = columns;
        this.visibleRows = visibleRows;
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
        this.clampScroll();
        if (this.selectedRecipe() != null) {
            this.onSelected.accept(this.selectedRecipe());
        }
    }

    public RecipeView selectedRecipe() {
        return this.selectedIndex >= 0 && this.selectedIndex < this.recipes.size() ? this.recipes.get(this.selectedIndex) : null;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.getX() - 2, this.getY() - 2, this.getRight() + 2, this.getBottom() + 2, 0xFF11141B);
        this.hoveredIndex = -1;
        int firstIndex = this.scrollRow * this.columns;
        int visibleCount = this.columns * this.visibleRows;

        for (int visibleIndex = 0; visibleIndex < visibleCount; visibleIndex++) {
            int recipeIndex = firstIndex + visibleIndex;
            if (recipeIndex >= this.recipes.size()) {
                break;
            }

            int cellX = this.getX() + visibleIndex % this.columns * CELL_SIZE;
            int cellY = this.getY() + visibleIndex / this.columns * CELL_SIZE;
            boolean hovered = mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE;
            if (hovered) {
                this.hoveredIndex = recipeIndex;
            }

            graphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, hovered ? HOVERED : BACKGROUND);
            graphics.outline(cellX, cellY, CELL_SIZE, CELL_SIZE, recipeIndex == this.selectedIndex ? SELECTED : BORDER);
            RecipeView recipe = this.recipes.get(recipeIndex);
            ItemStack output = recipe.recipe().output();
            graphics.pose().pushMatrix();
            graphics.pose().translate(cellX + 5, cellY + 5);
            graphics.pose().scale(1.25F, 1.25F);
            graphics.item(output, 0, 0, recipeIndex);
            graphics.itemDecorations(net.minecraft.client.Minecraft.getInstance().font, output, 0, 0);
            graphics.pose().popMatrix();
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
        this.scrollRow -= (int)Math.signum(scrollY);
        this.clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int delta = switch (event.key()) {
            case InputConstants.KEY_LEFT -> -1;
            case InputConstants.KEY_RIGHT -> 1;
            case InputConstants.KEY_UP -> -this.columns;
            case InputConstants.KEY_DOWN -> this.columns;
            case InputConstants.KEY_PAGEUP -> -this.columns * this.visibleRows;
            case InputConstants.KEY_PAGEDOWN -> this.columns * this.visibleRows;
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
        int index = this.scrollRow * this.columns + localY / CELL_SIZE * this.columns + localX / CELL_SIZE;
        return index < this.recipes.size() ? index : -1;
    }

    private void select(int index) {
        this.selectedIndex = index;
        this.ensureSelectedVisible();
        this.onSelected.accept(this.recipes.get(index));
    }

    private void ensureSelectedVisible() {
        int selectedRow = this.selectedIndex / this.columns;
        if (selectedRow < this.scrollRow) {
            this.scrollRow = selectedRow;
        } else if (selectedRow >= this.scrollRow + this.visibleRows) {
            this.scrollRow = selectedRow - this.visibleRows + 1;
        }
        this.clampScroll();
    }

    private void clampScroll() {
        int totalRows = (this.recipes.size() + this.columns - 1) / this.columns;
        this.scrollRow = Math.max(0, Math.min(this.scrollRow, Math.max(0, totalRows - this.visibleRows)));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        RecipeView selected = this.selectedRecipe();
        Component name = selected == null ? Component.translatable("legacycrafting.recipe.none") : selected.recipe().output().getHoverName();
        output.add(NarratedElementType.TITLE, Component.translatable("legacycrafting.recipe.selected", name));
        output.add(NarratedElementType.USAGE, Component.translatable("legacycrafting.recipe.navigation"));
    }
}
