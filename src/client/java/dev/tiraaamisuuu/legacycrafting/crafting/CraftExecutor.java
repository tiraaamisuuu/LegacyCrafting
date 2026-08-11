package dev.tiraaamisuuu.legacycrafting.crafting;

import dev.tiraaamisuuu.legacycrafting.recipe.RecipeView;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Coordinates vanilla recipe placement and result-slot clicks. All inventory
 * changes are standard client predictions followed by server validation.
 */
public final class CraftExecutor {
    private static final int RESPONSE_TIMEOUT_TICKS = 100;
    private static final int MAX_REPEAT_BATCHES = 64;

    private final Minecraft minecraft;
    private final AbstractCraftingMenu menu;
    private final CraftPlanner planner;
    private final Consumer<Component> statusSink;
    private State state = State.IDLE;
    private @Nullable RecipeView recipe;
    private boolean repeat;
    private int deadline;
    private int startingStateId;
    private int outputCountBeforeClick;
    private int batches;

    public CraftExecutor(Minecraft minecraft, AbstractCraftingMenu menu, Consumer<Component> statusSink) {
        this.minecraft = minecraft;
        this.menu = menu;
        this.statusSink = statusSink;
        this.planner = new CraftPlanner();
    }

    public boolean start(RecipeView recipe, boolean repeat) {
        if (this.state != State.IDLE || this.minecraft.player == null || this.minecraft.gameMode == null) {
            return false;
        }
        if (!recipe.craftable()) {
            this.statusSink.accept(Component.translatable("legacycrafting.status.missing"));
            return false;
        }
        if (!this.menu.getCarried().isEmpty()) {
            this.statusSink.accept(Component.translatable("legacycrafting.status.cursor"));
            return false;
        }

        var plan = this.planner.plan(recipe.recipe(), this.minecraft.player, this.menu, repeat);
        if (plan.isEmpty()) {
            this.statusSink.accept(Component.translatable("legacycrafting.status.unavailable"));
            return false;
        }

        this.recipe = recipe;
        this.repeat = repeat;
        this.batches = 0;
        this.statusSink.accept(Component.translatable("legacycrafting.status.placing"));
        this.sendPlacement();
        return true;
    }

    public void tick() {
        if (this.state == State.IDLE) {
            return;
        }
        LocalPlayer player = this.minecraft.player;
        if (player == null || this.minecraft.gameMode == null || player.containerMenu != this.menu || this.recipe == null) {
            this.cancel(Component.translatable("legacycrafting.status.cancelled"));
            return;
        }
        if (--this.deadline <= 0) {
            this.cancel(Component.translatable("legacycrafting.status.timeout"));
            return;
        }

        if (this.state == State.WAITING_FOR_RESULT) {
            ItemStack result = this.menu.getResultSlot().getItem();
            if (this.menu.getStateId() != this.startingStateId
                && !result.isEmpty()
                && ItemStack.isSameItem(result, this.recipe.recipe().output())) {
                if (!canAccept(player, result)) {
                    this.finish(Component.translatable("legacycrafting.status.inventory_full"));
                    return;
                }
                this.outputCountBeforeClick = countMatching(player, this.recipe.recipe().output());
                this.startingStateId = this.menu.getStateId();
                this.minecraft.gameMode.handleContainerInput(
                    this.menu.containerId,
                    this.menu.getResultSlot().index,
                    0,
                    ContainerInput.QUICK_MOVE,
                    player
                );
                this.state = State.WAITING_FOR_CONFIRMATION;
                this.deadline = RESPONSE_TIMEOUT_TICKS;
                this.statusSink.accept(Component.translatable("legacycrafting.status.crafting"));
            }
        } else if (this.state == State.WAITING_FOR_CONFIRMATION && this.menu.getStateId() != this.startingStateId) {
            int outputCount = countMatching(player, this.recipe.recipe().output());
            if (outputCount <= this.outputCountBeforeClick) {
                this.cancel(Component.translatable("legacycrafting.status.rejected"));
                return;
            }

            if (!this.repeat) {
                this.finish(Component.translatable("legacycrafting.status.complete"));
                return;
            }
            if (++this.batches >= MAX_REPEAT_BATCHES || !canStillCraft(player, this.recipe)) {
                this.finish(Component.translatable("legacycrafting.status.complete_many"));
                return;
            }
            ItemStack result = this.menu.getResultSlot().getItem();
            if (!result.isEmpty() && !canAccept(player, result)) {
                this.finish(Component.translatable("legacycrafting.status.inventory_full"));
                return;
            }
            this.sendPlacement();
        }
    }

    public boolean isBusy() {
        return this.state != State.IDLE;
    }

    public void cancel(Component message) {
        this.state = State.IDLE;
        this.recipe = null;
        this.statusSink.accept(message);
    }

    private void sendPlacement() {
        Objects.requireNonNull(this.recipe);
        this.startingStateId = this.menu.getStateId();
        this.minecraft.gameMode.handlePlaceRecipe(this.menu.containerId, this.recipe.recipe().entry().id(), this.repeat);
        this.state = State.WAITING_FOR_RESULT;
        this.deadline = RESPONSE_TIMEOUT_TICKS;
    }

    private void finish(Component message) {
        this.state = State.IDLE;
        this.recipe = null;
        this.statusSink.accept(message);
    }

    private boolean canStillCraft(LocalPlayer player, RecipeView recipe) {
        StackedItemContents contents = new StackedItemContents();
        player.getInventory().fillStackedContents(contents);
        this.menu.fillCraftSlotsStackedContents(contents);
        return recipe.recipe().entry().canCraft(contents);
    }

    private static int countMatching(LocalPlayer player, ItemStack expected) {
        return player.getInventory().getNonEquipmentItems().stream()
            .filter(stack -> ItemStack.isSameItem(stack, expected))
            .mapToInt(ItemStack::getCount)
            .sum();
    }

    private static boolean canAccept(LocalPlayer player, ItemStack result) {
        int capacity = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.isEmpty()) {
                capacity += result.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(stack, result)) {
                capacity += stack.getMaxStackSize() - stack.getCount();
            }
            if (capacity >= result.getCount()) {
                return true;
            }
        }
        return false;
    }

    private enum State {
        IDLE,
        WAITING_FOR_RESULT,
        WAITING_FOR_CONFIRMATION
    }
}
