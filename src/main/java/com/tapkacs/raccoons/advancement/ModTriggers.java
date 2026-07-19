package com.tapkacs.raccoons.advancement;

import com.tapkacs.raccoons.Raccoons;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Custom advancement triggers for events with no vanilla equivalent (overfeeding into the chunky
 * variant, taming enough raccoons). Both reuse vanilla's {@link PlayerTrigger} - a trigger with no
 * extra conditions beyond "did this player do the thing" - since the threshold/condition checks
 * already happen in Java before {@code trigger(ServerPlayer)} is called.
 */
public class ModTriggers {

    public static final PlayerTrigger BECAME_CHUNKY = register("became_chunky");
    public static final PlayerTrigger TAMED_ENOUGH_RACCOONS = register("tamed_enough_raccoons");
    public static final PlayerTrigger EQUIPPED_HAT = register("equipped_hat");
    public static final PlayerTrigger STOLE_GOLDEN_APPLE = register("stole_golden_apple");
    public static final PlayerTrigger CALLED_RACCOON = register("called_raccoon");

    private static PlayerTrigger register(String path) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES,
                Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, path), new PlayerTrigger());
    }

    public static void registerModTriggers() {
        Raccoons.LOGGER.info("Registering advancement triggers for " + Raccoons.MOD_ID);
    }
}
