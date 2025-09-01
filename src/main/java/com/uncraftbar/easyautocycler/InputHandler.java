package com.uncraftbar.easyautocycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

import com.uncraftbar.easyautocycler.gui.ConfigScreen;

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
        }

        if (Keybindings.openConfigKey != null && Keybindings.openConfigKey.consumeClick()) {
            EasyAutoCyclerMod.LOGGER.info("--- Open Config Key Pressed! ---");
            Minecraft.getInstance().setScreen(new ConfigScreen(currentScreen, Component.translatable("gui.easyautocycler.config.title")));
        }
    }
}