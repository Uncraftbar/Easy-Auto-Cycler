package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.gui.CustomImageButton;
import com.uncraftbar.easyautocycler.gui.ConfigScreen;
import com.uncraftbar.easyautocycler.mixin.ScreenAccessorMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClientEventHandler {

    private static final ResourceLocation CONFIG_BUTTON_NORMAL_RL = ResourceLocation.tryParse(EasyAutoCyclerMod.MODID + ":gui/config_button.png");
    private static final ResourceLocation CONFIG_BUTTON_HOVER_RL = ResourceLocation.tryParse(EasyAutoCyclerMod.MODID + ":gui/config_button_highlighted.png");
    private static final ResourceLocation PLAY_BUTTON_NORMAL_RL = ResourceLocation.tryParse(EasyAutoCyclerMod.MODID + ":gui/play_button.png");
    private static final ResourceLocation PLAY_BUTTON_HOVER_RL = ResourceLocation.tryParse(EasyAutoCyclerMod.MODID + ":gui/play_button_highlighted.png");

    public static void onScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof MerchantScreen merchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen opened. Adding custom image buttons...");
            int leftPos = (merchantScreen.width - 276) / 2; int topPos = (merchantScreen.height - 166) / 2; int buttonWidth = 18; int buttonHeight = 18; int buttonPadding = 2; int cycleButtonPosX = leftPos + 107; int cycleButtonPosY = topPos + 8; int cycleButtonHeight = 14; int configButtonX = cycleButtonPosX; int configButtonY = cycleButtonPosY + cycleButtonHeight + buttonPadding; int toggleButtonX = cycleButtonPosX; int toggleButtonY = configButtonY + buttonHeight + buttonPadding;

            CustomImageButton configButton = new CustomImageButton( configButtonX, configButtonY, buttonWidth, buttonHeight, CONFIG_BUTTON_NORMAL_RL, CONFIG_BUTTON_HOVER_RL, Component.translatable("gui.easyautocycler.button.config.tooltip"),
                    (button) -> {
                        EasyAutoCyclerMod.LOGGER.debug("Calling ConfigScreen.open from button.");
                        Minecraft.getInstance().setScreen(new ConfigScreen(merchantScreen, Component.translatable("gui.easyautocycler.config.title")));
                    }
            );
            CustomImageButton toggleButton = new CustomImageButton( toggleButtonX, toggleButtonY, buttonWidth, buttonHeight, PLAY_BUTTON_NORMAL_RL, PLAY_BUTTON_HOVER_RL, Component.translatable("gui.easyautocycler.button.toggle.tooltip"), (button) -> { AutomationManager.INSTANCE.toggle(); } );

            ScreenAccessorMixin accessor = (ScreenAccessorMixin) screen;
            accessor.getChildren().add(configButton);
            accessor.getRenderables().add(configButton);
            accessor.getChildren().add(toggleButton);
            accessor.getRenderables().add(toggleButton);
            EasyAutoCyclerMod.LOGGER.debug("Added Config and Toggle custom image buttons to MerchantScreen.");
        }
    }
}