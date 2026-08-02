package com.tapkacs.raccoons.entity;

import com.tapkacs.raccoons.Raccoons;
import com.tapkacs.raccoons.config.RaccoonsConfig;
import com.tapkacs.raccoons.config.ModConfigManager;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import java.lang.reflect.Method;

public class ModEntityTypes {

    public static final EntityType<RaccoonEntity> RACCOON = register(
            "raccoon",
            EntityType.Builder.<RaccoonEntity>of(RaccoonEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.7f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void registerModEntityTypes() {
        Raccoons.LOGGER.info("Registering entities for " + Raccoons.MOD_ID);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(RACCOON, RaccoonEntity.createAttributes());
    }

    public static void registerSpawns() {
        // SpawnPlacements.register is private in vanilla; there's no public Fabric API
        // wrapper for it, so we reach it via reflection instead of an access widener.
        try {
            Method register = SpawnPlacements.class.getDeclaredMethod("register",
                    EntityType.class, SpawnPlacementType.class, Heightmap.Types.class, SpawnPlacements.SpawnPredicate.class);
            register.setAccessible(true);
            register.invoke(null, RACCOON, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (SpawnPlacements.SpawnPredicate<RaccoonEntity>) Animal::checkAnimalSpawnRules);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register raccoon spawn placement", e);
        }

        RaccoonsConfig.Spawning config = ModConfigManager.get().spawning;

        // Any forest/taiga - their main habitat, spawning in gangs.
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(BiomeTags.IS_FOREST).or(BiomeSelectors.tag(BiomeTags.IS_TAIGA)),
                MobCategory.CREATURE, RACCOON, config.forestTaigaWeight,
                config.forestTaigaMinGroupSize, config.forestTaigaMaxGroupSize);

        // Also the common village biomes (lower weight - they're visitors there, not native), so
        // gangs reliably show up near villages to raid chests at night.
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.SNOWY_PLAINS, Biomes.DESERT, Biomes.SAVANNA),
                MobCategory.CREATURE, RACCOON, config.villageBiomeWeight,
                config.villageBiomeMinGroupSize, config.villageBiomeMaxGroupSize);

        // Mountains and badlands, so the gray/taupe coat variants actually occur in the wild
        // (see RaccoonEntity#pickNaturalVariantFor for the coat-per-biome mapping).
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN).or(BiomeSelectors.tag(BiomeTags.IS_BADLANDS)),
                MobCategory.CREATURE, RACCOON, config.mountainBadlandsWeight,
                config.mountainBadlandsMinGroupSize, config.mountainBadlandsMaxGroupSize);
    }
}