package dev.tiraaamisuuu.legacycrafting.screen;

record LegacyCraftingLayout(
    int imageWidth,
    int imageHeight,
    int maxCategoryTabs,
    int maxRecipeButtons,
    int recipeButtonsX,
    int craftingPanelWidth,
    int inventoryPanelX,
    int inventoryPanelWidth,
    int inventorySlotsX,
    int craftingGridX,
    int craftingGridY,
    int craftingArrowX,
    int craftingResultX,
    int bottomPanelTitleY
) {
    static final int BOTTOM_PANEL_Y = 103;
    static final int BOTTOM_PANEL_HEIGHT = 105;
    static final int CRAFTING_PANEL_X = 9;
    static final int RECIPE_BUTTONS_Y = 38;
    static final int INVENTORY_SLOTS_Y = 133;
    static final int HOTBAR_SLOTS_Y = 186;

    static LegacyCraftingLayout forGrid(int gridWidth) {
        if (gridWidth == 2) {
            return new LegacyCraftingLayout(
                300,
                215,
                6,
                10,
                16,
                125,
                138,
                153,
                144,
                9,
                41,
                61,
                84,
                5
            );
        }
        return new LegacyCraftingLayout(
            348,
            215,
            7,
            12,
            13,
            163,
            176,
            163,
            186,
            12,
            30,
            88,
            115,
            11
        );
    }

    int categoryTabsX() {
        return -(this.maxCategoryTabs * CategoryTabsWidget.TAB_WIDTH - this.imageWidth) / 2;
    }
}
