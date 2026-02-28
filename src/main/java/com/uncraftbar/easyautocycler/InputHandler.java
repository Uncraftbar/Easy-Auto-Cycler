package com.uncraftbar.easyautocycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;

import com.uncraftbar.easyautocycler.gui.ConfigScreen;

public class InputHandler {

    public static void onClientTick(Minecraft client) {
        // Check open config key (works anywhere, uses consumeClick for IN_GAME-style behavior)
        if (Keybindings.openConfigKey != null && Keybindings.openConfigKey.consumeClick()) {
            EasyAutoCyclerMod.LOGGER.info("--- Open Config Key Pressed! ---");
            Screen currentScreen = client.screen;
            Minecraft.getInstance().setScreen(new ConfigScreen(currentScreen, Component.translatable("gui.easyautocycler.config.title")));
        }

        // Check toggle key (only in MerchantScreen)
        if (Keybindings.toggleAutoTradeKey != null && Keybindings.toggleAutoTradeKey.consumeClick()) {
            Screen currentScreen = client.screen;
            if (currentScreen instanceof MerchantScreen) {
                EasyAutoCyclerMod.LOGGER.info("--- Toggle Key Pressed (MerchantScreen)! ---");
                AutomationManager.INSTANCE.toggle();
            }
        }
    }
}