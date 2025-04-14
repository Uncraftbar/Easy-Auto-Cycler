package com.uncraftbar.easyautocycler;

import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;


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
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (AutomationManager.INSTANCE.isRunning()) {
                AutomationManager.INSTANCE.clientTick();
            }
        }
    }
}