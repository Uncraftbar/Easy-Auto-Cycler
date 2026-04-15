package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.gui.ConfigScreen;
import com.uncraftbar.easyautocycler.gui.CustomImageButton;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ClientEventHandler {

    private static final Identifier CONFIG_BUTTON_NORMAL_RL = Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "textures/gui/config_button.png");
    private static final Identifier CONFIG_BUTTON_HOVER_RL = Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "textures/gui/config_button_highlighted.png");
    private static final Identifier PLAY_BUTTON_NORMAL_RL = Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "textures/gui/play_button.png");
    private static final Identifier PLAY_BUTTON_HOVER_RL = Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "textures/gui/play_button_highlighted.png");

    public static void onScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof MerchantScreen merchantScreen)) {
            return;
        }

        int leftPos = (merchantScreen.width - 276) / 2;
        int topPos = (merchantScreen.height - 166) / 2;
        int buttonWidth = 18;
        int buttonHeight = 18;
        int buttonPadding = 2;
        int cycleButtonPosX = leftPos + 107;
        int cycleButtonPosY = topPos + 8;
        int cycleButtonHeight = 14;
        int configButtonX = cycleButtonPosX;
        int configButtonY = cycleButtonPosY + cycleButtonHeight + buttonPadding;
        int toggleButtonX = cycleButtonPosX;
        int toggleButtonY = configButtonY + buttonHeight + buttonPadding;

        CustomImageButton configButton = new CustomImageButton(
                configButtonX, configButtonY, buttonWidth, buttonHeight,
                CONFIG_BUTTON_NORMAL_RL, CONFIG_BUTTON_HOVER_RL,
                Component.translatable("gui.easyautocycler.button.config.tooltip"),
                (button) -> Minecraft.getInstance().setScreen(
                        new ConfigScreen(merchantScreen, Component.translatable("gui.easyautocycler.config.title"))));

        CustomImageButton toggleButton = new CustomImageButton(
                toggleButtonX, toggleButtonY, buttonWidth, buttonHeight,
                PLAY_BUTTON_NORMAL_RL, PLAY_BUTTON_HOVER_RL,
                Component.translatable("gui.easyautocycler.button.toggle.tooltip"),
                (button) -> AutomationManager.INSTANCE.toggle());

        Screens.getWidgets(screen).add(configButton);
        Screens.getWidgets(screen).add(toggleButton);

        // The toggle keybind doesn't fire through KeyMapping.consumeClick() while a Screen is
        // open, so hook the screen's key-press event directly for MerchantScreen.
        ScreenKeyboardEvents.beforeKeyPress(screen).register((s, event) -> {
            if (Keybindings.toggleAutoTradeKey != null && Keybindings.toggleAutoTradeKey.matches(event)) {
                AutomationManager.INSTANCE.toggle();
            }
        });
    }
}
