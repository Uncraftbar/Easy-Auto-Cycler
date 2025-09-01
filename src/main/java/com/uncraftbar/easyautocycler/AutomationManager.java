package com.uncraftbar.easyautocycler;

import com.uncraftbar.easyautocycler.filter.FilterEntry;
import com.uncraftbar.easyautocycler.config.FilterConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import de.maxhenkel.easyvillagers.Main;
import de.maxhenkel.easyvillagers.gui.CycleTradesButton;
import de.maxhenkel.easyvillagers.net.MessageCycleTrades;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AutomationManager {

    public static final AutomationManager INSTANCE = new AutomationManager();
    
    // Load filters on class initialization
    static {
        INSTANCE.loadFiltersFromConfig();
    }

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int delayTicks = 0;
    private int currentCycles = 0;
    private static final int MAX_CYCLES_SAFETY = 3000;

    public static final int DEFAULT_CLICK_DELAY = 2;
    public static final int MIN_CLICK_DELAY = 1;
    public static final int MAX_CLICK_DELAY = 5;
    
    // Mode constants for cycling
    public static final int MODE_ENCHANTMENT = 0;
    public static final int MODE_ITEM = 1;
    private int cycleMode = MODE_ENCHANTMENT; // Default to enchantment mode
    
    // Legacy fields (kept for backward compatibility)
    @Nullable private Enchantment targetEnchantment = null;
    @Nullable private ResourceLocation targetEnchantmentId = null;
    @Nullable private ResourceLocation targetItemId = null;
    private int maxEmeraldCost = 64;
    private int targetLevel = 1;
    private int clickDelay = DEFAULT_CLICK_DELAY;
    private int targetItemCount = 1;
    
    // New multi-filter system
    private List<FilterEntry> filterEntries = new ArrayList<>();
    private boolean matchAny = true; // true = OR logic, false = AND logic
    private FilterEntry lastMatchedFilter = null;

    private AutomationManager() {}

    public boolean isRunning() { return isRunning.get(); }
    @Nullable public Enchantment getTargetEnchantment() { return targetEnchantment; }
    @Nullable public ResourceLocation getTargetEnchantmentId() { return targetEnchantmentId; }
    @Nullable public ResourceLocation getTargetItemId() { return targetItemId; }
    public int getMaxEmeraldCost() { return maxEmeraldCost; }
    public int getTargetLevel() { return targetLevel; }
    public int getClickDelay() { return clickDelay; }
    public int getCycleMode() { return cycleMode; }
    public int getTargetItemCount() { return targetItemCount; }

    public void configureTarget(Enchantment enchantment, ResourceLocation enchantmentId, int level, int emeraldCost) {
        this.targetEnchantment = enchantment;
        this.targetEnchantmentId = enchantmentId;
        this.targetLevel = level;
        this.maxEmeraldCost = emeraldCost;
        this.cycleMode = MODE_ENCHANTMENT;
        
        // Create a filter entry for the new filter system
        FilterEntry entry = new FilterEntry();
        entry.setEnchantmentId(enchantmentId);
        entry.setEnchantmentLevel(level);
        entry.setMaxPrice(emeraldCost);
        
        this.filterEntries.clear();
        this.filterEntries.add(entry);
    }

    public void configureTargetItem(ResourceLocation itemId, int itemCount, int emeraldCost) {
        this.targetItemId = itemId;
        this.targetItemCount = itemCount;
        this.maxEmeraldCost = emeraldCost;
        this.cycleMode = MODE_ITEM;
        
        // Create a filter entry for the new filter system
        FilterEntry entry = new FilterEntry();
        entry.setItemId(itemId);
        entry.setMinCount(itemCount);
        entry.setMaxPrice(emeraldCost);
        
        this.filterEntries.clear();
        this.filterEntries.add(entry);
    }
    
    // New filter system getters and setters
    public List<FilterEntry> getFilterEntries() {
        // Migrate old configuration if necessary
        migrateOldConfigToFilters();
        return new ArrayList<>(filterEntries);
    }
    
    public void setFilterEntries(List<FilterEntry> entries) {
        if (entries == null) {
            this.filterEntries = new ArrayList<>();
        } else {
            this.filterEntries = new ArrayList<>(entries);
        }
        // Clear old config as it's been replaced by the filter system
        this.targetEnchantment = null;
        this.targetEnchantmentId = null;
        this.targetItemId = null;
        
        // Auto-save filters to maintain session persistence
        saveFiltersToConfig();
    }
    
    public boolean isMatchAny() {
        return matchAny;
    }
    
    public void setMatchAny(boolean matchAny) {
        this.matchAny = matchAny;
    }
    
    @Nullable
    public FilterEntry getLastMatchedFilter() {
        return lastMatchedFilter;
    }
    
    /**
     * Migrate from old single-target system to new filter system
     */
    private void migrateOldConfigToFilters() {
        if (filterEntries.isEmpty() && (targetEnchantment != null || targetItemId != null)) {
            FilterEntry entry = new FilterEntry();
            
            if (targetEnchantment != null) {
                entry.setEnchantmentId(targetEnchantmentId);
                entry.setEnchantmentLevel(targetLevel);
            }
            
            if (targetItemId != null) {
                entry.setItemId(targetItemId);
                entry.setMinCount(targetItemCount);
            }
            
            entry.setMaxPrice(maxEmeraldCost);
            filterEntries.add(entry);
        }
    }

    public void configureSpeed(int delay) {
        this.clickDelay = Math.max(MIN_CLICK_DELAY, Math.min(MAX_CLICK_DELAY, delay));
        String message = String.format("Set cycle delay to %d ticks.", this.clickDelay);
        EasyAutoCyclerMod.LOGGER.info(message);
    }

    public void clearTarget() {
        this.targetEnchantment = null;
        this.targetEnchantmentId = null;
        this.targetItemId = null;
        this.filterEntries.clear();
        this.lastMatchedFilter = null;
        EasyAutoCyclerMod.LOGGER.info("Cleared target trade.");
        this.sendMessageToPlayer(Component.literal("Cleared target trade. Automation will not stop automatically."));
    }

    public void toggle() { if (isRunning.get()) { stop("Toggled off by user"); } else { start(); } }

    private void start() {
        Screen currentScreen = Minecraft.getInstance().screen;
        String screenName = (currentScreen != null) ? currentScreen.getClass().getName() : "null";
        EasyAutoCyclerMod.LOGGER.debug("AutomationManager.start(): Checking screen. Current screen is: {}", screenName);
        
        if (!(currentScreen instanceof MerchantScreen)) {
            this.sendMessageToPlayer(Component.literal("Error: Villager trade screen not open."));
            EasyAutoCyclerMod.LOGGER.warn("Cannot start: Screen check failed. Screen was: {}", screenName); 
            return; 
        }
        
        // Check if we have any filters
        migrateOldConfigToFilters();
        if (filterEntries.isEmpty()) {
            this.sendMessageToPlayer(Component.literal("Warning: No filters configured. Cycling will not stop automatically."));
            EasyAutoCyclerMod.LOGGER.warn("Starting cycle without filters defined."); 
        }
        
        if (isRunning.compareAndSet(false, true)) {
            EasyAutoCyclerMod.LOGGER.debug("Starting villager trade cycling. Delay: {} ticks.", this.clickDelay);
            this.sendMessageToPlayer(Component.literal("Auto-cycling started. Press button again to stop."));
            this.delayTicks = 0;
            this.currentCycles = 0;
            this.lastMatchedFilter = null;
        }
        
        // Check if we have any enabled filters or legacy targets
        List<FilterEntry> enabledFilters = filterEntries.stream()
            .filter(FilterEntry::isEnabled)
            .collect(Collectors.toList());
            
        if (enabledFilters.isEmpty() && targetEnchantment == null && targetItemId == null) {
            this.sendMessageToPlayer(Component.literal("Warning: No target trade configured. Cycling will not stop automatically."));
            EasyAutoCyclerMod.LOGGER.warn("Starting cycle without target definition."); 
        }
    }

    public void stop(String reason) { 
        if (isRunning.compareAndSet(true, false)) { 
            EasyAutoCyclerMod.LOGGER.debug("Stopping villager trade cycling. Reason: {}", reason); 
        } 
    }

    public void clientTick() {
        if (!isRunning.get()) return;
        
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
        
        // Check for trades using the new filter system first
        List<FilterEntry> enabledFilters = filterEntries.stream()
            .filter(FilterEntry::isEnabled)
            .collect(Collectors.toList());
            
        if (!enabledFilters.isEmpty() && checkTradesWithFilters(offers)) {
            Component message = Component.empty()
                .append(Component.literal("§aTarget trade found: "))
                .append(this.lastMatchedFilter.getDisplayName());
                
            EasyAutoCyclerMod.LOGGER.debug("Target trade FOUND using filter!");
            this.sendMessageToPlayer(message);
            playSuccessSound();
            stop("Target trade found with filter"); 
            return;
        }
        // Legacy system support (only if no enabled filters exist)
        else if (enabledFilters.isEmpty()) {
            if (cycleMode == MODE_ENCHANTMENT && targetEnchantment != null && checkTradesForEnchantment(offers)) {
                EasyAutoCyclerMod.LOGGER.debug("Target trade FOUND!");
                this.sendMessageToPlayer(Component.literal("§aTarget trade found!"));
                playSuccessSound();
                stop("Target trade found"); 
                return; 
            } else if (cycleMode == MODE_ITEM && targetItemId != null && checkTradesForItem(offers)) {
                EasyAutoCyclerMod.LOGGER.debug("Target item trade FOUND!");
                this.sendMessageToPlayer(Component.literal("§aTarget item trade found!"));
                playSuccessSound();
                stop("Target item trade found"); 
                return; 
            }
        }
        
        // Direct Easy Villagers integration (no reflection needed)
        if (CycleTradesButton.canCycle(screen.getMenu())) {
            try {
                EasyAutoCyclerMod.LOGGER.trace("Conditions met, sending cycle trades packet (Cycle {})", currentCycles);
                Main.SIMPLE_CHANNEL.sendToServer(new MessageCycleTrades());
                delayTicks = this.clickDelay;
            } catch(Exception e) {
                EasyAutoCyclerMod.LOGGER.error("Failed to send cycle trades packet!", e);
                stop("Network error");
            }
        } else {
            EasyAutoCyclerMod.LOGGER.trace("canCycle() returned false, waiting...");
        }
    }

    private boolean checkTradesForEnchantment(MerchantOffers offers) {
        if (targetEnchantment == null) return false;
        for (MerchantOffer offer : offers) { 
            ItemStack resultStack = offer.getResult();
            if (offer.isOutOfStock()) continue;
            
            // Check emerald cost
            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            
            if (costA.is(Items.EMERALD) && costA.getCount() <= this.maxEmeraldCost) {
                // Cost A is emeralds and within our limit - proceed
            } else if (costB.is(Items.EMERALD) && costB.getCount() <= this.maxEmeraldCost) {
                // Cost B is emeralds and within our limit - proceed
            } else { 
                continue; 
            }
            
            // Check for enchantments using EnchantmentHelper (1.20.1 compatible)
            boolean foundMatchingEnchantment = false;
            
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(resultStack);
            if (!enchantments.isEmpty()) {
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment ench = entry.getKey();
                    int level = entry.getValue();
                    if (ench.equals(this.targetEnchantment) && level == this.targetLevel) { 
                        foundMatchingEnchantment = true; 
                        break; 
                    } 
                }
            }
            
            if (foundMatchingEnchantment) return true; 
        } 
        
        return false;
    }
    
    private boolean checkTradesForItem(MerchantOffers offers) {
        if (targetItemId == null) return false;
        for (MerchantOffer offer : offers) { 
            ItemStack resultStack = offer.getResult();
            
            // Check if the item ID matches the target
            ResourceLocation itemIdInStack = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(Registries.ITEM).getKey(resultStack.getItem());
            if (!itemIdInStack.equals(targetItemId)) continue;
            
            if (offer.isOutOfStock()) continue;
            
            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            
            if (costA.is(Items.EMERALD) && costA.getCount() <= this.maxEmeraldCost) {
                // Allow any item as secondary cost for general items
            } else if (costB.is(Items.EMERALD) && costB.getCount() <= this.maxEmeraldCost) {
                // Allow any item as primary cost for general items
            } else { 
                continue; 
            } 
            
            if (resultStack.getCount() >= this.targetItemCount) return true; 
        } 
        
        return false; 
    }

    private void sendMessageToPlayer(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) { 
            mc.player.sendSystemMessage(message); 
        }
    }
    
    /**
     * Play the success sound when a target trade is found
     */
    private void playSuccessSound() {
        try { 
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getSoundManager() != null) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0F)); 
            } 
        } catch (Exception e) { 
            EasyAutoCyclerMod.LOGGER.error("Failed to play 'trade found' sound effect", e); 
        }
    }
    
    /**
     * Check trades using the new filter system
     */
    private boolean checkTradesWithFilters(MerchantOffers offers) {
        if (filterEntries.isEmpty()) return false;
        
        List<FilterEntry> enabledFilters = filterEntries.stream()
            .filter(FilterEntry::isEnabled)
            .collect(Collectors.toList());
            
        if (enabledFilters.isEmpty()) return false;
        
        this.lastMatchedFilter = null;
        
        if (matchAny) {
            // OR mode: any filter match is sufficient
            for (FilterEntry filter : enabledFilters) {
                if (checkTradeWithFilter(offers, filter)) {
                    this.lastMatchedFilter = filter;
                    return true;
                }
            }
            return false;
        } else {
            // AND mode: all filters must match
            for (FilterEntry filter : enabledFilters) {
                if (!checkTradeWithFilter(offers, filter)) {
                    return false; // One filter didn't match, fail immediately
                }
            }
            // All filters matched, set the first enabled filter as the match
            this.lastMatchedFilter = enabledFilters.get(0);
            return true;
        }
    }
    
    /**
     * Check trades against a single filter (adapted for 1.20.1)
     */
    private boolean checkTradeWithFilter(MerchantOffers offers, FilterEntry filter) {
        for (MerchantOffer offer : offers) {
            if (offer.isOutOfStock()) continue;
            
            ItemStack resultStack = offer.getResult();
            
            // Check price condition first
            ItemStack costA = offer.getCostA();
            ItemStack costB = offer.getCostB();
            
            boolean priceMatches = false;
            
            // Determine what payment item we're looking for (null = emeralds)
            if (filter.getPaymentItemId() == null) {
                // Default to emerald payments
                if (costA.is(Items.EMERALD) && costA.getCount() <= filter.getMaxPrice()) {
                    priceMatches = true;
                } else if (costB.is(Items.EMERALD) && costB.getCount() <= filter.getMaxPrice()) {
                    priceMatches = true;
                }
            } else {
                // Custom payment item specified
                ResourceLocation paymentItemId = filter.getPaymentItemId();
                net.minecraft.world.item.Item paymentItem = Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(Registries.ITEM)
                    .getOptional(paymentItemId)
                    .orElse(null);
                
                if (paymentItem != null) {
                    if (costA.is(paymentItem) && costA.getCount() <= filter.getMaxPrice()) {
                        priceMatches = true;
                    } else if (costB.is(paymentItem) && costB.getCount() <= filter.getMaxPrice()) {
                        priceMatches = true;
                    }
                }
            }
            
            if (!priceMatches) continue;
            
            // Check item id condition if specified
            if (filter.getItemId() != null) {
                ResourceLocation itemIdInStack = Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(Registries.ITEM).getKey(resultStack.getItem());
                
                if (!itemIdInStack.equals(filter.getItemId())) {
                    continue; // Item doesn't match
                }
                
                // Check count condition
                if (resultStack.getCount() < filter.getMinCount()) {
                    continue;
                }
            }
            
            // Check enchantment condition if specified (adapted for 1.20.1)
            if (filter.getEnchantmentId() != null) {
                Enchantment targetEnchant = Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getOptional(filter.getEnchantmentId())
                    .orElse(null);
                
                if (targetEnchant == null) continue; // Invalid enchantment ID
                
                boolean foundEnchantment = false;
                
                // Check enchantments using EnchantmentHelper (1.20.1 compatible)
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(resultStack);
                if (!enchantments.isEmpty()) {
                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        Enchantment ench = entry.getKey();
                        int level = entry.getValue();
                        
                        if (ench.equals(targetEnchant) && level >= filter.getEnchantmentLevel()) {
                            foundEnchantment = true;
                            break;
                        }
                    }
                }
                
                if (!foundEnchantment) {
                    continue; // Enchantment condition not met
                }
            }
            
            // If we get here, all conditions are met
            return true;
        }
        
        return false;
    }
    
    /**
     * Load filters from configuration file
     */
    public void loadFiltersFromConfig() {
        FilterConfig.Config config = FilterConfig.loadFilters();
        this.filterEntries = FilterConfig.dataToFilters(config.filters);
        this.matchAny = config.matchAny;
    }
    
    /**
     * Save filters to configuration file
     */
    public void saveFiltersToConfig() {
        FilterConfig.saveFilters(this.filterEntries, this.matchAny);
    }
}