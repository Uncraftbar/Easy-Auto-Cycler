package com.uncraftbar.easyautocycler;

// Remove CycleTradesButton import if no longer needed anywhere else
// import de.maxhenkel.easyvillagers.gui.CycleTradesButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;

// Imports for logging/casting removed if not needed
// import net.minecraft.client.gui.components.Renderable;
// import net.minecraft.client.gui.components.events.GuiEventListener;
// import java.lang.reflect.Field;


public class ClientEventHandler {

    // REMOVED: private static final String TARGET_BUTTON_CLASS_NAME = ...;

    @SubscribeEvent
    public void onScreenInitPost(ScreenEvent.Init.Post event) {
        // We no longer need to search for the button when the screen opens.
        // We can keep this log message for debugging if desired.
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen opened.");
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenEvent.Closing event) {
        // We still might want to stop cycling if the screen closes.
        // The AutomationManager.clientTick() already handles this,
        // but clearing target reference isn't needed.
        // We could call stop() directly here if needed, but redundant.
        if (event.getScreen() instanceof MerchantScreen) {
            EasyAutoCyclerMod.LOGGER.debug("MerchantScreen closing.");
            // If AutomationManager is running, its tick check will stop it.
            // No need to call AutomationManager.INSTANCE.clearTargetButton();
        }
    }

    // Tick Event (using Forge 1.20.1 structure) - No change needed here
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (AutomationManager.INSTANCE.isRunning()) {
                AutomationManager.INSTANCE.clientTick();
            }
        }
    }
}