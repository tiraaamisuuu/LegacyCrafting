package dev.tiraaamisuuu.legacycrafting.mixin;

import dev.tiraaamisuuu.legacycrafting.config.LegacyCraftConfig;
import dev.tiraaamisuuu.legacycrafting.screen.LegacyCraftingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private @Nullable Screen legacycrafting$replaceCraftingScreens(@Nullable Screen screen) {
        if (!LegacyCraftConfig.get().enabled() || this.minecraft.player == null || screen instanceof LegacyCraftingScreen<?>) {
            return screen;
        }
        if (screen instanceof CraftingScreen craftingScreen) {
            return new LegacyCraftingScreen<>(
                craftingScreen.getMenu(), this.minecraft.player.getInventory(), craftingScreen.getTitle()
            );
        }
        if (screen instanceof InventoryScreen inventoryScreen) {
            return new LegacyCraftingScreen<>(
                inventoryScreen.getMenu(), this.minecraft.player.getInventory(), inventoryScreen.getTitle()
            );
        }
        return screen;
    }
}

