package dev.tiraaamisuuu.legacycrafting.mixin;

import dev.tiraaamisuuu.legacycrafting.screen.LegacyCraftingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MenuScreens.class)
public abstract class MenuScreensMixin {
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static <T extends AbstractContainerMenu> void legacycrafting$createCraftingScreen(
        MenuType<T> type,
        Minecraft minecraft,
        int containerId,
        Component title,
        CallbackInfo callback
    ) {
        if (type != MenuType.CRAFTING || minecraft.player == null) {
            return;
        }

        CraftingMenu menu = MenuType.CRAFTING.create(containerId, minecraft.player.getInventory());
        LegacyCraftingScreen<CraftingMenu> screen = new LegacyCraftingScreen<>(menu, minecraft.player.getInventory(), title);
        minecraft.player.containerMenu = menu;
        minecraft.gui.setScreen(screen);
        callback.cancel();
    }
}

