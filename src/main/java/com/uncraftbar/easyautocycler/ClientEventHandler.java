package com.uncraftbar.easyautocycler;

import de.maxhenkel.easyvillagers.gui.CycleTradesButton; // Import the button class
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class ClientEventHandler {

    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof MerchantScreen merchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen opened. Searching for CycleTradesButton...");
            // Iterate through screen widgets to find the button
            boolean found = false;
            for (net.minecraft.client.gui.components.Renderable renderable : merchantScreen.renderables) {
                if (renderable instanceof CycleTradesButton cycleButton) {
                    EasyAutoCyclerMod.LOGGER.debug("Found CycleTradesButton!");
                    AutomationManager.INSTANCE.setTargetButton(cycleButton);
                    found = true;
                    break; // Assuming there's only one
                }
            }
            if (!found) {
                EasyAutoCyclerMod.LOGGER.debug("CycleTradesButton not found on MerchantScreen.");
                AutomationManager.INSTANCE.clearTargetButton(); // Ensure it's clear if not found
            }
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenEvent.Closing event) {
        // If the MerchantScreen is closed, clear the button reference and stop automation
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen closing.");
            AutomationManager.INSTANCE.clearTargetButton();
            AutomationManager.INSTANCE.stop("Merchant screen closed");
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) { // Or ClientTickEvent.End
        // Tick the automation manager only if it's running
        if (AutomationManager.INSTANCE.isRunning()) {
            AutomationManager.INSTANCE.clientTick();
        }
    }
}