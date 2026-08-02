package com.tapkacs.raccoons.advancement;

import com.tapkacs.raccoons.Raccoons;
import com.tapkacs.raccoons.entity.RaccoonEntity;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Custom advancement triggers for events with no vanilla equivalent (overfeeding into the chunky
 * variant, taming enough raccoons). Most reuse vanilla's {@link PlayerTrigger} - a trigger with no
 * extra conditions beyond "did this player do the thing" - since the threshold/condition checks
 * already happen in Java before {@code trigger(ServerPlayer)} is called.
 */
public class ModTriggers {

    public static final PlayerTrigger BECAME_CHUNKY = register("became_chunky");
    public static final PlayerTrigger TAMED_ENOUGH_RACCOONS = register("tamed_enough_raccoons");
    public static final PlayerTrigger EQUIPPED_HAT = register("equipped_hat");
    public static final PlayerTrigger STOLE_GOLDEN_APPLE = register("stole_golden_apple");
    public static final PlayerTrigger CALLED_RACCOON = register("called_raccoon");
    public static final PlayerTrigger WASHED_CANDY = register("washed_candy");

    /**
     * One trigger per color variant (chunky is a separate, orthogonal flag and has no entry here) -
     * the "tame every color" advancement requires all of these as separate criteria. NORMAL is
     * skipped: it's a legacy save-data alias for the retired classic texture, not a spawnable color.
     */
    public static final Map<RaccoonEntity.ColorVariant, PlayerTrigger> TAMED_COLOR_VARIANT = registerColorVariantTriggers();

    private static Map<RaccoonEntity.ColorVariant, PlayerTrigger> registerColorVariantTriggers() {
        Map<RaccoonEntity.ColorVariant, PlayerTrigger> map = new EnumMap<>(RaccoonEntity.ColorVariant.class);
        for (RaccoonEntity.ColorVariant variant : RaccoonEntity.ColorVariant.values()) {
            if (variant == RaccoonEntity.ColorVariant.NORMAL) {
                continue;
            }
            map.put(variant, register("tamed_color_" + variant.name().toLowerCase(Locale.ROOT)));
        }
        return map;
    }

    private static PlayerTrigger register(String path) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES,
                Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, path), new PlayerTrigger());
    }

    public static void registerModTriggers() {
        Raccoons.LOGGER.info("Registering advancement triggers for " + Raccoons.MOD_ID);
    }
}
