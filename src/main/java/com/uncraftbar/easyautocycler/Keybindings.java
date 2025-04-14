package com.uncraftbar.easyautocycler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class Keybindings {

    public static final String KEY_CATEGORY_AUTO_TRADER = "key.category.easyautocycler";
    public static final String KEY_TOGGLE_AUTO_TRADE = "key.easyautocycler.toggle_auto_trade";
    public static final String KEY_OPEN_CONFIG = "key.easyautocycler.open_config"; // New key

    public static KeyMapping toggleAutoTradeKey;
    public static KeyMapping openConfigKey; // New KeyMapping variable

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        toggleAutoTradeKey = new KeyMapping(
                KEY_TOGGLE_AUTO_TRADE,
                KeyConflictContext.GUI, // Keep GUI for the toggle
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY_AUTO_TRADER
        );
        event.register(toggleAutoTradeKey);

        // Register the new config key
        openConfigKey = new KeyMapping(
                KEY_OPEN_CONFIG,
                KeyConflictContext.IN_GAME, // IN_GAME context is suitable for opening config
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_T, // Default 'T' - Choose an unused key
                KEY_CATEGORY_AUTO_TRADER
        );
        event.register(openConfigKey);

        EasyAutoCyclerMod.LOGGER.info("Registered key mappings");
    }
}