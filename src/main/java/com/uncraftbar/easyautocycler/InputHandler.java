package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;

public class InputHandler {

    public static void onClientTick(Minecraft client) {
        if (Keybindings.openConfigKey != null && Keybindings.openConfigKey.consumeClick()) {
            client.setScreenAndShow(new ConfigScreen(client.gui.screen(), Component.translatable("gui.easyautocycler.config.title")));
        }

        if (Keybindings.toggleAutoTradeKey != null && Keybindings.toggleAutoTradeKey.consumeClick()) {
            if (client.gui.screen() instanceof MerchantScreen) {
                AutomationManager.INSTANCE.toggle();
            }
        }
    }
}
