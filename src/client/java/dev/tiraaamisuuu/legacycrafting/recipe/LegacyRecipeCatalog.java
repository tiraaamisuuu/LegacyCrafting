package dev.tiraaamisuuu.legacycrafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class LegacyRecipeCatalog {
    private static final String RESOURCE = "/assets/legacycrafting/recipes/legacy_groups.json";
    private static final LegacyRecipeCatalog INSTANCE = load();
    private final Map<String, Listing> byOutputPath;

    private LegacyRecipeCatalog(Map<String, Listing> byOutputPath) {
        this.byOutputPath = Map.copyOf(byOutputPath);
    }

    static LegacyRecipeCatalog instance() {
        return INSTANCE;
    }

    Optional<Listing> find(String outputPath) {
        return Optional.ofNullable(this.byOutputPath.get(outputPath));
    }

    private static LegacyRecipeCatalog load() {
        Map<String, Listing> listings = new HashMap<>();
        try (InputStream stream = LegacyRecipeCatalog.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                return new LegacyRecipeCatalog(Map.of());
            }
            JsonArray categories = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
            int groupOrder = 0;
            for (JsonElement categoryElement : categories) {
                JsonObject categoryObject = categoryElement.getAsJsonObject();
                LegacyCategory category = category(categoryObject.get("id").getAsString());
                if (category == null) {
                    continue;
                }
                for (JsonElement listingElement : categoryObject.getAsJsonArray("listing")) {
                    String group;
                    JsonArray recipes = new JsonArray();
                    if (listingElement.isJsonPrimitive()) {
                        group = listingElement.getAsString();
                        recipes.add(listingElement);
                    } else {
                        JsonObject listingObject = listingElement.getAsJsonObject();
                        group = listingObject.has("group") ? listingObject.get("group").getAsString() : "entry_" + groupOrder;
                        if (listingObject.has("recipes")) {
                            recipes = listingObject.getAsJsonArray("recipes");
                        } else {
                            recipes.add(listingElement);
                        }
                    }
                    int variantOrder = 0;
                    for (JsonElement recipe : recipes) {
                        String path = outputPath(recipe);
                        if (path != null && !path.startsWith("#")) {
                            listings.putIfAbsent(path, new Listing(category, category.name() + ":" + group, groupOrder, variantOrder));
                        }
                        variantOrder++;
                    }
                    groupOrder++;
                }
            }
        } catch (IOException | RuntimeException ignored) {
            return new LegacyRecipeCatalog(Map.of());
        }
        return new LegacyRecipeCatalog(listings);
    }

    private static String outputPath(JsonElement recipe) {
        if (recipe.isJsonPrimitive()) {
            return stripNamespace(recipe.getAsString());
        }
        JsonObject object = recipe.getAsJsonObject();
        if (!object.has("type") || !"item_id".equals(object.get("type").getAsString())) {
            return null;
        }
        JsonElement value = object.get("value");
        if (!value.isJsonObject() || !value.getAsJsonObject().has("id")) {
            return null;
        }
        return stripNamespace(value.getAsJsonObject().get("id").getAsString());
    }

    private static String stripNamespace(String value) {
        int separator = value.indexOf(':');
        return separator < 0 ? value : value.substring(separator + 1);
    }

    private static LegacyCategory category(String id) {
        return switch (id) {
            case "structures" -> LegacyCategory.BUILDING;
            case "tools" -> LegacyCategory.TOOLS;
            case "food" -> LegacyCategory.FOOD;
            case "armour" -> LegacyCategory.ARMOR;
            case "mechanisms" -> LegacyCategory.REDSTONE;
            case "transport" -> LegacyCategory.TRANSPORTATION;
            case "decoration" -> LegacyCategory.DECORATIONS;
            default -> null;
        };
    }

    record Listing(LegacyCategory category, String group, int groupOrder, int variantOrder) {
    }
}
