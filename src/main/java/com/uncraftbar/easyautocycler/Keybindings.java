package com.uncraftbar.easyautocycler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class Keybindings {

    public static final String KEY_TOGGLE_AUTO_TRADE = "key.easyautocycler.toggle_auto_trade";
    public static final String KEY_OPEN_CONFIG = "key.easyautocycler.open_config";

    // 26.3 removed GLFW/Type.KEYSYM: key codes come from InputConstants.KEY_*, and
    // mod categories are registered through RegisterKeyMappingsEvent on the mod bus.
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(EasyAutoCyclerMod.MODID, "auto_cycler"));

    public static KeyMapping toggleAutoTradeKey;
    public static KeyMapping openConfigKey;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);

        toggleAutoTradeKey = new KeyMapping(
                KEY_TOGGLE_AUTO_TRADE,
                KeyConflictContext.GUI,
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_R,
                CATEGORY
        );
        event.register(toggleAutoTradeKey);

        openConfigKey = new KeyMapping(
                KEY_OPEN_CONFIG,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_C,
                CATEGORY
        );
        event.register(openConfigKey);

        EasyAutoCyclerMod.LOGGER.info("Registered key mappings");
    }
}
