package com.uncraftbar.easyautocycler;

// Imports for NeoForge events
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;


public class ClientEventHandler {

    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen opened.");
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen closing.");
        }
    }

    @SubscribeEvent
    public void onClientTickPost(ClientTickEvent.Post event) {
        if (AutomationManager.INSTANCE.isRunning()) {
            AutomationManager.INSTANCE.clientTick();
        }
    }
}