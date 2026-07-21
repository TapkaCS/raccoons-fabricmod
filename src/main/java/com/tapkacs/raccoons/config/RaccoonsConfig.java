package com.tapkacs.raccoons.config;

/**
 * Plain data holder serialized to/from {@code config/raccoons.json} by {@link ModConfigManager}.
 * Field names double as the JSON keys - Gson reads/writes them directly, no annotations needed.
 * Loaded once at mod init; editing the file requires a restart to take effect.
 */
public class RaccoonsConfig {

    public Theft theft = new Theft();
    public Stashing stashing = new Stashing();
    public Spawning spawning = new Spawning();
    public Behavior behavior = new Behavior();

    public static class Theft {
        public boolean enabled = true;
        /** 1-in-N chance per AI tick that an idle raccoon starts a chest/barrel/composter raid. */
        public int chestStealChance = 400;
        /** 1-in-N chance while part of a wild nighttime gang (lower = more frequent raiding). */
        public int nightGangStealChance = 30;
    }

    public static class Stashing {
        /** Whether wild raccoons pick up dropped items off the ground to hoard in a stash. */
        public boolean enabled = true;
        /** 1-in-N chance per AI tick that an idle wild raccoon looks for a nearby item to grab. */
        public int stashChance = 600;
    }

    public static class Spawning {
        public int forestTaigaWeight = 20;
        public int forestTaigaMinGroupSize = 3;
        public int forestTaigaMaxGroupSize = 5;

        public int villageBiomeWeight = 8;
        public int villageBiomeMinGroupSize = 3;
        public int villageBiomeMaxGroupSize = 5;

        public int mountainBadlandsWeight = 8;
        public int mountainBadlandsMinGroupSize = 2;
        public int mountainBadlandsMaxGroupSize = 4;

        /** Chance (0.0-1.0) a freshly spawned raccoon rolls chunky. */
        public float chunkySpawnChance = 0.1f;
        /** Chance (0.0-1.0) a freshly spawned raccoon rolls albino. */
        public float albinoSpawnChance = 0.02f;
        /** Chance (0.0-1.0) a freshly spawned raccoon rolls melanistic. */
        public float melanisticSpawnChance = 0.02f;
    }

    public static class Behavior {
        /** How many non-taming food items (heal-only) flip a raccoon chunky. */
        public int overfeedThreshold = 32;
        /** How many raccoons a player must tame before the "tamed enough" advancement fires. */
        public int tamedRaccoonsAchievementThreshold = 50;
    }
}
