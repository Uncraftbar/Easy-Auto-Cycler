package com.uncraftbar.easyautocycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

public class InputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen != null) {

            if (Keybindings.toggleAutoCycleKey != null) {


                if (Keybindings.toggleAutoCycleKey.matches(event.getKey(), event.getScanCode())
                        && event.getAction() == GLFW.GLFW_PRESS) {

                    EasyAutoCyclerMod.LOGGER.info("--- Toggle Key Pressed (GUI Context Check)! ---");
                    EasyAutoCyclerMod.LOGGER.debug("InputHandler: Screen before toggle() call: {}", currentScreen.getClass().getName());
                    EasyAutoCyclerMod.LOGGER.debug("InputHandler: Attempting toggle. AutomationManager has targetButton: {}", AutomationManager.INSTANCE.getTargetButton() != null);

                    AutomationManager.INSTANCE.toggle();

                }
            }
        }
    }
}