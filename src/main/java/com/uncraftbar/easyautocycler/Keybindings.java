package com.uncraftbar.easyautocycler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class Keybindings {

    public static final String KEY_CATEGORY_AUTO_CYCLE = "key.category.easyautocycler";
    public static final String KEY_TOGGLE_AUTO_CYCLE = "key.easyautocycler.toggle_auto_cycle";

    public static KeyMapping toggleAutoCycleKey;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        toggleAutoCycleKey = new KeyMapping(
                KEY_TOGGLE_AUTO_CYCLE,
                KeyConflictContext.GUI,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY_AUTO_CYCLE
        );
        event.register(toggleAutoCycleKey);
        EasyAutoCyclerMod.LOGGER.info("Registered key mappings");
    }
}