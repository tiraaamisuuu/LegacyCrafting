package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tiraaamisuuu.legacycrafting.client.LegacyUiSounds;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeGroup;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeGroupingService;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
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
    private static final int CELL_SIZE = 27;
    private static final int UNCRAFTABLE_OVERLAY = 0x77707070;

    private final int visibleColumns;
    private final Consumer<RecipeView> onSelected;
    private final BiConsumer<RecipeView, Boolean> onActivated;
    private final RecipeGroupingService groupingService = new RecipeGroupingService();
    private List<RecipeGroup> groups = List.of();
    private int selectedGroupIndex = -1;
    private int selectedVariantIndex;
    private int scrollIndex;
    private int hoveredGroupIndex = -1;
    private int hoveredVariantDelta;

    public RecipeGridWidget(
        int x,
        int y,
        int visibleColumns,
        Consumer<RecipeView> onSelected,
        BiConsumer<RecipeView, Boolean> onActivated
    ) {
        super(x, y - CELL_SIZE, visibleColumns * CELL_SIZE, CELL_SIZE * 3, CommonComponents.EMPTY);
        this.visibleColumns = visibleColumns;
        this.onSelected = onSelected;
        this.onActivated = onActivated;
    }

    public void setRecipes(List<RecipeView> recipes) {
        int previousId = this.selectedRecipe() == null ? -1 : this.selectedRecipe().recipe().entry().id().index();
        this.groups = this.groupingService.group(recipes);
        this.selectedGroupIndex = -1;
        this.selectedVariantIndex = 0;
        for (int groupIndex = 0; groupIndex < this.groups.size(); groupIndex++) {
            List<RecipeView> variants = this.groups.get(groupIndex).variants();
            for (int variantIndex = 0; variantIndex < variants.size(); variantIndex++) {
                if (variants.get(variantIndex).recipe().entry().id().index() == previousId) {
                    this.selectedGroupIndex = groupIndex;
                    this.selectedVariantIndex = variantIndex;
                    break;
                }
            }
            if (this.selectedGroupIndex >= 0) {
                break;
            }
        }
        if (this.selectedGroupIndex < 0 && !this.groups.isEmpty()) {
            this.selectedGroupIndex = 0;
        }
        if (this.selectedGroupIndex >= 0) {
            this.ensureSelectedVisible();
        } else {
            this.clampScroll();
        }
        this.onSelected.accept(this.selectedRecipe());
    }

    public RecipeView selectedRecipe() {
        if (this.selectedGroupIndex < 0 || this.selectedGroupIndex >= this.groups.size()) {
            return null;
        }
        List<RecipeView> variants = this.groups.get(this.selectedGroupIndex).variants();
        return variants.isEmpty() ? null : variants.get(Math.min(this.selectedVariantIndex, variants.size() - 1));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredGroupIndex = -1;
        this.hoveredVariantDelta = 0;
        int rowY = this.getY() + CELL_SIZE;
        for (int visibleIndex = 0; visibleIndex < this.visibleColumns; visibleIndex++) {
            int groupIndex = this.scrollIndex + visibleIndex;
            int cellX = this.getX() + visibleIndex * CELL_SIZE;
            LegacyUiStyle.slot(graphics, cellX, rowY, CELL_SIZE, false, false);
            if (groupIndex >= this.groups.size()) {
                continue;
            }

            RecipeGroup group = this.groups.get(groupIndex);
            boolean selected = groupIndex == this.selectedGroupIndex;
            int variantIndex = selected ? this.selectedVariantIndex : 0;
            if (selected && group.variants().size() > 1) {
                LegacyUiStyle.recipeSelection(graphics, cellX, rowY, group.variants().size());
                if (group.variants().size() > 2) {
                    this.renderRecipe(graphics, group.variants().get(previousVariant(group, variantIndex)), cellX, rowY - CELL_SIZE, groupIndex);
                }
                this.renderRecipe(graphics, group.variants().get(nextVariant(group, variantIndex)), cellX, rowY + CELL_SIZE, groupIndex);
            }
            this.renderRecipe(graphics, group.variants().get(variantIndex), cellX, rowY, groupIndex);
            if (selected) {
                LegacyUiStyle.slot(graphics, cellX, rowY, CELL_SIZE, true, false);
                this.renderRecipe(graphics, group.variants().get(variantIndex), cellX, rowY, groupIndex);
            }

            if (contains(mouseX, mouseY, cellX, rowY)) {
                this.hoveredGroupIndex = groupIndex;
            } else if (selected && group.variants().size() > 2 && contains(mouseX, mouseY, cellX, rowY - CELL_SIZE)) {
                this.hoveredGroupIndex = groupIndex;
                this.hoveredVariantDelta = -1;
            } else if (selected && group.variants().size() > 1 && contains(mouseX, mouseY, cellX, rowY + CELL_SIZE)) {
                this.hoveredGroupIndex = groupIndex;
                this.hoveredVariantDelta = 1;
            }
        }

        if (this.scrollIndex > 0) {
            graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, LegacySprites.SCROLL_LEFT, this.getX() - 8, rowY + 7, 6, 11);
        }
        if (this.scrollIndex < Math.max(0, this.groups.size() - this.visibleColumns)) {
            graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, LegacySprites.SCROLL_RIGHT, this.getX() + this.getWidth() + 2, rowY + 7, 6, 11);
        }

        RecipeView hovered = this.hoveredRecipe();
        if (hovered != null) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, hovered.recipe().output(), mouseX, mouseY);
        }
    }

    private void renderRecipe(GuiGraphicsExtractor graphics, RecipeView recipe, int x, int y, int seed) {
        ItemStack output = recipe.recipe().output();
        LegacyItemRenderer.render(graphics, output, x, y, CELL_SIZE, seed);
        if (!recipe.craftable()) {
            graphics.fill(x + 2, y + 2, x + CELL_SIZE - 2, y + CELL_SIZE - 2, UNCRAFTABLE_OVERLAY);
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
        int groupIndex = this.groupAt(event.x());
        if (groupIndex < 0) {
            return;
        }
        int rowY = this.getY() + CELL_SIZE;
        int clickedDelta = (int)Math.floor((event.y() - rowY + CELL_SIZE / 2.0) / CELL_SIZE);
        if (groupIndex == this.selectedGroupIndex && clickedDelta != 0 && this.groups.get(groupIndex).variants().size() > 1) {
            this.cycleVariant(clickedDelta < 0 ? -1 : 1);
            return;
        }
        this.selectGroup(groupIndex);
        this.onActivated.accept(this.selectedRecipe(), event.hasShiftDown());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.isMouseOver(mouseX, mouseY) || scrollY == 0.0) {
            return false;
        }
        if (this.hoveredGroupIndex == this.selectedGroupIndex && this.selectedGroupIndex >= 0
            && this.groups.get(this.selectedGroupIndex).variants().size() > 1) {
            this.cycleVariant(scrollY > 0 ? -1 : 1);
            return true;
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
        if ((event.key() == InputConstants.KEY_UP || event.key() == InputConstants.KEY_DOWN) && this.selectedRecipe() != null) {
            this.cycleVariant(event.key() == InputConstants.KEY_UP ? -1 : 1);
            return true;
        }
        int delta = switch (event.key()) {
            case InputConstants.KEY_LEFT -> -1;
            case InputConstants.KEY_RIGHT -> 1;
            case InputConstants.KEY_PAGEUP -> -this.visibleColumns;
            case InputConstants.KEY_PAGEDOWN -> this.visibleColumns;
            default -> 0;
        };
        if (delta != 0 && !this.groups.isEmpty()) {
            this.selectGroup(Math.max(0, Math.min(this.groups.size() - 1, this.selectedGroupIndex + delta)));
            return true;
        }
        if (event.isSelection() && this.selectedRecipe() != null) {
            this.onActivated.accept(this.selectedRecipe(), event.hasShiftDown());
            return true;
        }
        return super.keyPressed(event);
    }

    private RecipeView hoveredRecipe() {
        if (this.hoveredGroupIndex < 0 || this.hoveredGroupIndex >= this.groups.size()) {
            return null;
        }
        RecipeGroup group = this.groups.get(this.hoveredGroupIndex);
        int variant = this.hoveredGroupIndex == this.selectedGroupIndex ? this.selectedVariantIndex : 0;
        if (this.hoveredVariantDelta < 0) {
            variant = previousVariant(group, variant);
        } else if (this.hoveredVariantDelta > 0) {
            variant = nextVariant(group, variant);
        }
        return group.variants().get(variant);
    }

    private int groupAt(double mouseX) {
        int localX = (int)mouseX - this.getX();
        if (localX < 0 || localX >= this.getWidth()) {
            return -1;
        }
        int index = this.scrollIndex + localX / CELL_SIZE;
        return index < this.groups.size() ? index : -1;
    }

    private void selectGroup(int index) {
        boolean changed = this.selectedGroupIndex != index;
        this.selectedGroupIndex = index;
        if (changed) {
            this.selectedVariantIndex = 0;
        }
        this.ensureSelectedVisible();
        this.onSelected.accept(this.selectedRecipe());
        if (changed) {
            LegacyUiSounds.play(LegacyUiSounds.Cue.FOCUS);
        }
    }

    private void cycleVariant(int delta) {
        if (this.selectedGroupIndex < 0) {
            return;
        }
        RecipeGroup group = this.groups.get(this.selectedGroupIndex);
        if (group.variants().size() < 2) {
            return;
        }
        this.selectedVariantIndex = Math.floorMod(this.selectedVariantIndex + delta, group.variants().size());
        this.onSelected.accept(this.selectedRecipe());
        LegacyUiSounds.play(LegacyUiSounds.Cue.FOCUS);
    }

    private static int previousVariant(RecipeGroup group, int index) {
        return Math.floorMod(index - 1, group.variants().size());
    }

    private static int nextVariant(RecipeGroup group, int index) {
        return Math.floorMod(index + 1, group.variants().size());
    }

    private static boolean contains(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE;
    }

    private void ensureSelectedVisible() {
        if (this.selectedGroupIndex < this.scrollIndex) {
            this.scrollIndex = this.selectedGroupIndex;
        } else if (this.selectedGroupIndex >= this.scrollIndex + this.visibleColumns) {
            this.scrollIndex = this.selectedGroupIndex - this.visibleColumns + 1;
        }
        this.clampScroll();
    }

    private void clampScroll() {
        this.scrollIndex = Math.max(0, Math.min(this.scrollIndex, Math.max(0, this.groups.size() - this.visibleColumns)));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        RecipeView selected = this.selectedRecipe();
        Component name = selected == null ? Component.translatable("legacycrafting.recipe.none") : selected.recipe().output().getHoverName();
        output.add(NarratedElementType.TITLE, Component.translatable("legacycrafting.recipe.selected", name));
        output.add(NarratedElementType.USAGE, Component.translatable("legacycrafting.recipe.navigation"));
    }
}
