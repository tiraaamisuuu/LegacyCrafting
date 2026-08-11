package dev.tiraaamisuuu.legacycrafting.screen;

import dev.tiraaamisuuu.legacycrafting.client.LegacyCraftingClient;
import net.minecraft.resources.Identifier;

final class LegacySprites {
    static final Identifier SMALL_PANEL = id("tiles/small_panel");
    static final Identifier SQUARE_RECESSED_PANEL = id("tiles/square_recessed_panel");
    static final Identifier HIGH_TAB_LEFT = id("tiles/high_tab_left");
    static final Identifier HIGH_TAB_MIDDLE = id("tiles/high_tab_middle");
    static final Identifier HIGH_TAB_RIGHT = id("tiles/high_tab_right");
    static final Identifier LOW_TAB = id("tiles/low_tab");
    static final Identifier HIGH_VERT_TAB_UP = id("tiles/high_vert_tab_up");
    static final Identifier HIGH_VERT_TAB_MIDDLE = id("tiles/high_vert_tab_middle");
    static final Identifier HIGH_VERT_TAB_DOWN = id("tiles/high_vert_tab_down");
    static final Identifier LOW_VERT_TAB = id("tiles/low_vert_tab");

    static final Identifier ICON_HOLDER = id("container/icon_holder");
    static final Identifier RED_ICON_HOLDER = id("container/red_icon_holder");
    static final Identifier GRAY_ICON_HOLDER = id("container/gray_icon_holder");
    static final Identifier SELECT_ICON_HIGHLIGHT = id("container/select_icon_highlight");
    static final Identifier CRAFTING_SELECTION = id("container/crafting_selection");
    static final Identifier CRAFTING_TWO_SLOT_SELECTION = id("container/crafting_2_slots_selection");
    static final Identifier SMALL_ARROW = id("container/small_arrow");
    static final Identifier ICON_WARNING = id("container/icon_warning");
    static final Identifier SCROLL_LEFT = id("widget/scroll_left");
    static final Identifier SCROLL_RIGHT = id("widget/scroll_right");

    private LegacySprites() {
    }

    static Identifier category(String name) {
        return id("icon/" + name);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(LegacyCraftingClient.MOD_ID, path);
    }
}
