package com.uncraftbar.easyautocycler;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class Keybindings {

    public static final String KEY_CATEGORY_AUTO_TRADER = "key.category.easyautocycler";
    public static final String KEY_TOGGLE_AUTO_TRADE = "key.easyautocycler.toggle_auto_trade";
    public static final String KEY_OPEN_CONFIG = "key.easyautocycler.open_config";

    public static KeyMapping toggleAutoTradeKey;
    public static KeyMapping openConfigKey;

    public static void registerKeyMappings() {
        toggleAutoTradeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_TOGGLE_AUTO_TRADE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY_AUTO_TRADER
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_OPEN_CONFIG,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                KEY_CATEGORY_AUTO_TRADER
        ));

        EasyAutoCyclerMod.LOGGER.info("Registered key mappings");
    }
}