package com.uncraftbar.easyautocycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.SubscribeEvent; // Use NeoForge bus API
import net.neoforged.neoforge.client.event.InputEvent; // Use NeoForge input event
import org.lwjgl.glfw.GLFW;

public class InputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen != null) {
            EasyAutoCyclerMod.LOGGER.trace("Key Event: Key={}, Action={}, Modifiers={}, Screen={}",
                    event.getKey(), event.getAction(), event.getModifiers(), currentScreen.getClass().getName());

            if (Keybindings.toggleAutoCycleKey != null) {
                // Use matches() + Action check
                if (Keybindings.toggleAutoCycleKey.matches(event.getKey(), event.getScanCode())
                        && event.getAction() == GLFW.GLFW_PRESS) {

                    EasyAutoCyclerMod.LOGGER.info("--- Toggle Key Pressed (GUI Context Check)! ---");
                    EasyAutoCyclerMod.LOGGER.debug("InputHandler: Screen before toggle() call: {}", currentScreen.getClass().getName());
                    // REMOVED: Button state logging

                    AutomationManager.INSTANCE.toggle();
                }
            }
        }
    }
}