package dev.tiraaamisuuu.legacycrafting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class LegacyUiSounds {
    private LegacyUiSounds() {
    }

    public static void play(Cue cue) {
        float pitch = cue.randomPitch
            ? cue.pitch + (Minecraft.getInstance().player == null
                ? 0.0F
                : (Minecraft.getInstance().player.getRandom().nextFloat() - 0.5F) / 10.0F)
            : cue.pitch;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(cue.sound, pitch, cue.volume));
    }

    public enum Cue {
        FOCUS("ui.focus", 1.0F, 1.0F, true),
        SCROLL("ui.scroll", 1.0F, 1.0F, false),
        ACTION("ui.action", 1.0F, 1.0F, false),
        BACK("ui.back", 1.0F, 1.0F, false),
        CRAFT_FAIL("ui.craft_fail", 1.0F, 1.0F, false),
        CRAFT_SUCCESS("ui.craft_success", 1.0F, 1.0F, false);

        private final SoundEvent sound;
        private final float pitch;
        private final float volume;
        private final boolean randomPitch;

        Cue(String path, float pitch, float volume, boolean randomPitch) {
            this.sound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(LegacyCraftingClient.MOD_ID, path));
            this.pitch = pitch;
            this.volume = volume;
            this.randomPitch = randomPitch;
        }
    }
}
