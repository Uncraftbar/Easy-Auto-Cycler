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
            component.append(Component.literal(enchantmentId.getPath()).withStyle(ChatFormatting.AQUA));
            component.append(Component.literal("  Lv. " + enchantmentLevel).withStyle(ChatFormatting.GRAY));

            if (itemId != null) {
                component.append(Component.literal("  •  ").withStyle(ChatFormatting.DARK_GRAY));
                component.append(Component.literal(itemId.getPath()).withStyle(ChatFormatting.GOLD));
            }
        } else if (itemId != null) {
            component.append(Component.literal(itemId.getPath()).withStyle(ChatFormatting.GOLD));
        } else {
            component.append(Component.literal("Empty filter").withStyle(ChatFormatting.RED));
        }

        if (itemId != null && minCount > 1) {
            component.append(Component.literal("  ×" + minCount).withStyle(ChatFormatting.GRAY));
        }
        component.append(Component.literal("  •  ≤" + maxPrice + " ").withStyle(ChatFormatting.DARK_GRAY));
        component.append(Component.literal(paymentItemId == null ? "emeralds" : paymentItemId.getPath())
            .withStyle(ChatFormatting.GREEN));

        return component;
    }
}
