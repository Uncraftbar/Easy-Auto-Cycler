package com.uncraftbar.easyautocycler;

// Keep CustomImageButton import
import com.uncraftbar.easyautocycler.gui.CustomImageButton;
import net.minecraft.resources.ResourceLocation;
// Other imports...
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import com.uncraftbar.easyautocycler.gui.ConfigScreen;

public class ClientEventHandler {

    // --- Define ResourceLocations WITH .png ---
    // Config Button Textures
    private static final ResourceLocation CONFIG_BUTTON_NORMAL_RL = ResourceLocation.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "textures/gui/config_button.png");
    private static final ResourceLocation CONFIG_BUTTON_HOVER_RL = ResourceLocation.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "textures/gui/config_button_highlighted.png"); // Use NORMAL if no hover

    // Toggle/Play Button Textures
    private static final ResourceLocation PLAY_BUTTON_NORMAL_RL = ResourceLocation.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "textures/gui/play_button.png");
    private static final ResourceLocation PLAY_BUTTON_HOVER_RL = ResourceLocation.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "textures/gui/play_button_highlighted.png"); // Use NORMAL if no hover

    // REMOVED STOP_BUTTON ResourceLocations


    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof MerchantScreen merchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen opened. Adding custom image buttons (Static Toggle Icon)...");

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

            // --- Create "Open Config" CustomImageButton ---
            // (Use NORMAL RL for hover if config_button_highlighted.png doesn't exist)
            CustomImageButton configButton = new CustomImageButton(
                    configButtonX, configButtonY, buttonWidth, buttonHeight,
                    CONFIG_BUTTON_NORMAL_RL, CONFIG_BUTTON_HOVER_RL,
                    Component.translatable("gui.easyautocycler.button.config.tooltip"),
                    (button) -> {
                        if (mc != null) {
                            mc.setScreen(new ConfigScreen(merchantScreen, Component.translatable("gui.easyautocycler.config.title")));
                        }
                    }
            );

            // --- Create "Toggle Cycle" CustomImageButton (Static Icon) ---
            // Always use the PLAY button textures
            // (Use NORMAL RL for hover if play_button_highlighted.png doesn't exist)
            CustomImageButton toggleButton = new CustomImageButton(
                    toggleButtonX, toggleButtonY, buttonWidth, buttonHeight,
                    PLAY_BUTTON_NORMAL_RL, // Always use Play normal texture
                    PLAY_BUTTON_HOVER_RL,  // Always use Play hover texture (or normal again)
                    Component.translatable("gui.easyautocycler.button.toggle.tooltip"), // Use a single static tooltip
                    (button) -> { // Action remains the same
                        AutomationManager.INSTANCE.toggle();
                    }
            );

            // --- Add Buttons to the Screen ---
            event.addListener(configButton);
            event.addListener(toggleButton);

            EasyAutoCyclerMod.LOGGER.debug("Added Config and Toggle custom image buttons to MerchantScreen.");
        }
    }

    // onScreenClose and onClientTickPost remain unchanged
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