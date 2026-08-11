package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.recipe.RecipeBrowser;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractCraftingMenu;

public final class LegacyCraftingScreen<T extends AbstractCraftingMenu> extends AbstractContainerScreen<T> {
    private static final int PANEL_WIDTH = 392;
    private static final int PANEL_HEIGHT = 226;
    private final RecipeBrowser recipeBrowser = new RecipeBrowser();
    private RecipeGridWidget recipeGrid;
    private RecipeDetailsWidget recipeDetails;

    public LegacyCraftingScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title, PANEL_WIDTH, PANEL_HEIGHT);
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        this.recipeDetails = new RecipeDetailsWidget(this.leftPos + 184, this.topPos + 154, 200, 64);
        this.recipeGrid = new RecipeGridWidget(this.leftPos + 184, this.topPos + 30, 5, 4, this.recipeDetails::setRecipe);
        this.addRenderableWidget(this.recipeGrid);
        this.addRenderableOnly(this.recipeDetails);
        this.recipeGrid.setRecipes(this.recipeBrowser.loadKnownRecipes(this.minecraft.player, this.menu));
        this.setInitialFocus(this.recipeGrid);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xE6101319);
        graphics.outline(this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFFE7B85A);
        graphics.fill(this.leftPos + 176, this.topPos + 1, this.leftPos + 177, this.topPos + this.imageHeight - 1, 0xFF596276);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, 8, 8, 0xFFFFFFFF, false);
        graphics.text(this.font, Component.translatable("legacycrafting.recipes"), 184, 9, 0xFFE7B85A, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFBBC3D1, false);
    }
}

