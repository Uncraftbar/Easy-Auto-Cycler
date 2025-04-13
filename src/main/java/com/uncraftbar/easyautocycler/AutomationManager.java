package com.uncraftbar.easyautocycler;

import de.maxhenkel.easyvillagers.gui.CycleTradesButton; // Import the button class
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
// Import classes for items, enchantments etc. as needed for your criteria check
// e.g., import net.minecraft.world.item.Items;
// e.g., import net.minecraft.world.item.enchantment.Enchantments;
// e.g., import net.minecraft.world.item.enchantment.EnchantedBookItem;

import java.util.concurrent.atomic.AtomicBoolean;

public class AutomationManager {

    // Singleton pattern or pass instance around
    public static final AutomationManager INSTANCE = new AutomationManager();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private CycleTradesButton targetButton = null;
    private int delayTicks = 0;
    private int currentCycles = 0;
    private static final int MAX_CYCLES_SAFETY = 2000; // Safety break
    private static final int CLICK_DELAY = 5; // Ticks to wait after clicking

    // TODO: Add fields for your desired trade criteria (e.g., Enchantment, level, max price)
    // Example: private Enchantment targetEnchantment = Enchantments.MENDING;

    private AutomationManager() {} // Private constructor for singleton

    public boolean isRunning() {
        return isRunning.get();
    }

    public void setTargetButton(CycleTradesButton button) {
        this.targetButton = button;
        EasyAutoCyclerMod.LOGGER.debug("Target button set: {}", button != null);
    }

    public void clearTargetButton() {
        this.targetButton = null;
        // Optionally stop if the button disappears mid-run
        // if (isRunning.get()) {
        //     stop("Button lost");
        // }
    }

    public void toggle() {
        if (isRunning.get()) {
            stop("Toggled off by user");
        } else {
            start();
        }
    }

    private void start() {
        // Ensure we are in the right screen and have the button
        if (!(Minecraft.getInstance().screen instanceof MerchantScreen) || targetButton == null) {
            EasyAutoCyclerMod.LOGGER.warn("Cannot start: Not in MerchantScreen or button not found.");
            // Maybe send chat message to player
            return;
        }

        // TODO: Load desired trade criteria from config or commands here
        // if (targetTradeCriteria == null) { stop("No target trade configured"); return; }


        if (isRunning.compareAndSet(false, true)) {
            EasyAutoCyclerMod.LOGGER.info("Starting villager trade cycling.");
            // Send chat message: ChatUtils. Minedu.getInstance().player.sendSystemMessage(...)
            this.delayTicks = 0; // Start immediately on the first tick
            this.currentCycles = 0;
        }
    }

    public void stop(String reason) {
        if (isRunning.compareAndSet(true, false)) {
            EasyAutoCyclerMod.LOGGER.info("Stopping villager trade cycling. Reason: {}", reason);
            // Send chat message
        }
        // Don't clear targetButton here, it's cleared by screen events
    }

    // This method will be called every client tick by an event handler
    public void clientTick() {
        if (!isRunning.get()) {
            return;
        }

        // Basic checks
        if (!(Minecraft.getInstance().screen instanceof MerchantScreen screen) || targetButton == null) {
            stop("Screen closed or button lost");
            return;
        }

        if (currentCycles++ > MAX_CYCLES_SAFETY) {
            stop("Max cycles safety limit reached");
            return;
        }

        // Handle delay
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        // --- Action Phase ---
        MerchantOffers offers = screen.getMenu().getOffers();

        // 1. Check if current trades match criteria
        if (checkTrades(offers)) {
            EasyAutoCyclerMod.LOGGER.info("Target trade FOUND!");
            // Send chat message to player
            stop("Target trade found");
            return; // Stop!
        }

        // 2. If not found, try to cycle
        if (!targetButton.visible || !targetButton.active) {
            // Button isn't clickable (e.g., villager leveled up, trade selected)
            // We could stop, or just wait. Waiting might be better.
            EasyAutoCyclerMod.LOGGER.debug("Cycle button not active, waiting...");
            // Don't set delay here, just wait for next tick check
            return;
        }

        // 3. Click the button
        EasyAutoCyclerMod.LOGGER.debug("Clicking Cycle Trades button (Cycle {})", currentCycles);
        targetButton.onPress();

        // 4. Set delay to allow trades to refresh
        delayTicks = CLICK_DELAY;
    }

    private boolean checkTrades(MerchantOffers offers) {
        // TODO: Implement your specific trade checking logic here!
        // Iterate through offers: for (MerchantOffer offer : offers) { ... }
        // Check offer.getResult() Item, enchantments, count
        // Check offer.getCostA(), offer.getCostB() (first input, second input) Item, count
        // Check if offer.isOutOfStock() or offer.getMaxUses() - offer.getUses() > 0

        // Example: Look for Mending book for <= 20 Emeralds
        /*
        for (MerchantOffer offer : offers) {
            ItemStack result = offer.getResult();
            if (result.is(Items.ENCHANTED_BOOK)) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
                if (enchantments.containsKey(Enchantments.MENDING)) {
                    ItemStack cost = offer.getCostA();
                    if (cost.is(Items.EMERALD) && cost.getCount() <= 20 && !offer.isOutOfStock()) {
                         return true; // Found it!
                    }
                }
            }
        }
        */
        return false; // Placeholder
    }
}