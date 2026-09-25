package com.uncraftbar.easyautocycler;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import org.slf4j.Logger;

/**
 * Common (both-sides) entrypoint.
 *
 * <p>This class MUST NOT reference any {@code net.minecraft.client.*} type, directly or through a
 * listener target: a dedicated server constructs it too. The client-only half lives in
 * {@link EasyAutoCyclerClientMod} ({@code dist = Dist.CLIENT}).
 *
 * <p>Before this split, the single entrypoint registered {@code clientSetup} and
 * {@code registerKeybindings} from here. Verifying those method references made the server resolve
 * {@code net.minecraft.client.resources.sounds.SoundInstance}, which does not exist on a dedicated
 * server, aborting startup with {@code NoClassDefFoundError}.
 */
@Mod(EasyAutoCyclerMod.MODID)
public class EasyAutoCyclerMod {

    public static final String MODID = "easyautocycler";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EasyAutoCyclerMod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("EasyAutoCyclerMod loaded!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        if (FMLEnvironment.getDist().isClient()) {
            // AutomationManager is client-only code; it is initialised from the client entrypoint.
            LOGGER.info("Common setup (client)");
        } else {
            // Documented behaviour for a client-only mod on a physical server: no-op, do not crash.
            LOGGER.info("Easy Auto Cycler is a client-only mod - nothing to set up on the dedicated server.");
        }
    }
}