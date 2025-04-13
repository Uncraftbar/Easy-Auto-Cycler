package com.uncraftbar.easyautocycler;

import com.mojang.logging.LogUtils;
import com.uncraftbar.easyautocycler.command.CommandSetTrade;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.slf4j.Logger;


@Mod(EasyAutoCyclerMod.MODID)
public class EasyAutoCyclerMod {

    public static final String MODID = "easyautocycler";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EasyAutoCyclerMod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(Keybindings::registerKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::registerClientCommands);

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

    public void registerClientCommands(RegisterClientCommandsEvent event) { // Note the event type change
        // CommandBuildContext is typically NOT provided or needed for client commands
        CommandSetTrade.register(event.getDispatcher()); // Pass only the dispatcher
        LOGGER.info("Registered client commands.");
    }
}