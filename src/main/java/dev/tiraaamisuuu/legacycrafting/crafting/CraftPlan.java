package dev.tiraaamisuuu.legacycrafting.crafting;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;

public record CraftPlan(
    RecipeDisplayEntry recipe,
    int craftCount,
    List<SourceTransfer> sourceTransfers,
    ItemStack result,
    int totalOutputCount,
    List<ExpectedRemainder> expectedRemainders
) {
    public record SourceTransfer(
        IngredientAllocator.SourceType sourceType,
        int sourceSlot,
        int quantity,
        int targetCraftingSlot
    ) {
    }

    public record ExpectedRemainder(int targetCraftingSlot, ItemStack stack, int quantity) {
    }
}

