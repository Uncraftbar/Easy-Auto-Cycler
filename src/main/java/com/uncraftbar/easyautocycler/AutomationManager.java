package com.uncraftbar.easyautocycler;

import de.maxhenkel.easyvillagers.gui.CycleTradesButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.core.Holder;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutomationManager {

    public static final AutomationManager INSTANCE = new AutomationManager();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private CycleTradesButton targetButton = null;
    private int delayTicks = 0;
    private int currentCycles = 0;
    private static final int MAX_CYCLES_SAFETY = 3000;
    private static final int CLICK_DELAY = 2;

    // --- Configuration Fields ---
    @Nullable
    private Enchantment targetEnchantment = null;
    @Nullable // Store the ID alongside the enchantment object
    private ResourceLocation targetEnchantmentId = null;
    private int maxEmeraldCost = 64;
    private int targetLevel = 1;

    private AutomationManager() {}

    // --- Getters for Status ---
    public boolean isRunning() { return isRunning.get(); }
    @Nullable public Enchantment getTargetEnchantment() { return targetEnchantment; }
    @Nullable public ResourceLocation getTargetEnchantmentId() { return targetEnchantmentId; } // Getter for ID
    public int getMaxEmeraldCost() { return maxEmeraldCost; }
    public int getTargetLevel() { return targetLevel; }

    // --- Internal State Management ---
    public void setTargetButton(CycleTradesButton button) {
        this.targetButton = button;
        EasyAutoCyclerMod.LOGGER.trace("Target button set: {}", button != null);
    }

    public void clearTargetButton() {
        this.targetButton = null;
        if (isRunning.get()) {
            stop("GUI got closed");
        }
    }

    // --- Public Controls ---
    public void configureTarget(Enchantment enchantment, ResourceLocation enchantmentId, int level, int emeraldCost) {
        this.targetEnchantment = enchantment;
        this.targetEnchantmentId = enchantmentId;
        this.targetLevel = level;
        this.maxEmeraldCost = emeraldCost;

        String message = String.format("Set target: Enchantment=%s, Level=%d, MaxEmeralds=%d",
                enchantmentId.toString(), level, emeraldCost);
        EasyAutoCyclerMod.LOGGER.info(message);
        sendMessageToPlayer(Component.literal(message));
    }

    public void clearTarget() {
        this.targetEnchantment = null;
        this.targetEnchantmentId = null; // Clear the ID too
        EasyAutoCyclerMod.LOGGER.info("Cleared target trade.");
        sendMessageToPlayer(Component.literal("Cleared target trade. Automation will not stop automatically."));
    }

    public void toggle() {
        if (isRunning.get()) {
            stop("Toggled off by user");
        } else {
            start();
        }
    }


    private void start() {
        Screen currentScreen = Minecraft.getInstance().screen;
        String screenName = (currentScreen != null) ? currentScreen.getClass().getName() : "null";

        if (!(currentScreen instanceof MerchantScreen)) { // Check against the variable we just logged
            sendMessageToPlayer(Component.literal("Error: Villager trade screen not open."));
            EasyAutoCyclerMod.LOGGER.warn("Cannot start: Screen check failed. Screen was: {}", screenName);
            return;
        }
        if (targetButton == null) {
            sendMessageToPlayer(Component.literal("Error: Cycle button not found (Easy Villagers issue?)."));
            EasyAutoCyclerMod.LOGGER.warn("Cannot start: Button not found.");
            return;
        }
        if (targetEnchantment == null) {
            sendMessageToPlayer(Component.literal("Warning: No target trade configured. Cycling will not stop automatically."));
            EasyAutoCyclerMod.LOGGER.warn("Starting cycle without target definition.");
        }

        if (isRunning.compareAndSet(false, true)) {
            EasyAutoCyclerMod.LOGGER.info("Starting villager trade cycling.");
            sendMessageToPlayer(Component.literal("Auto-cycling started. Press keybind again to stop."));
            this.delayTicks = 0;
            this.currentCycles = 0;
        }
    }

    @Nullable // Mark it as nullable just in case
    public CycleTradesButton getTargetButton() {
        return this.targetButton;
    }

    public void stop(String reason) {
        if (isRunning.compareAndSet(true, false)) {
            EasyAutoCyclerMod.LOGGER.info("Stopping villager trade cycling. Reason: {}", reason);
            sendMessageToPlayer(Component.literal("Auto-cycling stopped: " + reason));
        }
    }

    // --- Core Loop Logic (clientTick) ---
    public void clientTick() {
        if (!isRunning.get()) return;

        if (!(Minecraft.getInstance().screen instanceof MerchantScreen screen) || targetButton == null) {
            stop("Screen closed or button lost");
            return;
        }

        currentCycles++;
        if (currentCycles > MAX_CYCLES_SAFETY) {
            stop("Max cycles safety limit reached");
            return;
        }

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        MerchantOffers offers = screen.getMenu().getOffers();
        if (targetEnchantment != null && checkTrades(offers)) {
            EasyAutoCyclerMod.LOGGER.info("Target trade FOUND!");
            sendMessageToPlayer(Component.literal("§aTarget trade found!"));
            stop("Target trade found");
            return;
        }

        if (!targetButton.visible || !targetButton.active) {
            EasyAutoCyclerMod.LOGGER.trace("Cycle button not active, waiting...");
            return;
        }

        EasyAutoCyclerMod.LOGGER.trace("Clicking Cycle Trades button (Cycle {})", currentCycles);
        targetButton.onPress();
        delayTicks = CLICK_DELAY;
    }

    private boolean checkTrades(MerchantOffers offers) {
        if (targetEnchantment == null) return false;

        for (MerchantOffer offer : offers) {
            ItemStack resultStack = offer.getResult();
            if (!resultStack.is(Items.ENCHANTED_BOOK)) continue;
            if (offer.isOutOfStock()) continue;

            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            final int requiredBookCost = 1;

            if (costA.is(Items.EMERALD) && costA.getCount() <= this.maxEmeraldCost) {
                // If A is emeralds within price, B must be exactly 1 book
                if (!costB.is(Items.BOOK) || costB.getCount() != requiredBookCost) { // Check for == 1 book
                    continue;
                }
            } else if (costB.is(Items.EMERALD) && costB.getCount() <= this.maxEmeraldCost) {
                // If B is emeralds within price, A must be exactly 1 book
                if (!costA.is(Items.BOOK) || costA.getCount() != requiredBookCost) { // Check for == 1 book
                    continue;
                }
            } else {
                continue;
            }

            ItemEnchantments enchantments = resultStack.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchantments == null) continue;

            boolean foundMatchingEnchantment = false;

            for (Holder<Enchantment> enchHolder : enchantments.keySet()) {
                Enchantment ench = enchHolder.value();

                if (ench.equals(this.targetEnchantment)) {
                    int level = enchantments.getLevel(enchHolder);
                    if (level == this.targetLevel) {
                        foundMatchingEnchantment = true;
                        break;
                    }
                }
            }

            if (foundMatchingEnchantment) return true;
        }
        return false;
    }

    private void sendMessageToPlayer(Component message) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(message);
        }
    }
}