package dev.tiraaamisuuu.legacycrafting.crafting;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Calculates ingredient usage from immutable stack snapshots. It never mutates
 * the supplied stacks or the player's inventory.
 */
public final class IngredientAllocator {
    private final CapacityAllocator capacityAllocator = new CapacityAllocator();

    public Allocation allocateMaximum(List<Ingredient> ingredients, List<SourceStack> sources) {
        CapacityAllocator.Result result = this.capacityAllocator.allocateMaximum(counts(sources), matches(ingredients, sources));
        return toAllocation(result, sources);
    }

    public Allocation allocate(List<Ingredient> ingredients, List<SourceStack> sources, int craftCount) {
        if (craftCount <= 0 || ingredients.isEmpty()) {
            return Allocation.EMPTY;
        }

        CapacityAllocator.Result result = this.capacityAllocator.allocate(counts(sources), matches(ingredients, sources), craftCount);
        return toAllocation(result, sources);
    }

    private static Allocation toAllocation(CapacityAllocator.Result result, List<SourceStack> sources) {
        if (!result.craftable()) {
            return Allocation.EMPTY;
        }
        List<Transfer> transfers = new java.util.ArrayList<>();
        for (int sourceIndex = 0; sourceIndex < sources.size(); sourceIndex++) {
            for (int ingredientIndex = 0; ingredientIndex < result.assignments()[sourceIndex].length; ingredientIndex++) {
                int quantity = result.assignments()[sourceIndex][ingredientIndex];
                if (quantity > 0) {
                    SourceStack source = sources.get(sourceIndex);
                    transfers.add(new Transfer(source.type(), source.slot(), ingredientIndex, quantity));
                }
            }
        }
        return new Allocation(result.craftCount(), List.copyOf(transfers));
    }

    private static int[] counts(List<SourceStack> sources) {
        return sources.stream().mapToInt(source -> source.stack().getCount()).toArray();
    }

    private static boolean[][] matches(List<Ingredient> ingredients, List<SourceStack> sources) {
        boolean[][] matches = new boolean[sources.size()][ingredients.size()];
        for (int sourceIndex = 0; sourceIndex < sources.size(); sourceIndex++) {
            for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
                matches[sourceIndex][ingredientIndex] = ingredients.get(ingredientIndex).test(sources.get(sourceIndex).stack());
            }
        }
        return matches;
    }

    public enum SourceType {
        INVENTORY,
        CRAFTING_GRID
    }

    public record SourceStack(SourceType type, int slot, ItemStack stack) {
        public SourceStack {
            stack = stack.copy();
        }
    }

    public record Transfer(SourceType sourceType, int sourceSlot, int ingredientIndex, int quantity) {
    }

    public record Allocation(int craftCount, List<Transfer> transfers) {
        public static final Allocation EMPTY = new Allocation(0, List.of());

        public boolean craftable() {
            return this.craftCount > 0;
        }
    }

}
