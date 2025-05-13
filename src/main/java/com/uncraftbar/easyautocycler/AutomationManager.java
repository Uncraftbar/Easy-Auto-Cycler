package com.uncraftbar.easyautocycler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

    // Mod integration handlers
    private static boolean initialized = false;
    private static boolean easyVillagersLoaded = false;
    private static boolean tradeCyclingLoaded = false;
    private static Object easyVillagersHandler = null;
    private static Object tradeCyclingHandler = null;

    // Handler classes for each mod
    private static class EasyVillagersHandler {
        private final Class<?> buttonClass;
        private final Method canCycleMethod;
        private final Constructor<?> packetConstructor;
        
        public EasyVillagersHandler() throws Exception {
            buttonClass = Class.forName("de.maxhenkel.easyvillagers.gui.CycleTradesButton");
            canCycleMethod = buttonClass.getMethod("canCycle", MerchantMenu.class);
            Class<?> packetClass = Class.forName("de.maxhenkel.easyvillagers.net.MessageCycleTrades");
            packetConstructor = packetClass.getDeclaredConstructor();
        }
        
        public boolean canCycle(MerchantMenu menu) {
            try {
                return (boolean) canCycleMethod.invoke(null, menu);
            } catch (Exception e) {
                EasyAutoCyclerMod.LOGGER.error("Error calling Easy Villagers canCycle", e);
                return false;
            }
        }
        
        public void sendCyclePacket() {
            try {
                Object packet = packetConstructor.newInstance();
                PacketDistributor.sendToServer((CustomPacketPayload)packet);
                EasyAutoCyclerMod.LOGGER.trace("Sent Easy Villagers cycle packet");
            } catch (Exception e) {
                EasyAutoCyclerMod.LOGGER.error("Failed to send Easy Villagers packet", e);
            }
        }
    }

    private static class TradeCyclingHandler {
        private final Class<?> buttonClass;
        private final Method canCycleMethod;
        private final Constructor<?> packetConstructor;
        
        public TradeCyclingHandler() throws Exception {
            buttonClass = Class.forName("de.maxhenkel.tradecycling.gui.CycleTradesButton");
            canCycleMethod = buttonClass.getMethod("canCycle", MerchantMenu.class);
            Class<?> packetClass = Class.forName("de.maxhenkel.tradecycling.net.CycleTradesPacket");
            packetConstructor = packetClass.getDeclaredConstructor();
        }
        
        public boolean canCycle(MerchantMenu menu) {
            try {
                return (boolean) canCycleMethod.invoke(null, menu);
            } catch (Exception e) {
                EasyAutoCyclerMod.LOGGER.error("Error calling Trade Cycling canCycle", e);
                return false;
            }
        }
        
        public void sendCyclePacket() {
            try {
                Object packet = packetConstructor.newInstance();
                PacketDistributor.sendToServer((CustomPacketPayload)packet);
                EasyAutoCyclerMod.LOGGER.trace("Sent Trade Cycling cycle packet");
            } catch (Exception e) {
                EasyAutoCyclerMod.LOGGER.error("Failed to send Trade Cycling packet", e);
            }
        }
    }

    // Initialize mod handlers - call this in mod initialization, not in static initializer
    public static void initialize() {
        if (initialized) return;
        initialized = true;
        
        // Check if mods are actually loaded using ModList
        easyVillagersLoaded = ModList.get().isLoaded("easy_villagers");
        tradeCyclingLoaded = ModList.get().isLoaded("trade_cycling");
        
        EasyAutoCyclerMod.LOGGER.info("Easy Villagers mod is {}", easyVillagersLoaded ? "loaded" : "not loaded");
        EasyAutoCyclerMod.LOGGER.info("Trade Cycling mod is {}", tradeCyclingLoaded ? "loaded" : "not loaded");
        
        // Initialize handlers for loaded mods
        if (easyVillagersLoaded) {
            try {
                easyVillagersHandler = new EasyVillagersHandler();
                EasyAutoCyclerMod.LOGGER.info("Easy Villagers support enabled");
            } catch (Exception e) {
                easyVillagersLoaded = false;
                EasyAutoCyclerMod.LOGGER.error("Failed to initialize Easy Villagers support: {}", e.getMessage());
            }
        }
        
        if (tradeCyclingLoaded) {
            try {
                tradeCyclingHandler = new TradeCyclingHandler();
                EasyAutoCyclerMod.LOGGER.info("Trade Cycling support enabled");
            } catch (Exception e) {
                tradeCyclingLoaded = false;
                EasyAutoCyclerMod.LOGGER.error("Failed to initialize Trade Cycling support: {}", e.getMessage());
            }
        }
        
        if (!easyVillagersLoaded && !tradeCyclingLoaded) {
            EasyAutoCyclerMod.LOGGER.warn("No supported trading mods detected! This mod requires either Easy Villagers or Trade Cycling.");
        }
    }

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
        this.maxEmeraldCost = emeraldCost;
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
        this.sendMessageToPlayer(Component.literal("Cleared target trade. Automation will not stop automatically."));
    }

    public void toggle() { if (isRunning.get()) { stop("Toggled off by user"); } else { start(); } }

    private void start() {
        Screen currentScreen = Minecraft.getInstance().screen;
        String screenName = (currentScreen != null) ? currentScreen.getClass().getName() : "null";
        EasyAutoCyclerMod.LOGGER.debug("AutomationManager.start(): Checking screen. Current screen is: {}", screenName);
        
        if (!initialized) {
            this.sendMessageToPlayer(Component.literal("§cError: Mod not properly initialized."));
            EasyAutoCyclerMod.LOGGER.error("Cannot start: AutomationManager not initialized");
            return;
        }
        
        if (!easyVillagersLoaded && !tradeCyclingLoaded) {
            this.sendMessageToPlayer(Component.literal("§cError: No supported trading mod detected."));
            EasyAutoCyclerMod.LOGGER.error("Cannot start: No supported trading mod detected");
            return;
        }
        
        if (!(currentScreen instanceof MerchantScreen)) {
            this.sendMessageToPlayer(Component.literal("Error: Villager trade screen not open."));
            EasyAutoCyclerMod.LOGGER.warn("Cannot start: Screen check failed. Screen was: {}", screenName); 
            return; 
        }
        
        if (targetEnchantment == null) {
            this.sendMessageToPlayer(Component.literal("Warning: No target trade configured. Cycling will not stop automatically."));
            EasyAutoCyclerMod.LOGGER.warn("Starting cycle without target definition."); 
        }
        
        if (isRunning.compareAndSet(false, true)) {
            EasyAutoCyclerMod.LOGGER.debug("Starting villager trade cycling. Delay: {} ticks.", this.clickDelay);
            this.sendMessageToPlayer(Component.literal("Auto-cycling started. Press button again to stop."));
            this.delayTicks = 0;
            this.currentCycles = 0; 
        }
    }

    public void stop(String reason) { 
        if (isRunning.compareAndSet(true, false)) { 
            EasyAutoCyclerMod.LOGGER.debug("Stopping villager trade cycling. Reason: {}", reason); 
        } 
    }

    // Simplified canCycle method using handlers
    private boolean canCycle(AbstractContainerMenu menu) {
        if (!initialized) return false;
        
        if (!(menu instanceof MerchantMenu merchantMenu)) {
            return false;
        }
        
        if (easyVillagersLoaded && easyVillagersHandler != null) {
            return ((EasyVillagersHandler)easyVillagersHandler).canCycle(merchantMenu);
        } else if (tradeCyclingLoaded && tradeCyclingHandler != null) {
            return ((TradeCyclingHandler)tradeCyclingHandler).canCycle(merchantMenu);
        }
        
        return false;
    }

    public void clientTick() {
        if (!isRunning.get()) return;
        if (!initialized) {
            stop("Not initialized");
            return;
        }
        
        if (!(Minecraft.getInstance().screen instanceof MerchantScreen screen)) { 
            stop("Screen closed"); 
            return; 
        }
        
        currentCycles++; 
        if (currentCycles > MAX_CYCLES_SAFETY) {
            EasyAutoCyclerMod.LOGGER.debug("Max cycles safety limit reached!");
            this.sendMessageToPlayer(Component.literal("§cMax cycles safety limit reached!"));
            try { 
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.getSoundManager() != null) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, 1.0F)); 
                } 
            } catch (Exception e) { 
                EasyAutoCyclerMod.LOGGER.error("Failed to play 'trade found' sound effect", e); 
            } 
            stop("Max cycles safety limit reached!"); 
            return; 
        }
        
        if (delayTicks > 0) { 
            delayTicks--; 
            return; 
        }
        
        MerchantOffers offers = screen.getMenu().getOffers();
        if (targetEnchantment != null && checkTrades(offers)) {
            EasyAutoCyclerMod.LOGGER.debug("Target trade FOUND!");
            this.sendMessageToPlayer(Component.literal("§aTarget trade found!"));
            try { 
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.getSoundManager() != null) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0F)); 
                } 
            } catch (Exception e) { 
                EasyAutoCyclerMod.LOGGER.error("Failed to play 'trade found' sound effect", e); 
            } 
            stop("Target trade found"); 
            return; 
        }
        
        if (canCycle(screen.getMenu())) {
            try {
                EasyAutoCyclerMod.LOGGER.trace("Conditions met, sending cycle trades packet (Cycle {})", currentCycles);
                
                if (easyVillagersLoaded && easyVillagersHandler != null) {
                    ((EasyVillagersHandler)easyVillagersHandler).sendCyclePacket();
                } else if (tradeCyclingLoaded && tradeCyclingHandler != null) {
                    ((TradeCyclingHandler)tradeCyclingHandler).sendCyclePacket();
                }
                
                delayTicks = this.clickDelay;
            } catch(Exception e) {
                EasyAutoCyclerMod.LOGGER.error("Failed to send cycle trades packet!", e);
                stop("Network error");
            }
        } else {
            EasyAutoCyclerMod.LOGGER.trace("canCycle() returned false, waiting...");
        }
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
                if (!costB.is(Items.BOOK) || costB.getCount() != requiredBookCost) continue;
            } else if (costB.is(Items.EMERALD) && costB.getCount() <= this.maxEmeraldCost) {
                if (!costA.is(Items.BOOK) || costA.getCount() != requiredBookCost) continue; 
            } else { 
                continue; 
            } 
            
            ItemEnchantments enchantments = resultStack.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchantments == null || enchantments.isEmpty()) continue;
            
            boolean foundMatchingEnchantment = false;
            for (Holder<Enchantment> enchHolder : enchantments.keySet()) { 
                Enchantment ench = enchHolder.value();
                int level = enchantments.getLevel(enchHolder);
                if (ench.equals(this.targetEnchantment) && level == this.targetLevel) { 
                    foundMatchingEnchantment = true; 
                    break; 
                } 
            }
            
            if (foundMatchingEnchantment) return true; 
        } 
        
        return false; 
    }

    private void sendMessageToPlayer(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) { 
            mc.player.sendSystemMessage(message); 
        }
    }
}