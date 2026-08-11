package dev.tiraaamisuuu.legacycrafting.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.tiraaamisuuu.legacycrafting.config.LegacyCraftConfig;
import dev.tiraaamisuuu.legacycrafting.screen.LegacyCraftingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LegacyCraftingClient implements ClientModInitializer {
    public static final String MOD_ID = "legacycrafting";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        LegacyCraftConfig.load();
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.legacycrafting.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_L,
            KeyMapping.Category.INVENTORY
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                toggleInterface(client);
            }
        });
        LOGGER.info("LegacyCrafting initialized");
    }

    private static void toggleInterface(Minecraft minecraft) {
        LegacyCraftConfig config = LegacyCraftConfig.get();
        config.setEnabled(!config.enabled());
        Screen screen = minecraft.gui.screen();
        if (minecraft.player == null || screen == null) {
            return;
        }

        if (!config.enabled() && screen instanceof LegacyCraftingScreen<?> legacyScreen) {
            legacyScreen.openVanillaScreen();
        } else if (config.enabled() && screen instanceof CraftingScreen craftingScreen) {
            CraftingMenu menu = craftingScreen.getMenu();
            minecraft.gui.setScreen(new LegacyCraftingScreen<>(menu, minecraft.player.getInventory(), craftingScreen.getTitle()));
        } else if (config.enabled() && screen instanceof InventoryScreen inventoryScreen) {
            InventoryMenu menu = inventoryScreen.getMenu();
            minecraft.gui.setScreen(new LegacyCraftingScreen<>(menu, minecraft.player.getInventory(), inventoryScreen.getTitle()));
        }
    }
}
