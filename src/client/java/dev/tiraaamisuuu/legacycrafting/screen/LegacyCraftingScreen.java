package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.crafting.CraftExecutor;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeBrowser;
import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe;
import dev.tiraaamisuuu.legacycrafting.recipe.LegacyCategory;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeCraftabilityService;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public final class LegacyCraftingScreen<T extends AbstractCraftingMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
    private static final int PANEL_WIDTH = 392;
    private static final int PANEL_HEIGHT = 264;
    private final RecipeBrowser recipeBrowser = new RecipeBrowser();
    private final RecipeCraftabilityService craftabilityService = new RecipeCraftabilityService();
    private RecipeGridWidget recipeGrid;
    private RecipeDetailsWidget recipeDetails;
    private CategoryTabsWidget categoryTabs;
    private CraftExecutor craftExecutor;
    private Button filterButton;
    private List<BrowserRecipe> knownRecipes = List.of();
    private List<RecipeView> recipeViews = List.of();
    private boolean craftableOnly;
    private LegacyCategory selectedCategory = LegacyCategory.BUILDING;
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
        this.recipeDetails = new RecipeDetailsWidget(this.leftPos + 184, this.topPos + 158, 200, 98);
        this.craftExecutor = new CraftExecutor(this.minecraft, this.menu, this.recipeDetails::setStatus);
        this.categoryTabs = new CategoryTabsWidget(this.leftPos + 184, this.topPos + 5, category -> {
            this.selectedCategory = category;
            this.applyFilter();
        });
        this.recipeGrid = new RecipeGridWidget(
            this.leftPos + 184,
            this.topPos + 34,
            5,
            4,
            this.recipeDetails::setRecipe,
            this::activateRecipe
        );
        this.addRenderableWidget(this.categoryTabs);
        this.addRenderableWidget(this.recipeGrid);
        this.addRenderableOnly(this.recipeDetails);
        this.filterButton = this.addRenderableWidget(Button.builder(this.filterLabel(), button -> {
            this.craftableOnly = !this.craftableOnly;
            button.setMessage(this.filterLabel());
            this.applyFilter();
        }).bounds(this.leftPos + 8, this.topPos + 172, 160, 20).build());
        this.reloadRecipes();
        this.setInitialFocus(this.recipeGrid);
    }

    @Override
    protected void containerTick() {
        this.craftExecutor.tick();
        int inventoryVersion = this.minecraft.player.getInventory().getTimesChanged();
        int menuStateId = this.menu.getStateId();
        if (inventoryVersion != this.lastInventoryVersion || menuStateId != this.lastMenuStateId) {
            this.refreshCraftability();
        }
    }

    private void activateRecipe(RecipeView recipe, boolean maximum) {
        this.craftExecutor.start(recipe, maximum);
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
            ? this.recipeViews.stream()
                .filter(view -> view.recipe().category() == this.selectedCategory)
                .filter(RecipeView::craftable)
                .toList()
            : this.recipeViews.stream().filter(view -> view.recipe().category() == this.selectedCategory).toList());
    }

    private Component filterLabel() {
        return Component.translatable(this.craftableOnly ? "legacycrafting.filter.craftable" : "legacycrafting.filter.all");
    }

    public void openVanillaScreen() {
        if (this.menu instanceof CraftingMenu craftingMenu) {
            this.minecraft.gui.setScreen(new CraftingScreen(craftingMenu, this.minecraft.player.getInventory(), this.title));
        } else if (this.menu instanceof InventoryMenu) {
            this.minecraft.gui.setScreen(new InventoryScreen(this.minecraft.player));
        }
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
        for (Slot slot : this.menu.slots) {
            if (slot.x >= 176) {
                continue;
            }
            int x = this.leftPos + slot.x - 1;
            int y = this.topPos + slot.y - 1;
            graphics.fill(x, y, x + 18, y + 18, 0xFF252A35);
            graphics.outline(x, y, 18, 18, slot == this.menu.getResultSlot() ? 0xFFE7B85A : 0xFF596276);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, 8, 8, 0xFFFFFFFF, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFBBC3D1, false);
    }
}
