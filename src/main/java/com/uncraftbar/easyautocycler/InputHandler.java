package com.uncraftbar.easyautocycler;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class InputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        // Check if our keybind was pressed
        if (Keybindings.toggleAutoCycleKey != null && Keybindings.toggleAutoCycleKey.consumeClick()) {
            EasyAutoCyclerMod.LOGGER.debug("Toggle key pressed!");
            // Toggle the automation state
            AutomationManager.INSTANCE.toggle();
        }
    }
}