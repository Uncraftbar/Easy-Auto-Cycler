package com.uncraftbar.easyautocycler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class Keybindings {

    public static final String KEY_CATEGORY_AUTO_CYCLE = "key.category.easyautocycler";
    public static final String KEY_TOGGLE_AUTO_CYCLE = "key.easyautocycler.toggle_auto_cycle";

    public static KeyMapping toggleAutoCycleKey;

    // Called via the mod event bus listener in the main class
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        toggleAutoCycleKey = new KeyMapping(
                KEY_TOGGLE_AUTO_CYCLE,
                KeyConflictContext.GUI, // Only active when a GUI is open
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K, // Default key K - change as desired
                KEY_CATEGORY_AUTO_CYCLE
        );
        event.register(toggleAutoCycleKey);
        EasyAutoCyclerMod.LOGGER.info("Registered key mappings");
    }
}