package com.tapkacs.raccoons;

import net.fabricmc.api.ModInitializer;

import com.tapkacs.raccoons.advancement.ModTriggers;
import com.tapkacs.raccoons.entity.ModEntityTypes;
import com.tapkacs.raccoons.item.ModCreativeTabs;
import com.tapkacs.raccoons.item.ModItems;
import com.tapkacs.raccoons.sound.ModSounds;
import com.tapkacs.raccoons.stat.ModStats;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Raccoons implements ModInitializer {
	public static final String MOD_ID = "raccoons";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModEntityTypes.registerModEntityTypes();
		ModEntityTypes.registerAttributes();
		ModEntityTypes.registerSpawns();
		ModItems.registerModItems();
		ModCreativeTabs.registerModCreativeTabs();
		ModSounds.registerModSounds();
		ModTriggers.registerModTriggers();
		ModStats.registerModStats();

		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
