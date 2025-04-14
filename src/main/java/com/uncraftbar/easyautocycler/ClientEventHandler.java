package com.uncraftbar.easyautocycler;

// Imports for NeoForge events
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.neoforged.bus.api.SubscribeEvent; // Use NeoForge bus API
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent; // Use NeoForge client tick


public class ClientEventHandler {

    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        // No longer need to search for the button
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen opened.");
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenEvent.Closing event) {
        // No longer need to clear button reference
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen closing.");
            // AutomationManager will stop itself in its tick check if running
        }
    }

    // Use NeoForge ClientTickEvent.Post
    @SubscribeEvent
    public void onClientTickPost(ClientTickEvent.Post event) {
        // No phase check needed for .Post event
        if (AutomationManager.INSTANCE.isRunning()) {
            AutomationManager.INSTANCE.clientTick();
        }
    }
}