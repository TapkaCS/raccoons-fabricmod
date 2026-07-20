package com.tapkacs.raccoons.item;

import com.tapkacs.raccoons.Raccoons;
import com.tapkacs.raccoons.block.ModBlocks;
import com.tapkacs.raccoons.entity.ModEntityTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {

    public static final Item RACCOON_SPAWN_EGG = register("raccoon_spawn_egg",
            properties -> new RaccoonSpawnEggItem(properties.spawnEgg(ModEntityTypes.RACCOON)));

    public static final Item RACCOON_HAT = register("raccoon_hat", RaccoonHatItem::new);

    public static final Item RACCOON_BELL = register("raccoon_bell", RaccoonBellItem::new);

    public static final Item RACCOON_STASH = register("raccoon_stash",
            properties -> new BlockItem(ModBlocks.RACCOON_STASH, properties));

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, name));
        Item item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void registerModItems() {
        Raccoons.LOGGER.info("Registering items for " + Raccoons.MOD_ID);
    }
}
