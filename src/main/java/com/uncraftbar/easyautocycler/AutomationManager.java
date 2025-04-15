package com.uncraftbar.easyautocycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import de.maxhenkel.easyvillagers.gui.CycleTradesButton;
import de.maxhenkel.easyvillagers.net.MessageCycleTrades;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;


public class AutomationManager {

    public static final AutomationManager INSTANCE = new AutomationManager();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int delayTicks = 0;
    private int currentCycles = 0;
    private static final int MAX_CYCLES_SAFETY = 3000;

    public static final int DEFAULT_CLICK_DELAY = 2;
    public static final int MIN_CLICK_DELAY = 1;
    public static final int MAX_CLICK_DELAY = 5;

    @Nullable private Enchantment targetEnchantment = null;
    @Nullable private ResourceLocation targetEnchantmentId = null;
    private int maxEmeraldCost = 64;
    private int targetLevel = 1;
    private int clickDelay = DEFAULT_CLICK_DELAY; // Default value

    private AutomationManager() {}

    public boolean isRunning() { return isRunning.get(); }
    @Nullable public Enchantment getTargetEnchantment() { return targetEnchantment; }
    @Nullable public ResourceLocation getTargetEnchantmentId() { return targetEnchantmentId; }
    public int getMaxEmeraldCost() { return maxEmeraldCost; }
    public int getTargetLevel() { return targetLevel; }
    public int getClickDelay() { return clickDelay; }

    public void configureTarget(Enchantment enchantment, ResourceLocation enchantmentId, int level, int emeraldCost) {
        this.targetEnchantment = enchantment;
        this.targetEnchantmentId = enchantmentId;
        this.targetLevel = level;
        this.maxEmeraldCost = emeraldCost;// Use correct method name
    }

    public void configureSpeed(int delay) {
        this.clickDelay = Math.max(MIN_CLICK_DELAY, Math.min(MAX_CLICK_DELAY, delay));
        String message = String.format("Set cycle delay to %d ticks.", this.clickDelay);
        EasyAutoCyclerMod.LOGGER.info(message);
    }

    public void clearTarget() {
        this.targetEnchantment = null;
        this.targetEnchantmentId = null;
        EasyAutoCyclerMod.LOGGER.info("Cleared target trade.");
        this.sendMessageToPlayer(Component.literal("Cleared target trade. Automation will not stop automatically.")); // Use correct method name
    }

    public void toggle() { if (isRunning.get()) { stop("Toggled off by user"); } else { start(); } }

    private void start() {
        Screen currentScreen = Minecraft.getInstance().screen;
        String screenName = (currentScreen != null) ? currentScreen.getClass().getName() : "null";
        EasyAutoCyclerMod.LOGGER.debug("AutomationManager.start(): Checking screen. Current screen is: {}", screenName);
        if (!(currentScreen instanceof MerchantScreen)) {
            this.sendMessageToPlayer(Component.literal("Error: Villager trade screen not open."));
            EasyAutoCyclerMod.LOGGER.warn("Cannot start: Screen check failed. Screen was: {}", screenName); return; }
        if (targetEnchantment == null) {
            this.sendMessageToPlayer(Component.literal("Warning: No target trade configured. Cycling will not stop automatically."));
            EasyAutoCyclerMod.LOGGER.warn("Starting cycle without target definition."); }
        if (isRunning.compareAndSet(false, true)) {
            EasyAutoCyclerMod.LOGGER.debug("Starting villager trade cycling. Delay: {} ticks.", this.clickDelay);
            this.sendMessageToPlayer(Component.literal("Auto-cycling started. Press button again to stop."));
            this.delayTicks = 0;
            this.currentCycles = 0; }
    }

    public void stop(String reason) { if (isRunning.compareAndSet(true, false)) { EasyAutoCyclerMod.LOGGER.debug("Stopping villager trade cycling. Reason: {}", reason); } }

    public void clientTick() {
        if (!isRunning.get()) return;
        if (!(Minecraft.getInstance().screen instanceof MerchantScreen screen)) { stop("Screen closed"); return; }
        currentCycles++; if (currentCycles > MAX_CYCLES_SAFETY) {
            EasyAutoCyclerMod.LOGGER.debug("Max cycles safety limit reached!");
            this.sendMessageToPlayer(Component.literal("§cMax cycles safety limit reached!"));
            try { Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.getSoundManager() != null) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, 1.0F)); } }
            catch (Exception e) { EasyAutoCyclerMod.LOGGER.error("Failed to play 'trade found' sound effect", e); } stop("Max cycles safety limit reached!"); return; }
        if (delayTicks > 0) { delayTicks--; return; }
        MerchantOffers offers = screen.getMenu().getOffers();
        if (targetEnchantment != null && checkTrades(offers)) {
            EasyAutoCyclerMod.LOGGER.debug("Target trade FOUND!");
            this.sendMessageToPlayer(Component.literal("§aTarget trade found!"));
            try { Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.getSoundManager() != null) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0F)); } }
            catch (Exception e) { EasyAutoCyclerMod.LOGGER.error("Failed to play 'trade found' sound effect", e); } stop("Target trade found"); return; }
        if (CycleTradesButton.canCycle(screen.getMenu())) {
            try { EasyAutoCyclerMod.LOGGER.trace("Conditions met, sending MessageCycleTrades packet (Cycle {})", currentCycles);
                PacketDistributor.sendToServer(new MessageCycleTrades()); delayTicks = this.clickDelay; }
            catch(Exception e) { EasyAutoCyclerMod.LOGGER.error("Failed to send MessageCycleTrades packet via PacketDistributor!", e);
                stop("Network error"); } } else { EasyAutoCyclerMod.LOGGER.trace("CycleTradesButton.canCycle() returned false, waiting..."); }
    }

    private boolean checkTrades(MerchantOffers offers) {
        if (targetEnchantment == null) return false;
        for (MerchantOffer offer : offers) { ItemStack resultStack = offer.getResult();
            if (!resultStack.is(Items.ENCHANTED_BOOK)) continue;
            if (offer.isOutOfStock()) continue;
            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            final int requiredBookCost = 1;
            if (costA.is(Items.EMERALD) && costA.getCount() <= this.maxEmeraldCost) {
                if (!costB.is(Items.BOOK) || costB.getCount() != requiredBookCost) continue;
            } else if (costB.is(Items.EMERALD) && costB.getCount() <= this.maxEmeraldCost) {
                if (!costA.is(Items.BOOK) || costA.getCount() != requiredBookCost) continue; }
            else { continue; } ItemEnchantments enchantments = resultStack.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchantments == null || enchantments.isEmpty()) continue;
            boolean foundMatchingEnchantment = false;
            for (Holder<Enchantment> enchHolder : enchantments.keySet()) { Enchantment ench = enchHolder.value();
                int level = enchantments.getLevel(enchHolder);
                if (ench.equals(this.targetEnchantment) && level == this.targetLevel) { foundMatchingEnchantment = true; break; } }
            if (foundMatchingEnchantment) return true; } return false; }

    private void sendMessageToPlayer(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) { mc.player.sendSystemMessage(message); }
    }
}