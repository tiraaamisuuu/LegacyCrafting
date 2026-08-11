package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe;
import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe.IngredientSlot;
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
    private @Nullable BrowserRecipe recipe;

    public RecipeDetailsWidget(int x, int y, int width, int height) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.active = false;
    }

    public void setRecipe(@Nullable BrowserRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0xFF1B202A);
        graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0xFF596276);
        if (this.recipe == null) {
            graphics.centeredText(minecraft.font, Component.translatable("legacycrafting.recipe.none"), this.getX() + this.getWidth() / 2, this.getY() + 28, 0xFF9BA4B5);
            return;
        }

        int outputX = this.getX() + 8;
        int outputY = this.getY() + 8;
        graphics.item(this.recipe.output(), outputX, outputY);
        graphics.itemDecorations(minecraft.font, this.recipe.output(), outputX, outputY);
        graphics.text(minecraft.font, this.recipe.output().getHoverName(), outputX + 22, outputY, 0xFFFFFFFF, false);
        graphics.text(minecraft.font, Component.translatable("legacycrafting.output_count", this.recipe.output().getCount()), outputX + 22, outputY + 11, 0xFFBBC3D1, false);

        ContextMap context = SlotDisplayContext.fromLevel(minecraft.level);
        int gridX = this.getX() + 8;
        int gridY = this.getY() + 34;
        for (IngredientSlot ingredientSlot : this.recipe.ingredientSlots()) {
            int slotX = gridX + ingredientSlot.x() * SLOT_SIZE;
            int slotY = gridY + ingredientSlot.y() * SLOT_SIZE;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF303744);
            List<ItemStack> alternatives = ingredientSlot.display().resolveForStacks(context);
            if (!alternatives.isEmpty()) {
                int cycle = (int)((System.currentTimeMillis() / 1000L) % alternatives.size());
                ItemStack stack = alternatives.get(cycle);
                graphics.item(stack, slotX + 1, slotY + 1);
                if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                    graphics.setTooltipForNextFrame(minecraft.font, stack, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (this.recipe != null) {
            output.add(NarratedElementType.TITLE, this.recipe.output().getHoverName());
        }
    }
}

