package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.recipe.RecipeBrowser;
import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeCraftabilityService;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public final class LegacyCraftingScreen<T extends AbstractCraftingMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
    private static final int PANEL_WIDTH = 392;
    private static final int PANEL_HEIGHT = 226;
    private final RecipeBrowser recipeBrowser = new RecipeBrowser();
    private final RecipeCraftabilityService craftabilityService = new RecipeCraftabilityService();
    private RecipeGridWidget recipeGrid;
    private RecipeDetailsWidget recipeDetails;
    private Button filterButton;
    private List<BrowserRecipe> knownRecipes = List.of();
    private List<RecipeView> recipeViews = List.of();
    private boolean craftableOnly;
    private int lastInventoryVersion = -1;
    private int lastMenuStateId = -1;

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
        this.filterButton = this.addRenderableWidget(Button.builder(this.filterLabel(), button -> {
            this.craftableOnly = !this.craftableOnly;
            button.setMessage(this.filterLabel());
            this.applyFilter();
        }).bounds(this.leftPos + 300, this.topPos + 5, 84, 20).build());
        this.reloadRecipes();
        this.setInitialFocus(this.recipeGrid);
    }

    @Override
    protected void containerTick() {
        int inventoryVersion = this.minecraft.player.getInventory().getTimesChanged();
        int menuStateId = this.menu.getStateId();
        if (inventoryVersion != this.lastInventoryVersion || menuStateId != this.lastMenuStateId) {
            this.refreshCraftability();
        }
    }

    private void reloadRecipes() {
        this.knownRecipes = this.recipeBrowser.loadKnownRecipes(this.minecraft.player, this.menu);
        this.refreshCraftability();
    }

    private void refreshCraftability() {
        this.recipeViews = this.craftabilityService.evaluate(this.knownRecipes, this.minecraft.player, this.menu);
        this.lastInventoryVersion = this.minecraft.player.getInventory().getTimesChanged();
        this.lastMenuStateId = this.menu.getStateId();
        this.applyFilter();
    }

    private void applyFilter() {
        this.recipeGrid.setRecipes(this.craftableOnly
            ? this.recipeViews.stream().filter(RecipeView::craftable).toList()
            : this.recipeViews);
    }

    private Component filterLabel() {
        return Component.translatable(this.craftableOnly ? "legacycrafting.filter.craftable" : "legacycrafting.filter.all");
    }

    @Override
    public void recipesUpdated() {
        this.reloadRecipes();
    }

    @Override
    public void fillGhostRecipe(RecipeDisplay display) {
        // The details panel is the custom screen's ghost/ingredient preview.
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
