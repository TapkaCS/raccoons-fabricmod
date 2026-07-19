package com.tapkacs.raccoons.stat;

import com.tapkacs.raccoons.Raccoons;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModStats {

    public static final Identifier TAMED_RACCOONS = Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "tamed_raccoons");

    public static void registerModStats() {
        Registry.register(BuiltInRegistries.CUSTOM_STAT, TAMED_RACCOONS, TAMED_RACCOONS);
        Raccoons.LOGGER.info("Registering stats for " + Raccoons.MOD_ID);
    }
}
