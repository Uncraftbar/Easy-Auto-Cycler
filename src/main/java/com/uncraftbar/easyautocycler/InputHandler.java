package com.uncraftbar.easyautocycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;
import com.uncraftbar.easyautocycler.gui.ConfigScreen;
import com.uncraftbar.easyautocycler.gui.FilterListScreen;

public class InputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        Screen currentScreen = mc.screen;

        if (currentScreen instanceof MerchantScreen && Keybindings.toggleAutoTradeKey != null) {
            if (Keybindings.toggleAutoTradeKey.matches(event.getKey(), event.getScanCode())
                    && event.getAction() == GLFW.GLFW_PRESS) {
                EasyAutoCyclerMod.LOGGER.info("--- Toggle Key Pressed (MerchantScreen)! ---");
                AutomationManager.INSTANCE.toggle();
                return;
            }
        }        if (Keybindings.openConfigKey != null && Keybindings.openConfigKey.consumeClick()) {
            EasyAutoCyclerMod.LOGGER.info("--- Open Config Key Pressed! ---");
            // Check if shift is being held to open the filter list instead
            boolean shiftPressed = Screen.hasShiftDown();
            if (shiftPressed) {                EasyAutoCyclerMod.LOGGER.info("Opening FilterListScreen due to Shift key");
                mc.setScreen(new FilterListScreen(currentScreen));
            } else {
                mc.setScreen(new ConfigScreen(currentScreen, Component.translatable("gui.easyautocycler.config.title")));
            }
            return;
        }
    }
}