package com.uncraftbar.easyautocycler;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

@Mod(EasyAutoCyclerMod.MODID)
public class EasyAutoCyclerMod {

    public static final String MODID = "easyautocycler";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EasyAutoCyclerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeybindings);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("EasyAutoCyclerMod loaded!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup");
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new InputHandler());
        LOGGER.info("Client event handlers registered.");
    }

    private void registerKeybindings(final RegisterKeyMappingsEvent event) {
        Keybindings.registerKeyMappings(event);
    }

}