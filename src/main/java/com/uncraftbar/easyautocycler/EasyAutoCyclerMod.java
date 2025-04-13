package com.uncraftbar.easyautocycler;

import com.mojang.logging.LogUtils;
import com.uncraftbar.easyautocycler.command.CommandSetTrade;
// Import Forge event buses and FML event bus
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus; // Still needed
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent; // For client setup
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext; // Get mod event bus this way

import org.slf4j.Logger;

@Mod(EasyAutoCyclerMod.MODID)
public class EasyAutoCyclerMod {

    public static final String MODID = "easyautocycler";
    public static final Logger LOGGER = LogUtils.getLogger();

    // CHANGE: Make constructor have NO arguments
    public EasyAutoCyclerMod() {
        // Get the Mod specific event bus
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register lifecycle methods to the mod event bus
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        // Register keybinding event listener to the mod event bus
        modEventBus.addListener(this::registerKeybindings); // Moved registration here

        // Register client command listener to the FORGE bus (not mod bus)
        MinecraftForge.EVENT_BUS.addListener(this::registerClientCommands);

        LOGGER.info("EasyAutoCyclerMod loaded!");
    }

    // commonSetup - Register things needed on both sides (if any)
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup");
        // If you had common event handlers, register them to MinecraftForge.EVENT_BUS here
        // MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
    }

    // clientSetup - Register client-only things
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Client setup");
        // Register client-only event handlers to the FORGE bus
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new InputHandler());
        LOGGER.info("Client event handlers registered.");
    }

    // Keybinding registration - now called by listener setup in constructor
    public void registerKeybindings(RegisterKeyMappingsEvent event) { // Method needs correct event type
        Keybindings.registerKeyMappings(event); // Delegate to the Keybindings class
    }

    // Client command registration - called by listener setup in constructor
    public void registerClientCommands(RegisterClientCommandsEvent event) {
        CommandSetTrade.register(event.getDispatcher());
        LOGGER.info("Registered client commands.");
    }
}