package com.uncraftbar.easyautocycler;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

import org.slf4j.Logger;

public class EasyAutoCyclerMod implements ClientModInitializer {

    public static final String MODID = "easyautocycler";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        LOGGER.info("EasyAutoCyclerMod loaded! (Fabric)");

        AutomationManager.initialize();

        Keybindings.registerKeyMappings();

        ScreenEvents.BEFORE_INIT.register(ClientEventHandler::onScreenBeforeInit);
        ScreenEvents.AFTER_INIT.register(ClientEventHandler::onScreenInit);

        LOGGER.info("Client event handlers registered.");
    }

}
