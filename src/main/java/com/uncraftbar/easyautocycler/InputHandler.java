package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.gui.ConfigScreen;
import com.uncraftbar.easyautocycler.gui.FilterListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;

public class InputHandler {

    public static void onClientTick(Minecraft client) {
        if (Keybindings.openConfigKey != null && Keybindings.openConfigKey.consumeClick()) {
            Screen currentScreen = client.screen;
            if (client.hasShiftDown()) {
                client.setScreen(new FilterListScreen(currentScreen));
            } else {
                client.setScreen(new ConfigScreen(currentScreen, Component.translatable("gui.easyautocycler.config.title")));
            }
        }

        if (Keybindings.toggleAutoTradeKey != null && Keybindings.toggleAutoTradeKey.consumeClick()) {
            if (client.screen instanceof MerchantScreen) {
                AutomationManager.INSTANCE.toggle();
            }
        }
    }
}
