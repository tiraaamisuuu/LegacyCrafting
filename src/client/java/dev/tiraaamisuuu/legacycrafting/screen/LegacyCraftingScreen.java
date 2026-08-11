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
    private final LegacyCraftingLayout layout;
    private final RecipeBrowser recipeBrowser = new RecipeBrowser();
    private final RecipeCraftabilityService craftabilityService = new RecipeCraftabilityService();
    private RecipeGridWidget recipeGrid;
    private RecipeDetailsWidget recipeDetails;
    private CategoryTabsWidget categoryTabs;
    private CraftingTypeTabsWidget typeTabs;
    private CraftExecutor craftExecutor;
    private List<BrowserRecipe> knownRecipes = List.of();
    private List<RecipeView> recipeViews = List.of();
    private boolean craftableOnly;
    private CraftingType selectedType = CraftingType.CRAFTING;
    private LegacyCategory selectedCategory = LegacyCategory.BUILDING;
    private int lastInventoryVersion = -1;
    private int lastMenuStateId = -1;

    public LegacyCraftingScreen(T menu, Inventory inventory, Component title) {
        this(menu, inventory, title, LegacyCraftingLayout.forGrid(menu.getGridWidth()));
    }

    private LegacyCraftingScreen(T menu, Inventory inventory, Component title, LegacyCraftingLayout layout) {
        super(menu, inventory, title, layout.imageWidth(), layout.imageHeight());
        this.layout = layout;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = this.panelLeft();
        this.topPos = this.panelTop();

        this.recipeDetails = new RecipeDetailsWidget(
            this.panelLeft() + LegacyCraftingLayout.CRAFTING_PANEL_X,
            this.panelTop() + LegacyCraftingLayout.BOTTOM_PANEL_Y,
            this.menu.getGridWidth(),
            this.layout
        );
        this.craftExecutor = new CraftExecutor(
            this.minecraft,
            this.menu,
            this.recipeDetails::setStatus,
            feedback -> LegacyUiSounds.play(feedback == CraftExecutor.Feedback.CRAFT_SUCCEEDED
                ? LegacyUiSounds.Cue.CRAFT_SUCCESS
                : LegacyUiSounds.Cue.CRAFT_FAIL)
        );
        this.categoryTabs = new CategoryTabsWidget(
            this.panelLeft() + this.layout.categoryTabsX(),
            this.panelTop() - 37,
            this.layout.imageWidth(),
            this.layout.maxCategoryTabs(),
            LegacyCategory.forGrid(this.menu.getGridWidth()),
            category -> {
            this.selectedCategory = category;
            this.applyFilter();
        });
        this.recipeGrid = new RecipeGridWidget(
            this.panelLeft() + this.layout.recipeButtonsX(),
            this.panelTop() + LegacyCraftingLayout.RECIPE_BUTTONS_Y,
            this.layout.maxRecipeButtons(),
            this.recipeDetails::setRecipe,
            this::activateRecipe
        );
        this.typeTabs = new CraftingTypeTabsWidget(this.panelLeft() - 36, this.panelTop() + 4, type -> {
            this.selectedType = type;
            this.applyFilter();
        });
        this.addWidget(this.categoryTabs);
        this.addWidget(this.typeTabs);
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
        this.recipeGrid.setRecipes(this.recipeViews.stream()
            .filter(view -> view.recipe().category() == this.selectedCategory)
            .filter(this.selectedType::accepts)
            .filter(view -> !this.craftableOnly || view.craftable())
            .toList());
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
        this.categoryTabs.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.typeTabs.extractRenderState(graphics, mouseX, mouseY, partialTick);
        LegacyUiStyle.raisedPanel(
            graphics,
            panelLeft,
            panelTop,
            this.layout.imageWidth(),
            this.layout.imageHeight(),
            LegacyUiStyle.PANEL
        );

        LegacyUiStyle.insetPanel(
            graphics,
            panelLeft + this.layout.inventoryPanelX(),
            panelTop + LegacyCraftingLayout.BOTTOM_PANEL_Y,
            this.layout.inventoryPanelWidth(),
            LegacyCraftingLayout.BOTTOM_PANEL_HEIGHT,
            LegacyUiStyle.PANEL_DARK
        );

        for (Slot slot : this.menu.slots) {
            if (!this.isPlayerInventorySlot(slot)) {
                continue;
            }
            SlotPosition position = this.visualPosition(slot);
            LegacyUiStyle.slot(graphics, panelLeft + position.x(), panelTop + position.y(), 16, false, false);
        }

        int hintX = panelLeft;
        int hintY = panelTop + this.layout.imageHeight() + 8;
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
                SlotPosition position = this.visualPosition(slot);
                graphics.pose().pushMatrix();
                graphics.pose().translate(position.x() - slot.x, position.y() - slot.y);
                this.extractSlot(graphics, slot, mouseX, mouseY);
                graphics.pose().popMatrix();
            }
        }
    }

    @Override
    protected boolean isHovering(int left, int top, int width, int height, double mouseX, double mouseY) {
        SlotPosition position = this.visualPosition(left, top);
        return position != null && super.isHovering(position.x(), position.y(), 16, 16, mouseX, mouseY);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int screenX, int screenY) {
        int panelLeft = this.panelLeft();
        int panelTop = this.panelTop();
        return mouseX < panelLeft - 36 || mouseY < panelTop - 37
            || mouseX >= panelLeft + this.layout.imageWidth() || mouseY >= panelTop + this.layout.imageHeight();
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
            this.craftableOnly = !this.craftableOnly;
            this.applyFilter();
            LegacyUiSounds.play(LegacyUiSounds.Cue.ACTION);
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
        return slot.container == this.minecraft.player.getInventory() && slot.y >= 84;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        LegacyText.centered(
            graphics,
            this.font,
            this.selectedCategory.title(),
            this.layout.imageWidth() / 2,
            17,
            LegacyUiStyle.TEXT
        );
        LegacyText.centered(
            graphics,
            this.font,
            this.playerInventoryTitle,
            this.layout.inventoryPanelX() + this.layout.inventoryPanelWidth() / 2,
            LegacyCraftingLayout.BOTTOM_PANEL_Y + this.layout.bottomPanelTitleY(),
            LegacyUiStyle.TEXT
        );
    }

    private SlotPosition visualPosition(Slot slot) {
        SlotPosition position = this.visualPosition(slot.x, slot.y);
        if (position == null) {
            throw new IllegalArgumentException("Unsupported player inventory slot position");
        }
        return position;
    }

    private SlotPosition visualPosition(int slotX, int slotY) {
        if (slotY == 142 && slotX >= 8 && (slotX - 8) % 18 == 0) {
            int column = (slotX - 8) / 18;
            return column < 9
                ? new SlotPosition(this.layout.inventorySlotsX() + column * 16, LegacyCraftingLayout.HOTBAR_SLOTS_Y)
                : null;
        }
        if (slotY >= 84 && slotY <= 120 && (slotY - 84) % 18 == 0
            && slotX >= 8 && (slotX - 8) % 18 == 0) {
            int column = (slotX - 8) / 18;
            int row = (slotY - 84) / 18;
            return column < 9
                ? new SlotPosition(
                    this.layout.inventorySlotsX() + column * 16,
                    LegacyCraftingLayout.INVENTORY_SLOTS_Y + row * 16
                )
                : null;
        }
        return null;
    }

    private int panelLeft() {
        return (this.width - this.layout.imageWidth()) / 2;
    }

    private int panelTop() {
        return (this.height - this.layout.imageHeight()) / 2;
    }

    private record SlotPosition(int x, int y) {
    }
}
