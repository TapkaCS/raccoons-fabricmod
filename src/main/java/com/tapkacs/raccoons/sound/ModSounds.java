package com.tapkacs.raccoons.sound;

import com.tapkacs.raccoons.Raccoons;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    public static final SoundEvent RACCOON_IDLE = register("raccoon.idle");
    public static final SoundEvent RACCOON_HURT = register("raccoon.hurt");

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerModSounds() {
        Raccoons.LOGGER.info("Registering sounds for " + Raccoons.MOD_ID);
    }
}
