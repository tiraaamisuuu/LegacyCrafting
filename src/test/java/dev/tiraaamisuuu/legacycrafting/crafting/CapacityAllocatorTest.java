package dev.tiraaamisuuu.legacycrafting.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class CapacityAllocatorTest {
    @Test
    void combinesAlternativesAndRepeatedIngredients() {
        CapacityAllocator allocator = new CapacityAllocator();
        int[] counts = {13, 4, 12};
        boolean[][] matches = {
            {true, true, true, false, false},
            {true, true, true, false, false},
            {false, false, false, true, true}
        };

        CapacityAllocator.Result allocation = allocator.allocateMaximum(counts, matches);

        assertEquals(5, allocation.craftCount());
        int allocated = java.util.Arrays.stream(allocation.assignments()).flatMapToInt(java.util.Arrays::stream).sum();
        assertEquals(25, allocated);
        assertEquals(13, counts[0], "planning must not mutate source counts");
    }

    @Test
    void rejectsAPlanWhenSharedAlternativesAreInsufficient() {
        CapacityAllocator allocator = new CapacityAllocator();
        int[] counts = {1};
        boolean[][] matches = {{true, true}};

        assertFalse(allocator.allocate(counts, matches, 1).craftable());
        assertTrue(allocator.allocateMaximum(counts, matches).assignments().length == 0);
    }
}
