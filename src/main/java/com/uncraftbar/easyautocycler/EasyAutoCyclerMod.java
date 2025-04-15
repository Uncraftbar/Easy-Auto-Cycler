package com.uncraftbar.easyautocycler;

import com.mojang.logging.LogUtils;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import org.slf4j.Logger;

@Mod(EasyAutoCyclerMod.MODID)
public class EasyAutoCyclerMod {

    public static final String MODID = "easyautocycler";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EasyAutoCyclerMod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeybindings);


        LOGGER.info("EasyAutoCyclerMod loaded!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup");
        NeoForge.EVENT_BUS.register(new ClientEventHandler());
        NeoForge.EVENT_BUS.register(new InputHandler());
        LOGGER.info("Client event handlers registered.");
    }

    private void registerKeybindings(final RegisterKeyMappingsEvent event) {
        Keybindings.registerKeyMappings(event);
    }

}