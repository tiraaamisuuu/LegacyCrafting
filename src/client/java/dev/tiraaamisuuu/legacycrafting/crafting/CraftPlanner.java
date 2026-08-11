package dev.tiraaamisuuu.legacycrafting.crafting;

import dev.tiraaamisuuu.legacycrafting.crafting.CraftPlan.ExpectedRemainder;
import dev.tiraaamisuuu.legacycrafting.crafting.CraftPlan.SourceTransfer;
import dev.tiraaamisuuu.legacycrafting.crafting.IngredientAllocator.Allocation;
import dev.tiraaamisuuu.legacycrafting.crafting.IngredientAllocator.SourceStack;
import dev.tiraaamisuuu.legacycrafting.crafting.IngredientAllocator.SourceType;
import dev.tiraaamisuuu.legacycrafting.recipe.BrowserRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;

public final class CraftPlanner {
    private final IngredientAllocator allocator = new IngredientAllocator();

    public Optional<CraftPlan> plan(BrowserRecipe recipe, LocalPlayer player, AbstractCraftingMenu menu, boolean maximum) {
        List<Ingredient> ingredients = recipe.entry().craftingRequirements().orElse(List.of());
        if (ingredients.isEmpty() || ingredients.size() != recipe.ingredientSlots().size()) {
            return Optional.empty();
        }

        List<SourceStack> sources = snapshotSources(player, menu);
        Allocation allocation = maximum
            ? this.allocator.allocateMaximum(ingredients, sources)
            : this.allocator.allocate(ingredients, sources, 1);
        if (!allocation.craftable()) {
            return Optional.empty();
        }

        List<Integer> targetSlots = targetSlots(recipe, menu);
        List<SourceTransfer> transfers = allocation.transfers().stream()
            .map(transfer -> new SourceTransfer(
                transfer.sourceType(),
                transfer.sourceSlot(),
                transfer.quantity(),
                targetSlots.get(transfer.ingredientIndex())
            ))
            .toList();
        List<ExpectedRemainder> remainders = expectedRemainders(allocation, sources, targetSlots);
        int totalOutput = Math.multiplyExact(allocation.craftCount(), recipe.output().getCount());
        return Optional.of(new CraftPlan(
            recipe.entry(), allocation.craftCount(), transfers, recipe.output().copy(), totalOutput, remainders
        ));
    }

    private static List<SourceStack> snapshotSources(LocalPlayer player, AbstractCraftingMenu menu) {
        List<SourceStack> sources = new ArrayList<>();
        List<ItemStack> inventory = player.getInventory().getNonEquipmentItems();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.get(slot);
            if (Inventory.isUsableForCrafting(stack)) {
                sources.add(new SourceStack(SourceType.INVENTORY, slot, stack));
            }
        }
        for (Slot slot : menu.getInputGridSlots()) {
            ItemStack stack = slot.getItem();
            if (Inventory.isUsableForCrafting(stack)) {
                sources.add(new SourceStack(SourceType.CRAFTING_GRID, slot.index, stack));
            }
        }
        return List.copyOf(sources);
    }

    private static List<Integer> targetSlots(BrowserRecipe recipe, AbstractCraftingMenu menu) {
        if (!(recipe.entry().display() instanceof ShapedCraftingRecipeDisplay)) {
            return java.util.stream.IntStream.range(0, recipe.ingredientSlots().size()).map(index -> index + 1).boxed().toList();
        }

        int offsetX = recipe.recipeWidth() < menu.getGridWidth() / 2.0F
            ? (int)Math.floor(menu.getGridWidth() / 2.0F - recipe.recipeWidth() / 2.0F)
            : 0;
        int offsetY = recipe.recipeHeight() < menu.getGridHeight() / 2.0F
            ? (int)Math.floor(menu.getGridHeight() / 2.0F - recipe.recipeHeight() / 2.0F)
            : 0;
        return recipe.ingredientSlots().stream()
            .map(slot -> 1 + (slot.y() + offsetY) * menu.getGridWidth() + slot.x() + offsetX)
            .toList();
    }

    private static List<ExpectedRemainder> expectedRemainders(
        Allocation allocation,
        List<SourceStack> sources,
        List<Integer> targetSlots
    ) {
        List<ExpectedRemainder> remainders = new ArrayList<>();
        for (IngredientAllocator.Transfer transfer : allocation.transfers()) {
            SourceStack source = sources.stream()
                .filter(candidate -> candidate.type() == transfer.sourceType() && candidate.slot() == transfer.sourceSlot())
                .findFirst()
                .orElseThrow();
            ItemStackTemplate remainderTemplate = source.stack().getItem().getCraftingRemainder();
            if (remainderTemplate != null) {
                remainders.add(new ExpectedRemainder(
                    targetSlots.get(transfer.ingredientIndex()), remainderTemplate.create(), transfer.quantity()
                ));
            }
        }
        return List.copyOf(remainders);
    }
}

