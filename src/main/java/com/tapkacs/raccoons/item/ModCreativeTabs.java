package com.tapkacs.raccoons.item;

import com.tapkacs.raccoons.Raccoons;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {

    public static final ResourceKey<CreativeModeTab> RACCOON_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Raccoons.MOD_ID, "raccoon"));

    public static void registerModCreativeTabs() {
        Raccoons.LOGGER.info("Registering creative tabs for " + Raccoons.MOD_ID);

        CreativeModeTab tab = FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.raccoons.raccoon"))
                .icon(() -> new ItemStack(ModItems.RACCOON_SPAWN_EGG))
                .displayItems((parameters, output) -> {
                    output.accept(ModItems.RACCOON_SPAWN_EGG);
                    output.accept(ModItems.RACCOON_HAT);
                    output.accept(ModItems.RACCOON_BELL);
                    output.accept(ModItems.RACCOON_STASH);
                    output.accept(ModItems.CANDY_COTTON);
                })
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, RACCOON_TAB, tab);
    }
}
