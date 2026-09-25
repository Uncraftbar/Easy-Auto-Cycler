package com.uncraftbar.easyautocycler;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only entrypoint (physical client only).
 *
 * <p>Everything reachable from here touches client classes ({@code Minecraft},
 * {@code MerchantScreen}, {@code SimpleSoundInstance}, ...), so it must not be part of the
 * both-sides entrypoint: on a dedicated server those types are absent and resolving them aborts
 * startup with {@code NoClassDefFoundError}.
 */
@Mod(value = EasyAutoCyclerMod.MODID, dist = Dist.CLIENT)
public class EasyAutoCyclerClientMod {

    public EasyAutoCyclerClientMod(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeybindings);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        EasyAutoCyclerMod.LOGGER.info("Client setup");

        // AutomationManager mirrors client state (current screen, sound manager), so it is
        // initialised on the physical client rather than in common setup.
        event.enqueueWork(() -> {
            EasyAutoCyclerMod.LOGGER.info("Initializing AutomationManager");
            AutomationManager.initialize();
        });

        NeoForge.EVENT_BUS.register(new ClientEventHandler());
        NeoForge.EVENT_BUS.register(new InputHandler());
        EasyAutoCyclerMod.LOGGER.info("Client event handlers registered.");
    }

    private void registerKeybindings(final RegisterKeyMappingsEvent event) {
        Keybindings.registerKeyMappings(event);
    }
}
