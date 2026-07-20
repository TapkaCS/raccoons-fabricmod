package com.tapkacs.raccoons.block;

import com.tapkacs.raccoons.Raccoons;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    public static final Block RACCOON_STASH = register("raccoon_stash",
            properties -> new RaccoonStashBlock(properties.mapColor(MapColor.WOOD).strength(2.0f).sound(net.minecraft.world.level.block.SoundType.WOOD).noOcclusion()));

    public static final BlockEntityType<RaccoonStashBlockEntity> RACCOON_STASH_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "raccoon_stash"),
                    FabricBlockEntityTypeBuilder.create(RaccoonStashBlockEntity::new, RACCOON_STASH).build());

    private static Block register(String name, java.util.function.Function<BlockBehaviour.Properties, Block> factory) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, name));
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static void registerModBlocks() {
        Raccoons.LOGGER.info("Registering blocks for " + Raccoons.MOD_ID);
    }
}
