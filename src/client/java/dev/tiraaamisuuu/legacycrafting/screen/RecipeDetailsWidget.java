package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe;
import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe.IngredientSlot;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jspecify.annotations.Nullable;

public final class RecipeDetailsWidget extends AbstractWidget {
    private static final int INGREDIENT_SLOT_SIZE = 23;
    private final int craftingGridSize;
    private @Nullable RecipeView recipeView;
    private Component status = Component.translatable("legacycrafting.status.ready");

    public RecipeDetailsWidget(int x, int y, int width, int height, int craftingGridSize) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.craftingGridSize = craftingGridSize;
        this.active = false;
    }

    public void setRecipe(@Nullable RecipeView recipeView) {
        this.recipeView = recipeView;
        if (recipeView != null) {
            this.status = Component.translatable(
                recipeView.craftable() ? "legacycrafting.status.ready" : "legacycrafting.status.missing"
            );
        }
    }

    public void setStatus(Component status) {
        this.status = status;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        LegacyUiStyle.insetPanel(graphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), LegacyUiStyle.PANEL_DARK);
        if (this.recipeView == null) {
            graphics.centeredText(
                minecraft.font,
                Component.translatable("legacycrafting.recipe.none"),
                this.getX() + this.getWidth() / 2,
                this.getY() + 12,
                LegacyUiStyle.MUTED_TEXT
            );
            return;
        }

        BrowserRecipe recipe = this.recipeView.recipe();
        graphics.centeredText(
            minecraft.font,
            recipe.output().getHoverName(),
            this.getX() + this.getWidth() / 2,
            this.getY() + 9,
            LegacyUiStyle.TEXT
        );

        int gridWidth = this.craftingGridSize * INGREDIENT_SLOT_SIZE;
        int gridX = this.getX() + 10 + (72 - gridWidth) / 2;
        int gridY = this.getY() + 28;
        for (int y = 0; y < this.craftingGridSize; y++) {
            for (int x = 0; x < this.craftingGridSize; x++) {
                LegacyUiStyle.slot(
                    graphics,
                    gridX + x * INGREDIENT_SLOT_SIZE,
                    gridY + y * INGREDIENT_SLOT_SIZE,
                    INGREDIENT_SLOT_SIZE,
                    false,
                    false
                );
            }
        }

        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        for (IngredientSlot ingredientSlot : recipe.ingredientSlots()) {
            int slotX = gridX + ingredientSlot.x() * INGREDIENT_SLOT_SIZE;
            int slotY = gridY + ingredientSlot.y() * INGREDIENT_SLOT_SIZE;
            boolean missing = this.isMissing(ingredientSlot);
            LegacyUiStyle.slot(graphics, slotX, slotY, INGREDIENT_SLOT_SIZE, false, missing);
            List<ItemStack> alternatives = ingredientSlot.display().resolveForStacks(context);
            if (!alternatives.isEmpty()) {
                int cycle = (int)((System.currentTimeMillis() / 1000L) % alternatives.size());
                ItemStack stack = alternatives.get(cycle);
                graphics.item(stack, slotX + 3, slotY + 3);
                if (missing) {
                    graphics.text(minecraft.font, "!", slotX + 2, slotY + 1, 0xFFFFFF00, true);
                }
                if (contains(mouseX, mouseY, slotX, slotY, INGREDIENT_SLOT_SIZE)) {
                    List<Component> tooltip = new ArrayList<>(net.minecraft.client.gui.screens.Screen.getTooltipFromItem(minecraft, stack));
                    this.recipeView.ingredientSummaries().stream()
                        .filter(summary -> ingredientSlot.ingredient() != null && summary.ingredient().equals(ingredientSlot.ingredient()))
                        .findFirst()
                        .ifPresent(summary -> tooltip.add(Component.translatable(
                            "legacycrafting.ingredient_count", summary.required(), summary.available()
                        )));
                    graphics.setComponentTooltipForNextFrame(minecraft.font, tooltip, mouseX, mouseY);
                }
            }
        }

        int arrowX = this.getX() + 91;
        int arrowY = gridY + gridWidth / 2 - 3;
        graphics.text(minecraft.font, "→", arrowX, arrowY, 0xFF8A8A8A, false);

        int outputSize = 36;
        int outputX = this.getX() + 136;
        int outputY = gridY + Math.max(0, (gridWidth - outputSize) / 2);
        LegacyUiStyle.slot(graphics, outputX, outputY, outputSize, false, !this.recipeView.craftable());
        graphics.pose().pushMatrix();
        graphics.pose().translate(outputX + 4, outputY + 4);
        graphics.pose().scale(1.75F, 1.75F);
        graphics.item(recipe.output(), 0, 0);
        graphics.pose().popMatrix();
        graphics.itemDecorations(minecraft.font, recipe.output(), outputX + 10, outputY + 10);
        if (contains(mouseX, mouseY, outputX, outputY, outputSize)) {
            graphics.setTooltipForNextFrame(minecraft.font, recipe.output(), mouseX, mouseY);
        }

        graphics.centeredText(
            minecraft.font,
            Component.translatable("legacycrafting.output_count", recipe.output().getCount()),
            outputX + outputSize / 2,
            outputY + outputSize + 4,
            LegacyUiStyle.MUTED_TEXT
        );
        int statusColor = this.recipeView.craftable() ? 0xFF315B37 : 0xFF8A2020;
        graphics.centeredText(
            minecraft.font,
            this.status,
            this.getX() + this.getWidth() / 2,
            this.getBottom() - 14,
            statusColor
        );
    }

    private boolean isMissing(IngredientSlot ingredientSlot) {
        if (ingredientSlot.ingredient() == null || this.recipeView == null) {
            return false;
        }
        return this.recipeView.ingredientSummaries().stream()
            .filter(summary -> summary.ingredient().equals(ingredientSlot.ingredient()))
            .anyMatch(summary -> summary.available() < summary.required());
    }

    private static boolean contains(int mouseX, int mouseY, int x, int y, int size) {
        return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (this.recipeView != null) {
            output.add(NarratedElementType.TITLE, this.recipeView.recipe().output().getHoverName());
        }
    }
}
