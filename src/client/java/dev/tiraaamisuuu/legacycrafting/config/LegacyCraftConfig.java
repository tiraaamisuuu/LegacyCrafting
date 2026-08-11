package dev.tiraaamisuuu.legacycrafting.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.tiraaamisuuu.legacycrafting.client.LegacyCraftingClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;

public final class LegacyCraftConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("legacycrafting.json");
    private static LegacyCraftConfig instance = new LegacyCraftConfig();

    private boolean enabled = true;

    private LegacyCraftConfig() {
    }

    public static LegacyCraftConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.isRegularFile(CONFIG_PATH)) {
            instance.save();
            return;
        }
        try {
            LegacyCraftConfig loaded = GSON.fromJson(Files.readString(CONFIG_PATH, StandardCharsets.UTF_8), LegacyCraftConfig.class);
            if (loaded != null) {
                instance = loaded;
            }
        } catch (IOException | RuntimeException exception) {
            LegacyCraftingClient.LOGGER.warn("Could not read {}. Using defaults.", CONFIG_PATH, exception);
        }
    }

    public boolean enabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.save();
    }

    private void save() {
        Path temporary = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(temporary, GSON.toJson(this), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(temporary, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LegacyCraftingClient.LOGGER.warn("Could not save {}.", CONFIG_PATH, exception);
        }
    }
}

