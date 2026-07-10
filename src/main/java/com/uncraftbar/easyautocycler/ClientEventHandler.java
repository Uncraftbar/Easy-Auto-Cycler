package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.gui.CustomImageButton;
import com.uncraftbar.easyautocycler.gui.ConfigScreen;
import com.uncraftbar.easyautocycler.config.ClientConfig;
import com.uncraftbar.easyautocycler.mixin.ScreenAccessorMixin;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClientEventHandler {

    private static final ResourceLocation CONFIG_BUTTON_NORMAL_RL = ResourceLocation.tryParse(EasyAutoCyclerMod.MODID + ":gui/config_button.png");
    private static final ResourceLocation CONFIG_BUTTON_HOVER_RL = ResourceLocation.tryParse(EasyAutoCyclerMod.MODID + ":gui/config_button_highlighted.png");
    private static final ResourceLocation PLAY_BUTTON_NORMAL_RL = ResourceLocation.tryParse(EasyAutoCyclerMod.MODID + ":gui/play_button.png");
    private static final ResourceLocation PLAY_BUTTON_HOVER_RL = ResourceLocation.tryParse(EasyAutoCyclerMod.MODID + ":gui/play_button_highlighted.png");

    /**
     * Register per-MerchantScreen keyboard handler. Called from BEFORE_INIT so the
     * per-screen event registry has just been reset (Fabric clears it at init HEAD).
     * The toggle keybind doesn't fire through KeyMapping.consumeClick() while a Screen
     * has input focus, so we hook the screen's key-press event directly.
     */
    public static void onScreenBeforeInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (!(screen instanceof MerchantScreen)) return;
        ScreenKeyboardEvents.beforeKeyPress(screen).register((s, key, scancode, modifiers) -> {
            if (Keybindings.toggleAutoTradeKey != null && Keybindings.toggleAutoTradeKey.matches(key, scancode)) {
                AutomationManager.INSTANCE.toggle();
            }
        });
    }

    public static void onScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof MerchantScreen merchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen opened. Adding custom image buttons...");
            ClientConfig.Config clientConfig = ClientConfig.load();
            ClientConfig.ButtonLocation buttonLocation = clientConfig.parsedButtonLocation();
            if (buttonLocation == ClientConfig.ButtonLocation.NONE) return;
            int leftPos = (merchantScreen.width - 276) / 2; int topPos = (merchantScreen.height - 166) / 2; int buttonWidth = 18; int buttonHeight = 18; int buttonPadding = 2; int anchorX = buttonLocation == ClientConfig.ButtonLocation.TOP_RIGHT ? 250 : 107; int cycleButtonPosX = leftPos + anchorX + clientConfig.buttonOffsetX; int cycleButtonPosY = topPos + 8 + clientConfig.buttonOffsetY; int cycleButtonHeight = 14; int configButtonX = cycleButtonPosX; int configButtonY = cycleButtonPosY + cycleButtonHeight + buttonPadding; int toggleButtonX = cycleButtonPosX; int toggleButtonY = configButtonY + buttonHeight + buttonPadding;

            CustomImageButton configButton = new CycleAwareImageButton(
                    merchantScreen, configButtonX, configButtonY, buttonWidth, buttonHeight, CONFIG_BUTTON_NORMAL_RL, CONFIG_BUTTON_HOVER_RL, Component.translatable("gui.easyautocycler.button.config.tooltip"),
                    (button) -> {
                        EasyAutoCyclerMod.LOGGER.debug("Calling ConfigScreen.open from button.");
                        Minecraft.getInstance().setScreen(new ConfigScreen(merchantScreen, Component.translatable("gui.easyautocycler.config.title")));
                    }
            );
            CustomImageButton toggleButton = new CycleAwareImageButton(
                    merchantScreen, toggleButtonX, toggleButtonY, buttonWidth, buttonHeight, PLAY_BUTTON_NORMAL_RL, PLAY_BUTTON_HOVER_RL, Component.translatable("gui.easyautocycler.button.toggle.tooltip"), (button) -> { AutomationManager.INSTANCE.toggle(); } );

            ScreenAccessorMixin accessor = (ScreenAccessorMixin) screen;
            accessor.getChildren().add(configButton);
            accessor.getRenderables().add(configButton);
            accessor.getChildren().add(toggleButton);
            accessor.getRenderables().add(toggleButton);
            EasyAutoCyclerMod.LOGGER.debug("Added Config and Toggle custom image buttons to MerchantScreen.");
        }
    }

    private static class CycleAwareImageButton extends CustomImageButton {
        private final MerchantScreen merchantScreen;

        CycleAwareImageButton(MerchantScreen merchantScreen, int x, int y, int width, int height,
                              ResourceLocation textureNormal, ResourceLocation textureHover,
                              Component tooltip, OnPress onPress) {
            super(x, y, width, height, textureNormal, textureHover, tooltip, onPress);
            this.merchantScreen = merchantScreen;
        }

        private void refreshCycleAvailability() {
            boolean canCycle = AutomationManager.INSTANCE.canCycleTrades(this.merchantScreen.getMenu());
            this.visible = canCycle;
            this.active = canCycle;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            refreshCycleAvailability();
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public void onPress() {
            refreshCycleAvailability();
            if (this.visible && this.active) {
                super.onPress();
            }
        }
    }

}
