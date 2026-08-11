package dev.tiraaamisuuu.legacycrafting.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tiraaamisuuu.legacycrafting.client.LegacyUiSounds;
import dev.tiraaamisuuu.legacycrafting.crafting.CraftExecutor;
import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe;
import dev.tiraaamisuuu.legacycrafting.recipe.LegacyCategory;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeBrowser;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeCraftabilityService;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public final class LegacyCraftingScreen<T extends AbstractCraftingMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
    private static final int PANEL_WIDTH = 392;
    private static final int PANEL_HEIGHT = 226;
    private static final int MENU_ORIGIN_X = 200;
    private static final int MENU_ORIGIN_Y = 49;
    private static final int LOWER_PANEL_Y = 94;
    private final RecipeBrowser recipeBrowser = new RecipeBrowser();
    private final RecipeCraftabilityService craftabilityService = new RecipeCraftabilityService();
    private RecipeGridWidget recipeGrid;
    private RecipeDetailsWidget recipeDetails;
    private CategoryTabsWidget categoryTabs;
    private RecipeFilterTabsWidget filterTabs;
    private CraftExecutor craftExecutor;
    private List<BrowserRecipe> knownRecipes = List.of();
    private List<RecipeView> recipeViews = List.of();
    private boolean craftableOnly;
    private LegacyCategory selectedCategory = LegacyCategory.BUILDING;
    private int lastInventoryVersion = -1;
    private int lastMenuStateId = -1;

    public LegacyCraftingScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = this.panelLeft() + MENU_ORIGIN_X;
        this.topPos = this.panelTop() + MENU_ORIGIN_Y;

        this.recipeDetails = new RecipeDetailsWidget(
            this.panelLeft() + 4,
            this.panelTop() + LOWER_PANEL_Y + 2,
            196,
            PANEL_HEIGHT - LOWER_PANEL_Y - 5,
            this.menu.getGridWidth()
        );
        this.craftExecutor = new CraftExecutor(
            this.minecraft,
            this.menu,
            this.recipeDetails::setStatus,
            feedback -> LegacyUiSounds.play(feedback == CraftExecutor.Feedback.CRAFT_SUCCEEDED
                ? LegacyUiSounds.Cue.CRAFT_SUCCESS
                : LegacyUiSounds.Cue.CRAFT_FAIL)
        );
        this.categoryTabs = new CategoryTabsWidget(this.panelLeft() + 24, this.panelTop() - 27, category -> {
            this.selectedCategory = category;
            this.applyFilter();
        });
        this.recipeGrid = new RecipeGridWidget(
            this.panelLeft() + 44,
            this.panelTop() + 35,
            11,
            this.recipeDetails::setRecipe,
            this::activateRecipe
        );
        this.filterTabs = new RecipeFilterTabsWidget(this.panelLeft() - 34, this.panelTop() + 7, craftableOnly -> {
            this.craftableOnly = craftableOnly;
            this.applyFilter();
        });
        this.addRenderableWidget(this.categoryTabs);
        this.addRenderableWidget(this.filterTabs);
        this.addRenderableWidget(this.recipeGrid);
        this.addRenderableOnly(this.recipeDetails);
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

    public void openVanillaScreen() {
        LegacyUiSounds.play(LegacyUiSounds.Cue.BACK);
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
        int panelLeft = this.panelLeft();
        int panelTop = this.panelTop();
        LegacyUiStyle.raisedPanel(graphics, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT, LegacyUiStyle.PANEL);

        graphics.fill(panelLeft + 2, panelTop + LOWER_PANEL_Y, panelLeft + PANEL_WIDTH - 2, panelTop + LOWER_PANEL_Y + 2, LegacyUiStyle.SHADOW);
        graphics.fill(panelLeft + 201, panelTop + LOWER_PANEL_Y + 2, panelLeft + 203, panelTop + PANEL_HEIGHT - 2, LegacyUiStyle.SHADOW);

        for (Slot slot : this.menu.slots) {
            if (!this.isPlayerInventorySlot(slot)) {
                continue;
            }
            LegacyUiStyle.slot(graphics, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 18, false, false);
        }

        int hintX = panelLeft;
        int hintY = panelTop + PANEL_HEIGHT + 7;
        hintX += LegacyControlHint.draw(graphics, this.font, hintX, hintY, LegacyControlHint.Button.A,
            Component.translatable("legacycrafting.hint.craft")) + 6;
        hintX += LegacyControlHint.draw(graphics, this.font, hintX, hintY, LegacyControlHint.Button.Y,
            Component.translatable("legacycrafting.hint.maximum")) + 6;
        hintX += LegacyControlHint.draw(graphics, this.font, hintX, hintY, LegacyControlHint.Button.X,
            Component.translatable("legacycrafting.hint.filter")) + 6;
        hintX += LegacyControlHint.draw(graphics, this.font, hintX, hintY, LegacyControlHint.Button.LEFT_BUMPER,
            Component.translatable("legacycrafting.hint.vanilla")) + 6;
        LegacyControlHint.draw(graphics, this.font, hintX, hintY, LegacyControlHint.Button.B,
            Component.translatable("legacycrafting.hint.exit"));
    }

    @Override
    protected void extractSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for (Slot slot : this.menu.slots) {
            if (this.isPlayerInventorySlot(slot)) {
                this.extractSlot(graphics, slot, mouseX, mouseY);
            }
        }
    }

    @Override
    protected boolean isHovering(int left, int top, int width, int height, double mouseX, double mouseY) {
        return top >= 84 && super.isHovering(left, top, width, height, mouseX, mouseY);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int screenX, int screenY) {
        int panelLeft = this.panelLeft();
        int panelTop = this.panelTop();
        return mouseX < panelLeft || mouseY < panelTop - 27
            || mouseX >= panelLeft + PANEL_WIDTH || mouseY >= panelTop + PANEL_HEIGHT;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_A && this.recipeGrid.selectedRecipe() != null) {
            this.activateRecipe(this.recipeGrid.selectedRecipe(), false);
            return true;
        }
        if (event.key() == InputConstants.KEY_Y && this.recipeGrid.selectedRecipe() != null) {
            this.activateRecipe(this.recipeGrid.selectedRecipe(), true);
            return true;
        }
        if (event.key() == InputConstants.KEY_X) {
            this.filterTabs.toggle();
            return true;
        }
        if (event.key() == InputConstants.KEY_B) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        LegacyUiSounds.play(LegacyUiSounds.Cue.BACK);
        super.onClose();
    }

    private boolean isPlayerInventorySlot(Slot slot) {
        return slot.y >= 84;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.centeredText(
            this.font,
            this.selectedCategory.title(),
            -MENU_ORIGIN_X + PANEL_WIDTH / 2,
            -MENU_ORIGIN_Y + 14,
            LegacyUiStyle.TEXT
        );
        graphics.centeredText(
            this.font,
            this.playerInventoryTitle,
            -MENU_ORIGIN_X + 297,
            -MENU_ORIGIN_Y + LOWER_PANEL_Y + 10,
            LegacyUiStyle.TEXT
        );
    }

    private int panelLeft() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return (this.height - PANEL_HEIGHT) / 2;
    }
}
