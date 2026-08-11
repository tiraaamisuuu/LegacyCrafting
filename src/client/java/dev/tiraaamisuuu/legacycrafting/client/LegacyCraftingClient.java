package dev.tiraaamisuuu.legacycrafting.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LegacyCraftingClient implements ClientModInitializer {
    public static final String MOD_ID = "legacycrafting";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("LegacyCrafting initialized");
    }
}

