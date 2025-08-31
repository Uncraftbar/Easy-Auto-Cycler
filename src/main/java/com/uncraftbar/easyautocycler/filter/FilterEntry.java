package com.uncraftbar.easyautocycler.filter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a single filter entry in the automated trade cycling system.
 * Each entry contains the criteria for a trade that should be detected.
 */
public class FilterEntry {
    private boolean enabled = true;
    
    // Item-related criteria
    private ResourceLocation itemId;
    private int minCount = 1;
    
    // Enchantment-related criteria
    private ResourceLocation enchantmentId;
    private int enchantmentLevel = 1;
    
    // Payment criteria
    private ResourceLocation paymentItemId; // null = emeralds (default)
    private int maxPrice = 64;

    public FilterEntry() {
        // Default constructor
    }
    
    /**
     * Copy constructor to create a clone of an existing filter
     */
    public FilterEntry(FilterEntry other) {
        this.enabled = other.enabled;
        this.itemId = other.itemId;
        this.minCount = other.minCount;
        this.enchantmentId = other.enchantmentId;
        this.enchantmentLevel = other.enchantmentLevel;
        this.paymentItemId = other.paymentItemId;
        this.maxPrice = other.maxPrice;
    }

    // Getters and setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public void setItemId(ResourceLocation itemId) {
        this.itemId = itemId;
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    public ResourceLocation getEnchantmentId() {
        return enchantmentId;
    }

    public void setEnchantmentId(ResourceLocation enchantmentId) {
        this.enchantmentId = enchantmentId;
    }

    public int getEnchantmentLevel() {
        return enchantmentLevel;
    }

    public void setEnchantmentLevel(int enchantmentLevel) {
        this.enchantmentLevel = enchantmentLevel;
    }

    public ResourceLocation getPaymentItemId() {
        return paymentItemId;
    }

    public void setPaymentItemId(ResourceLocation paymentItemId) {
        this.paymentItemId = paymentItemId;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    /**
     * Checks if this filter is valid (has at least one criterion set)
     */
    public boolean isValid() {
        return itemId != null || enchantmentId != null;
    }
    
    /**
     * Creates a human-readable display name for this filter
     */
    public Component getDisplayName() {
        MutableComponent component = Component.empty();
        
        if (enchantmentId != null) {
            // Display the enchantment info
            String enchName = enchantmentId.toString();
            // Show just the path part for cleaner display
            String displayName = enchName.contains(":") ? enchName.substring(enchName.indexOf(":") + 1) : enchName;
            component.append(Component.literal(displayName).withStyle(ChatFormatting.AQUA));
            component.append(Component.literal(" (Lvl " + enchantmentLevel + ")").withStyle(ChatFormatting.GRAY));
            
            if (itemId != null) {
                // We have both enchantment and item
                component.append(Component.literal(" on ").withStyle(ChatFormatting.WHITE));
                String itemName = itemId.toString();
                String itemDisplayName = itemName.contains(":") ? itemName.substring(itemName.indexOf(":") + 1) : itemName;
                component.append(Component.literal(itemDisplayName).withStyle(ChatFormatting.GOLD));
                
                if (minCount > 1) {
                    component.append(Component.literal(" x" + minCount).withStyle(ChatFormatting.GRAY));
                }
            }
        } else if (itemId != null) {
            // Only item, no enchantment
            String itemName = itemId.toString();
            String itemDisplayName = itemName.contains(":") ? itemName.substring(itemName.indexOf(":") + 1) : itemName;
            component.append(Component.literal(itemDisplayName).withStyle(ChatFormatting.GOLD));
            
            if (minCount > 1) {
                component.append(Component.literal(" x" + minCount).withStyle(ChatFormatting.GRAY));
            }
        } else {
            // Neither enchantment nor item - show "Empty filter"
            component.append(Component.literal("Empty filter").withStyle(ChatFormatting.RED));
        }
        
        // Add price info
        component.append(Component.literal(" (≤" + maxPrice + "💠)").withStyle(ChatFormatting.GREEN));
        
        return component;
    }
}
