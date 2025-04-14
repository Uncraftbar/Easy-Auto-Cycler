package com.uncraftbar.easyautocycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component; // Ensure Component is imported
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;
// Import ConfigScreen
import com.uncraftbar.easyautocycler.gui.ConfigScreen;

public class InputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        Screen currentScreen = mc.screen;

        // --- Handle Toggle Key ---
        if (currentScreen != null && Keybindings.toggleAutoTradeKey != null) {
            if (Keybindings.toggleAutoTradeKey.matches(event.getKey(), event.getScanCode())
                    && event.getAction() == GLFW.GLFW_PRESS) {
                EasyAutoCyclerMod.LOGGER.info("--- Toggle Key Pressed (GUI Context Check)! ---");
                AutomationManager.INSTANCE.toggle();
                return; // Prevent processing config key if toggle key was handled
            }
        }

        // --- Handle Config Key ---
        if (Keybindings.openConfigKey != null && Keybindings.openConfigKey.consumeClick()) {
            EasyAutoCyclerMod.LOGGER.info("--- Open Config Key Pressed! ---");
            // Get the current screen to pass as the 'previousScreen' argument
            // It might be null if pressed from the main game world, which is fine.
            Screen previousScreen = mc.screen;
            // --- Updated call to ConfigScreen constructor ---
            mc.setScreen(new ConfigScreen(previousScreen, Component.translatable("gui.easyautocycler.config.title")));
            return; // Prevent further processing
        }

        // Trace logging (optional)
        if (currentScreen != null) {
            EasyAutoCyclerMod.LOGGER.trace("Key Event: Key={}, Action={}, Modifiers={}, Screen={}",
                    event.getKey(), event.getAction(), event.getModifiers(), currentScreen.getClass().getName());
        }
    }
}