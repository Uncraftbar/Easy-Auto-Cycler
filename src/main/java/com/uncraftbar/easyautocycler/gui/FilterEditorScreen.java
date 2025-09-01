package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Screen for editing an individual filter entry
 */
public class FilterEditorScreen extends Screen {

    @Nullable
    private final Screen previousScreen;
    private final FilterEntry filter;
    private final Consumer<Integer> onSave;
    
    // UI Components
    private SuggestingEditBox enchantmentIdInput;
    private SuggestingEditBox itemIdInput;
    private EditBox enchantmentLevelInput;
    private EditBox minCountInput;
    private SuggestingEditBox paymentItemInput;
    private EditBox maxPriceInput;
    private Component statusText = Component.empty();
    private boolean hasError = false;
    
    // Suggestions for autocomplete
    private List<String> enchantmentSuggestions = List.of();
    private List<String> itemSuggestions = List.of();
      private static final int PADDING = 10;
    private static final int INPUT_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int INPUT_WIDTH = 220;
    
    public FilterEditorScreen(@Nullable Screen previousScreen, FilterEntry filter, Consumer<Integer> onSave) {
        super(Component.translatable("gui.easyautocycler.filter.title"));
        this.previousScreen = previousScreen;
        this.filter = filter;
        this.onSave = onSave;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Initialize suggestions for autocomplete
        initSuggestions();
        
        int left = (this.width - INPUT_WIDTH) / 2;
        int yPos = PADDING * 3;        // Enchantment ID input
        enchantmentIdInput = new SuggestingEditBox(
            this.font, 
            left, 
            yPos, 
            INPUT_WIDTH, 
            INPUT_HEIGHT, 
            Component.translatable("gui.easyautocycler.filter.enchantment_id"),
            enchantmentSuggestions,
            (text) -> {});
        
        enchantmentIdInput.setMaxLength(256);
        if (filter.getEnchantmentId() != null) {
            enchantmentIdInput.setValue(filter.getEnchantmentId().toString());
        }
        this.addRenderableWidget(enchantmentIdInput);
        
        // Enchantment Level input
        yPos += INPUT_HEIGHT + PADDING;
        
        enchantmentLevelInput = new EditBox(
            this.font, 
            left, 
            yPos, 
            INPUT_WIDTH, 
            INPUT_HEIGHT, 
            Component.translatable("gui.easyautocycler.filter.enchantment_level"));
        
        enchantmentLevelInput.setValue(String.valueOf(filter.getEnchantmentLevel()));
        this.addRenderableWidget(enchantmentLevelInput);
        
        // Item ID input
        yPos += INPUT_HEIGHT + PADDING;
        
        itemIdInput = new SuggestingEditBox(
            this.font, 
            left, 
            yPos, 
            INPUT_WIDTH, 
            INPUT_HEIGHT, 
            Component.translatable("gui.easyautocycler.filter.item_id"),
            itemSuggestions,
            (text) -> {});
        
        itemIdInput.setMaxLength(256);
        if (filter.getItemId() != null) {
            itemIdInput.setValue(filter.getItemId().toString());
        }
        this.addRenderableWidget(itemIdInput);
        
        // Min Count input
        yPos += INPUT_HEIGHT + PADDING;
        
        minCountInput = new EditBox(
            this.font, 
            left, 
            yPos, 
            INPUT_WIDTH, 
            INPUT_HEIGHT, 
            Component.translatable("gui.easyautocycler.filter.min_count"));
        
        minCountInput.setValue(String.valueOf(filter.getMinCount()));
        this.addRenderableWidget(minCountInput);
        
        // Payment Item input
        yPos += INPUT_HEIGHT + PADDING;
        
        paymentItemInput = new SuggestingEditBox(
            this.font, 
            left, 
            yPos, 
            INPUT_WIDTH, 
            INPUT_HEIGHT, 
            Component.translatable("gui.easyautocycler.filter.payment_item"),
            itemSuggestions,
            (text) -> {});
        
        paymentItemInput.setMaxLength(256);
        if (filter.getPaymentItemId() != null) {
            paymentItemInput.setValue(filter.getPaymentItemId().toString());
        }
        this.addRenderableWidget(paymentItemInput);
        
        // Max Price input
        yPos += INPUT_HEIGHT + PADDING;
        
        maxPriceInput = new EditBox(
            this.font, 
            left, 
            yPos, 
            INPUT_WIDTH, 
            INPUT_HEIGHT, 
            Component.translatable("gui.easyautocycler.filter.max_price"));
        
        maxPriceInput.setValue(String.valueOf(filter.getMaxPrice()));
        this.addRenderableWidget(maxPriceInput);
        
        // Bottom buttons
        int bottomY = this.height - PADDING - BUTTON_HEIGHT;
        int buttonWidth = 100;
        
        // Save button
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.easyautocycler.filter.save"),
            button -> saveFilter())
            .pos(left, bottomY)
            .size(buttonWidth, BUTTON_HEIGHT)
            .build());
        
        // Cancel button
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.easyautocycler.filter.cancel"),
            button -> onClose())
            .pos(left + INPUT_WIDTH - buttonWidth, bottomY)
            .size(buttonWidth, BUTTON_HEIGHT)
            .build());
    }
    
    /**
     * Initialize suggestion lists for enchantment and item IDs
     */
    private void initSuggestions() {
        try {
            // Get enchantment suggestions
            enchantmentSuggestions = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .keySet().stream()
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.toList());
            
            // Get item suggestions
            itemSuggestions = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(Registries.ITEM)
                .keySet().stream()
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.toList());
        } catch (Exception e) {
            // Handle potential exceptions when accessing registries
            enchantmentSuggestions = List.of();
            itemSuggestions = List.of();
        }
    }
    
    /**
     * Save the filter if it's valid
     */
    private void saveFilter() {
        // Validate and update enchantment ID
        String enchantmentIdStr = enchantmentIdInput.getValue().trim();
        if (!enchantmentIdStr.isEmpty()) {
            try {                ResourceLocation enchantmentId = ResourceLocation.parse(enchantmentIdStr);
                Enchantment enchantment = Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getOptional(enchantmentId)
                    .orElse(null);
                
                if (enchantment == null) {
                    setError(Component.translatable("gui.easyautocycler.filter.error.invalid_enchantment_id", enchantmentIdStr));
                    return;
                }
                
                filter.setEnchantmentId(enchantmentId);
            } catch (Exception e) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_enchantment_id", enchantmentIdStr));
                return;
            }
        } else {
            filter.setEnchantmentId(null);
        }
        
        // Validate and update enchantment level
        String levelStr = enchantmentLevelInput.getValue().trim();
        try {
            int level = Integer.parseInt(levelStr);
            if (level < 1) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_level", levelStr));
                return;
            }
            filter.setEnchantmentLevel(level);
        } catch (NumberFormatException e) {
            if (!levelStr.isEmpty()) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_level", levelStr));
                return;
            }
            // Default to 1 if empty
            filter.setEnchantmentLevel(1);
        }
        
        // Validate and update item ID
        String itemIdStr = itemIdInput.getValue().trim();
        if (!itemIdStr.isEmpty()) {
            try {                ResourceLocation itemId = ResourceLocation.parse(itemIdStr);
                Item item = Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(Registries.ITEM)
                    .getOptional(itemId)
                    .orElse(null);
                
                if (item == null) {
                    setError(Component.translatable("gui.easyautocycler.filter.error.invalid_item_id", itemIdStr));
                    return;
                }
                
                filter.setItemId(itemId);
            } catch (Exception e) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_item_id", itemIdStr));
                return;
            }
        } else {
            filter.setItemId(null);
        }
        
        // Validate and update minimum count
        String minCountStr = minCountInput.getValue().trim();
        try {
            int minCount = Integer.parseInt(minCountStr);
            if (minCount < 1) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_count", minCountStr));
                return;
            }
            filter.setMinCount(minCount);
        } catch (NumberFormatException e) {
            if (!minCountStr.isEmpty()) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_count", minCountStr));
                return;
            }
            // Default to 1 if empty
            filter.setMinCount(1);
        }
        
        // Validate and update payment item
        String paymentItemStr = paymentItemInput.getValue().trim();
        if (!paymentItemStr.isEmpty()) {
            try {
                ResourceLocation paymentItemId = ResourceLocation.parse(paymentItemStr);
                Item paymentItem = Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(Registries.ITEM)
                    .getOptional(paymentItemId)
                    .orElse(null);
                
                if (paymentItem == null) {
                    setError(Component.translatable("gui.easyautocycler.filter.error.invalid_payment_item", paymentItemStr));
                    return;
                }
                
                filter.setPaymentItemId(paymentItemId);
            } catch (Exception e) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_payment_item", paymentItemStr));
                return;
            }
        } else {
            filter.setPaymentItemId(null); // null = emeralds (default)
        }
        
        // Validate and update maximum price
        String maxPriceStr = maxPriceInput.getValue().trim();
        try {
            int maxPrice = Integer.parseInt(maxPriceStr);
            if (maxPrice < 1 || maxPrice > 64) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_price", maxPriceStr));
                return;
            }
            filter.setMaxPrice(maxPrice);
        } catch (NumberFormatException e) {
            if (!maxPriceStr.isEmpty()) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_price", maxPriceStr));
                return;
            }
            // Default to 64 if empty
            filter.setMaxPrice(64);
        }
        
        // Check if the filter has at least one criterion set
        if (!filter.isValid()) {
            setError(Component.translatable("gui.easyautocycler.filter.error.no_criteria"));
            return;
        }
        
        // Save was successful
        onSave.accept(0); // The index parameter isn't needed since we're passing the filter directly
        onClose();
    }
    
    /**
     * Set an error message
     */
    private void setError(Component message) {
        statusText = message.copy().withStyle(ChatFormatting.RED);
        hasError = true;
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Draw title
        guiGraphics.drawCenteredString(
            this.font, 
            this.title, 
            this.width / 2, 
            PADDING, 
            0xFFFFFF);
        
        // Draw field labels above each input
        int left = (this.width - INPUT_WIDTH) / 2;
        int offset = 0;
        
        // Enchantment ID label
        offset += PADDING * 2;
        guiGraphics.drawString(
            this.font,
            Component.translatable("gui.easyautocycler.filter.enchantment_id"),
            left,
            offset,
            0xFFFFFF);
            
        // Enchantment Level label
        offset += INPUT_HEIGHT + PADDING;
        guiGraphics.drawString(
            this.font,
            Component.translatable("gui.easyautocycler.filter.enchantment_level"),
            left,
            offset,
            0xFFFFFF);
            
        // Item ID label
        offset += INPUT_HEIGHT + PADDING;
        guiGraphics.drawString(
            this.font,
            Component.translatable("gui.easyautocycler.filter.item_id"),
            left,
            offset,
            0xFFFFFF);
            
        // Min Count label
        offset += INPUT_HEIGHT + PADDING;
        guiGraphics.drawString(
            this.font,
            Component.translatable("gui.easyautocycler.filter.min_count"),
            left,
            offset,
            0xFFFFFF);
            
        // Payment Item label
        offset += INPUT_HEIGHT + PADDING;
        guiGraphics.drawString(
            this.font,
            Component.translatable("gui.easyautocycler.filter.payment_item"),
            left,
            offset,
            0xFFFFFF);
            
        // Max Price label
        offset += INPUT_HEIGHT + PADDING;
        guiGraphics.drawString(
            this.font,
            Component.translatable("gui.easyautocycler.filter.max_price"),
            left,
            offset,
            0xFFFFFF);
        
        // Draw status/error message
        if (!statusText.getString().isEmpty()) {
            int statusY = this.height - PADDING * 2 - BUTTON_HEIGHT - 15;
            guiGraphics.drawCenteredString(
                this.font,
                statusText,
                this.width / 2,
                statusY,
                hasError ? 0xFF5555 : 0x55FF55);
        }
        
        // Draw help text
        if (!hasError) {
            int helpY = this.height - PADDING * 2 - BUTTON_HEIGHT - 15;
            Component helpText = Component.translatable("gui.easyautocycler.filter.help")
                .withStyle(ChatFormatting.GRAY);
            guiGraphics.drawCenteredString(
                this.font,
                helpText,
                this.width / 2,
                helpY,
                0xAAAAAA);
        }
    }
    
    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(previousScreen);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
