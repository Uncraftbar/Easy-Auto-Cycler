package com.uncraftbar.easyautocycler;

import de.maxhenkel.easyvillagers.gui.CycleTradesButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent;

public class ClientEventHandler {

    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof MerchantScreen merchantScreen) {
            EasyAutoCyclerMod.LOGGER.trace("MerchantScreen opened. Searching for CycleTradesButton...");

            boolean found = false;
            for (net.minecraft.client.gui.components.Renderable renderable : event.getScreen().renderables) {
                if (renderable instanceof CycleTradesButton cycleButton) {
                    EasyAutoCyclerMod.LOGGER.trace("Found CycleTradesButton!");
                    AutomationManager.INSTANCE.setTargetButton(cycleButton);
                    found = true;
                    break;
                }
            }
            if (!found) {
                EasyAutoCyclerMod.LOGGER.debug("CycleTradesButton not found on MerchantScreen.");
                AutomationManager.INSTANCE.clearTargetButton();
            }


        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.trace("MerchantScreen closing.");
            AutomationManager.INSTANCE.clearTargetButton();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) { // Changed parameter
        if (event.phase == TickEvent.Phase.END) { // Add phase check
            if (AutomationManager.INSTANCE.isRunning()) {
                AutomationManager.INSTANCE.clientTick();
            }
        }
    }
}