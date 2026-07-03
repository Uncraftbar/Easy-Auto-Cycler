package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

public class InputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        Screen currentScreen = mc.gui.screen();

        if (currentScreen instanceof MerchantScreen
                && Keybindings.toggleAutoTradeKey != null
                && event.getAction() == GLFW.GLFW_PRESS
                && Keybindings.toggleAutoTradeKey.matches(event.getKeyEvent())) {
            EasyAutoCyclerMod.LOGGER.info("--- Toggle Key Pressed (MerchantScreen)! ---");
            AutomationManager.INSTANCE.toggle();
            return;
        }

        if (Keybindings.openConfigKey != null && Keybindings.openConfigKey.consumeClick()) {
            mc.setScreenAndShow(new ConfigScreen(currentScreen, Component.translatable("gui.easyautocycler.config.title")));
        }
    }
}
