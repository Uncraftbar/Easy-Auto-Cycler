package com.uncraftbar.easyautocycler;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EasyAutoCyclerMod.MODID) // MODID must match build.gradle and mods.toml
public class EasyAutoCyclerMod {

    public static final String MODID = "easyautocycler"; // Replace with your mod ID
    public static final Logger LOGGER = LogUtils.getLogger();

    public EasyAutoCyclerMod(IEventBus modEventBus) {
        // Register common setup
        modEventBus.addListener(this::commonSetup);
        // Register client setup
        modEventBus.addListener(this::clientSetup);

        // Register other mod-specific event listeners if needed (e.g., config loading)
        // ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.SPEC, "easyautocycler-client.toml");

        // Register keybinding event listener
        modEventBus.addListener(Keybindings::registerKeyMappings);

        LOGGER.info("EasyAutoCyclerMod loaded!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup");
        // Register client-side event handlers to the Forge event bus
        NeoForge.EVENT_BUS.register(new ClientEventHandler());
        NeoForge.EVENT_BUS.register(new InputHandler()); // Register input handler for keybinds
        LOGGER.info("Client event handlers registered.");
    }
}