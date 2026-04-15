package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.gui.ConfigScreen;
import com.uncraftbar.easyautocycler.gui.CustomImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class ClientEventHandler {

    private static final Identifier CONFIG_BUTTON_NORMAL_RL = Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "gui/config_button.png");
    private static final Identifier CONFIG_BUTTON_HOVER_RL = Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "gui/config_button_highlighted.png");
    private static final Identifier PLAY_BUTTON_NORMAL_RL = Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "gui/play_button.png");
    private static final Identifier PLAY_BUTTON_HOVER_RL = Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "gui/play_button_highlighted.png");

    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof MerchantScreen merchantScreen) {
            if (!AutomationManager.INSTANCE.canCycle(merchantScreen.getMenu())) {
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

            Minecraft mc = Minecraft.getInstance();

            CustomImageButton configButton = new CustomImageButton(
                    configButtonX, configButtonY, buttonWidth, buttonHeight,
                    CONFIG_BUTTON_NORMAL_RL,
                    CONFIG_BUTTON_HOVER_RL,
                    Component.translatable("gui.easyautocycler.button.config.tooltip"),
                    (button) -> mc.setScreen(new ConfigScreen(merchantScreen, Component.translatable("gui.easyautocycler.config.title")))
            );

            CustomImageButton toggleButton = new CustomImageButton(
                    toggleButtonX, toggleButtonY, buttonWidth, buttonHeight,
                    PLAY_BUTTON_NORMAL_RL,
                    PLAY_BUTTON_HOVER_RL,
                    Component.translatable("gui.easyautocycler.button.toggle.tooltip"),
                    (button) -> AutomationManager.INSTANCE.toggle()
            );

            event.addListener(configButton);
            event.addListener(toggleButton);
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen closing.");
        }
    }

    @SubscribeEvent
    public void onClientTickPost(ClientTickEvent.Post event) {
        if (AutomationManager.INSTANCE.isRunning()) {
            AutomationManager.INSTANCE.clientTick();
        }
    }
}
