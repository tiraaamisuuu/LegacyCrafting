package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe;
import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe.IngredientSlot;
import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
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
    private static final int SLOT_SIZE = 20;
    private @Nullable RecipeView recipeView;
    private Component status = Component.translatable("legacycrafting.status.ready");

    public RecipeDetailsWidget(int x, int y, int width, int height) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.active = false;
    }

    public void setRecipe(@Nullable RecipeView recipeView) {
        this.recipeView = recipeView;
    }

    public void setStatus(Component status) {
        this.status = status;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0xFF1B202A);
        graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xFF596276);
        if (this.recipeView == null) {
            graphics.centeredText(minecraft.font, Component.translatable("legacycrafting.recipe.none"), this.getX() + this.getWidth() / 2, this.getY() + 28, 0xFF9BA4B5);
            return;
        }

        BrowserRecipe recipe = this.recipeView.recipe();
        int outputX = this.getX() + 8;
        int outputY = this.getY() + 8;
        graphics.pose().pushMatrix();
        graphics.pose().translate(outputX, outputY);
        graphics.pose().scale(2.0F, 2.0F);
        graphics.item(recipe.output(), 0, 0);
        graphics.itemDecorations(minecraft.font, recipe.output(), 0, 0);
        graphics.pose().popMatrix();
        graphics.text(minecraft.font, recipe.output().getHoverName(), outputX + 38, outputY, this.recipeView.craftable() ? 0xFFFFFFFF : 0xFF8E96A5, false);
        graphics.text(minecraft.font, Component.translatable("legacycrafting.output_count", recipe.output().getCount()), outputX + 38, outputY + 11, 0xFFBBC3D1, false);

        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        int gridX = this.getX() + 8;
        int gridY = this.getY() + 34;
        for (IngredientSlot ingredientSlot : recipe.ingredientSlots()) {
            int slotX = gridX + ingredientSlot.x() * SLOT_SIZE;
            int slotY = gridY + ingredientSlot.y() * SLOT_SIZE;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF303744);
            List<ItemStack> alternatives = ingredientSlot.display().resolveForStacks(context);
            if (!alternatives.isEmpty()) {
                int cycle = (int)((System.currentTimeMillis() / 1000L) % alternatives.size());
                ItemStack stack = alternatives.get(cycle);
                graphics.item(stack, slotX + 1, slotY + 1);
                if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                    List<Component> tooltip = new java.util.ArrayList<>(net.minecraft.client.gui.screens.Screen.getTooltipFromItem(minecraft, stack));
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
        graphics.textWithWordWrap(minecraft.font, this.status, this.getX() + 75, this.getY() + 43, this.getWidth() - 83, 0xFFE7B85A, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (this.recipeView != null) {
            output.add(NarratedElementType.TITLE, this.recipeView.recipe().output().getHoverName());
        }
    }
}
