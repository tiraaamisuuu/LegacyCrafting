package dev.tiraaamisuuu.legacycrafting.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

final class LegacyItemRenderer {
    private static final float BASE_ITEM_SIZE = 18.0F;
    private static final float PADDED_ITEM_SCALE = 14.0F / 16.0F;

    private LegacyItemRenderer() {
    }

    static void render(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int holderSize, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        float holderScale = holderSize / BASE_ITEM_SIZE;
        boolean padded = usesSlotPadding(stack);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(holderScale, holderScale);
        if (padded) {
            graphics.pose().translate(1.0F, 1.0F);
            graphics.pose().scale(PADDED_ITEM_SCALE, PADDED_ITEM_SCALE);
        }
        graphics.fakeItem(stack, 0, 0, seed);
        graphics.pose().popMatrix();
    }

    private static boolean usesSlotPadding(ItemStack stack) {
        return !stack.has(DataComponents.TOOL)
            && !stack.has(DataComponents.WEAPON)
            && !stack.has(DataComponents.PIERCING_WEAPON)
            && !stack.has(DataComponents.KINETIC_WEAPON);
    }
}
