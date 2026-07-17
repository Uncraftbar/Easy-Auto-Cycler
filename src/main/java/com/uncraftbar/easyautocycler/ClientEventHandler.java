package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.gui.CustomImageButton;
import com.uncraftbar.easyautocycler.config.ClientConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import com.uncraftbar.easyautocycler.gui.ConfigScreen;

public class ClientEventHandler {

    private static final ResourceLocation CONFIG_BUTTON_NORMAL_RL = ResourceLocation.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "gui/config_button.png");
    private static final ResourceLocation CONFIG_BUTTON_HOVER_RL = ResourceLocation.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "gui/config_button_highlighted.png"); // Use NORMAL if no hover

    private static final ResourceLocation PLAY_BUTTON_NORMAL_RL = ResourceLocation.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "gui/play_button.png");
    private static final ResourceLocation PLAY_BUTTON_HOVER_RL = ResourceLocation.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "gui/play_button_highlighted.png"); // Use NORMAL if no hover


    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof MerchantScreen merchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen opened. Adding custom image buttons (Static Toggle Icon)...");

            ClientConfig.Config clientConfig = ClientConfig.load();
            ClientConfig.ButtonLocation buttonLocation = clientConfig.parsedButtonLocation();
            if (buttonLocation == ClientConfig.ButtonLocation.NONE) {
                return;
            }

            int leftPos = (merchantScreen.width - 276) / 2;
            int topPos = (merchantScreen.height - 166) / 2;
            int buttonWidth = 18; int buttonHeight = 18; int buttonPadding = 2;
            int anchorX = buttonLocation == ClientConfig.ButtonLocation.TOP_RIGHT ? 250 : 107;
            int cycleButtonPosX = leftPos + anchorX + clientConfig.buttonOffsetX;
            int cycleButtonPosY = topPos + 8 + clientConfig.buttonOffsetY;
            int cycleButtonHeight = 14;
            int configButtonX = cycleButtonPosX; int configButtonY = cycleButtonPosY + cycleButtonHeight + buttonPadding;
            int toggleButtonX = cycleButtonPosX; int toggleButtonY = configButtonY + buttonHeight + buttonPadding;

            Minecraft mc = Minecraft.getInstance();

            CustomImageButton configButton = new CycleAwareImageButton(
                    merchantScreen,
                    configButtonX, configButtonY, buttonWidth, buttonHeight,
                    CONFIG_BUTTON_NORMAL_RL,
                    CONFIG_BUTTON_HOVER_RL,
                    Component.translatable("gui.easyautocycler.button.config.tooltip"),
                    (button) -> {
                        if (mc != null) {
                            EasyAutoCyclerMod.LOGGER.debug("Opening ConfigScreen via setScreen.");
                            mc.setScreen(new ConfigScreen(merchantScreen, Component.translatable("gui.easyautocycler.config.title")));
                        }
                    }
            );

            CustomImageButton toggleButton = new CycleAwareImageButton(
                    merchantScreen,
                    toggleButtonX, toggleButtonY, buttonWidth, buttonHeight,
                    PLAY_BUTTON_NORMAL_RL,
                    PLAY_BUTTON_HOVER_RL,
                    Component.translatable("gui.easyautocycler.button.toggle.tooltip"),
                    (button) -> {
                        AutomationManager.INSTANCE.toggle();
                    }
            );

            event.addListener(configButton);
            event.addListener(toggleButton);

            EasyAutoCyclerMod.LOGGER.trace("Added Config and Toggle custom image buttons to MerchantScreen.");
        }
    }

    @SubscribeEvent public void onScreenClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen closing."); } }
    @SubscribeEvent public void onClientTickPost(ClientTickEvent.Post event) {
        if (AutomationManager.INSTANCE.isRunning()) {AutomationManager.INSTANCE.clientTick(); } }
    private static class CycleAwareImageButton extends CustomImageButton {
        private final MerchantScreen merchantScreen;
        private boolean cycleAvailable;

        CycleAwareImageButton(MerchantScreen merchantScreen, int x, int y, int width, int height,
                              ResourceLocation textureNormal, ResourceLocation textureHover,
                              Component tooltip, OnPress onPress) {
            super(x, y, width, height, textureNormal, textureHover, tooltip, onPress);
            this.merchantScreen = merchantScreen;
        }

        private void refreshCycleAvailability() {
            // Keep visible=true: Minecraft stops invoking the inner render hook for invisible widgets,
            // which would permanently prevent a temporarily unavailable button from recovering.
            this.cycleAvailable = AutomationManager.INSTANCE.canCycle(this.merchantScreen.getMenu());
            this.active = this.cycleAvailable;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            refreshCycleAvailability();
            if (this.cycleAvailable) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public void onPress() {
            refreshCycleAvailability();
            if (this.cycleAvailable) {
                super.onPress();
            }
        }
    }

}
