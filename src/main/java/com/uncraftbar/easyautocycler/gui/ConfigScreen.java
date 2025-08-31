package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.AutomationManager;
import com.uncraftbar.easyautocycler.EasyAutoCyclerMod;
import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.Item;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import javax.annotation.Nullable;

public class ConfigScreen extends Screen {

    @Nullable
    private final Screen previousScreen;

    private SuggestingEditBox enchantmentIdInput;
    private SuggestingEditBox itemIdInput;
    private EditBox levelInput;
    private EditBox itemCountInput;
    private EditBox priceInput;
    private CycleButton<Integer> delayCycleButton;
    private CycleButton<Integer> modeCycleButton;

    private List<String> enchantmentSuggestions = List.of();
    private List<String> itemSuggestions = List.of();
    private int currentMode = AutomationManager.MODE_ENCHANTMENT;
    
    // Filter system components
    private ScrollableContainer filtersContainer;
    private List<FilterEntry> filters = new ArrayList<>();
    private CycleButton<Boolean> matchModeCycleButton;
    private boolean matchAny = true;

    private static final int PADDING = 6;
    private static final int INPUT_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FILTER_ITEM_HEIGHT = 25;

    private static final Component DELAY_TOOLTIP = Component.translatable("gui.easyautocycler.config.delay.tooltip",
                    AutomationManager.MIN_CLICK_DELAY, AutomationManager.MAX_CLICK_DELAY)
            .withStyle(ChatFormatting.GRAY);


    public ConfigScreen(@Nullable Screen previousScreen, Component title) {
        super(title);
        this.previousScreen = previousScreen;
        
        // Load current filter data
        this.filters.addAll(AutomationManager.INSTANCE.getFilterEntries());
        this.matchAny = AutomationManager.INSTANCE.isMatchAny();
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) { this.onClose(); return; }

        // Load suggestions for enchantments and items
        try {
            Registry<Enchantment> enchantmentRegistry = this.minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            this.enchantmentSuggestions = enchantmentRegistry.keySet().stream().map(ResourceLocation::toString).sorted().collect(Collectors.toList());
            
            Registry<Item> itemRegistry = this.minecraft.level.registryAccess().registryOrThrow(Registries.ITEM);
            this.itemSuggestions = itemRegistry.keySet().stream().map(ResourceLocation::toString).sorted().collect(Collectors.toList());
        } catch (Exception e) { 
            EasyAutoCyclerMod.LOGGER.error("Failed to load registry for suggestions", e); 
            this.enchantmentSuggestions = List.of(); 
            this.itemSuggestions = List.of();
        }        int contentWidth = 300; // Wider to accommodate filters
        int guiLeft = (this.width - contentWidth) / 2; 
        int inputWidthFull = contentWidth; 
        int inputWidthSmall = (contentWidth / 2) - (PADDING / 2);
        
        // Start with proper spacing for title
        int currentY = PADDING * 4 + 10; // More space for title

        // Get current configuration  
        int currentDelay = AutomationManager.INSTANCE.getClickDelay();

        // Filter management section - center the two buttons properly
        int topButtonWidth = 120; // Slightly wider for better text fit
        int buttonSpacing = 10;
        int totalButtonsWidth = (topButtonWidth * 2) + buttonSpacing;
        int buttonsStartX = guiLeft + (contentWidth - totalButtonsWidth) / 2;
        
        // Add filter button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.easyautocycler.filters.add"),
                button -> openFilterEditor(null))
                .pos(buttonsStartX, currentY)
                .size(topButtonWidth, BUTTON_HEIGHT)
                .build());
                
        // Match mode cycle button
        this.matchModeCycleButton = CycleButton.<Boolean>builder(value -> 
                Component.translatable(value ? 
                    "gui.easyautocycler.filters.match_any" : 
                    "gui.easyautocycler.filters.match_all"))
                .withValues(true, false)
                .withInitialValue(matchAny)
                .displayOnlyValue()
                .create(buttonsStartX + topButtonWidth + buttonSpacing, currentY, topButtonWidth, BUTTON_HEIGHT,
                        Component.empty(),
                        (cycleButton, newValue) -> {
                            matchAny = newValue;
                        });
        
        this.addRenderableWidget(this.matchModeCycleButton);
        currentY += BUTTON_HEIGHT + PADDING + 5; // Add a bit more spacing

        // Calculate better spacing for bottom elements
        int bottomMargin = PADDING + 5; // Space from screen bottom
        int delayButtonY = this.height - bottomMargin - BUTTON_HEIGHT; // Done button position
        int saveButtonY = delayButtonY - BUTTON_HEIGHT - PADDING; // Save/Clear buttons
        int delayY = saveButtonY - BUTTON_HEIGHT - PADDING; // Delay button
        
        // Scrollable container for filters - calculate remaining space
        int containerHeight = delayY - currentY - PADDING;
        filtersContainer = new ScrollableContainer(
            guiLeft, 
            currentY, 
            inputWidthFull, 
            containerHeight);
        this.addRenderableWidget(filtersContainer);

        // Delay button (positioned above Save/Clear buttons)
        this.delayCycleButton = CycleButton.<Integer>builder(value -> 
            Component.translatable("gui.easyautocycler.config.delay.value", value))
                .withValues(AutomationManager.MIN_CLICK_DELAY, 2, 3, 4, 5)
                .withInitialValue(currentDelay)
                .displayOnlyValue()
                .create(guiLeft, delayY, inputWidthFull, BUTTON_HEIGHT,
                        Component.translatable("gui.easyautocycler.config.delay"),
                        (cycleButton, newValue) -> {
                            AutomationManager.INSTANCE.configureSpeed(newValue);
                        });

        this.addRenderableWidget(this.delayCycleButton);

        // Save and Clear buttons
        int bottomButtonWidth = (inputWidthFull - PADDING) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.save"), this::onSave)
            .pos(guiLeft, saveButtonY).size(bottomButtonWidth, BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.clear"), this::onClear)
            .pos(guiLeft + bottomButtonWidth + PADDING, saveButtonY).size(bottomButtonWidth, BUTTON_HEIGHT).build()); 
        
        // Cancel button (at bottom with proper margin)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
            .pos(guiLeft, delayButtonY).size(inputWidthFull, BUTTON_HEIGHT).build());
            
        // Populate the filter list
        refreshFiltersList();
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return filtersContainer.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return filtersContainer.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return filtersContainer.mouseDragged(mouseX, mouseY, button, dragX, dragY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return filtersContainer.mouseReleased(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // Renders widgets
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, PADDING * 2, 0xFFFFFF);

        int labelYOffset = -12; // Slightly more space above button
        // Only draw delay label if it won't overlap with filter container
        int delayLabelY = this.delayCycleButton.getY() + labelYOffset;
        int containerBottomY = filtersContainer.getY() + filtersContainer.getHeight();
        
        if (delayLabelY > containerBottomY + 5) { // 5px clearance
            guiGraphics.drawString(this.font, Component.translatable("gui.easyautocycler.config.delay"), 
                this.delayCycleButton.getX(), delayLabelY, 0xA0A0A0);
        }
        
        // If no filters, display a message
        if (filters.isEmpty()) {
            Component noFiltersMsg = Component.translatable("gui.easyautocycler.filters.no_filters")
                .withStyle(ChatFormatting.GRAY);
            
            int containerCenterX = filtersContainer.getX() + filtersContainer.getWidth() / 2;
            int containerCenterY = filtersContainer.getY() + filtersContainer.getHeight() / 2;
            
            guiGraphics.drawCenteredString(
                this.font,
                noFiltersMsg,
                containerCenterX,
                containerCenterY - 10,
                0xAAAAAA);
        }

        if (this.delayCycleButton != null && this.delayCycleButton.isHovered() && this.delayCycleButton.active) {
            guiGraphics.renderTooltip(this.font, DELAY_TOOLTIP, mouseX, mouseY);
        }
    }

    private void onSave(Button button) {
        if (this.minecraft == null || this.minecraft.level == null) { 
            EasyAutoCyclerMod.LOGGER.error("Cannot save, GUI components not initialized correctly."); 
            this.sendMessageToPlayer(Component.literal("Error saving configuration!").withStyle(ChatFormatting.RED)); 
            return; 
        }

        // Save all filters to the AutomationManager
        AutomationManager.INSTANCE.setFilterEntries(filters);
        AutomationManager.INSTANCE.setMatchAny(matchModeCycleButton.getValue());
        
        this.sendMessageToPlayer(Component.literal("Configuration saved!").withStyle(ChatFormatting.GREEN));
        this.onClose();
    }


    private void onClear(Button button) {
        AutomationManager.INSTANCE.clearTarget();
        int defaultDelay = AutomationManager.DEFAULT_CLICK_DELAY;
        AutomationManager.INSTANCE.configureSpeed(defaultDelay);

        // Clear all filters
        filters.clear();
        refreshFiltersList();
        
        // Reset delay button
        if(this.delayCycleButton != null) this.delayCycleButton.setValue(defaultDelay);
        
        this.sendMessageToPlayer(Component.literal("Configuration cleared!").withStyle(ChatFormatting.YELLOW));
    }

    private void sendMessageToPlayer(Component message) { 
        if (this.minecraft != null && this.minecraft.player != null) { 
            this.minecraft.player.sendSystemMessage(message); 
        } 
    }
    
    @Override public void onClose() { 
        if (this.minecraft != null) { 
            this.minecraft.setScreen(this.previousScreen); 
        } 
    }
    
    @Override public boolean isPauseScreen() { 
        return false; 
    }

    /**
     * Opens the filter editor screen for a new or existing filter
     */
    private void openFilterEditor(@Nullable FilterEntry filterToEdit) {
        if (filterToEdit == null) {
            // Create a new filter
            FilterEntry newFilter = new FilterEntry();
            FilterEditorScreen editorScreen = new FilterEditorScreen(this, newFilter, index -> {
                // Add the new filter when saved
                filters.add(newFilter);
                refreshFiltersList();
            });
            Minecraft.getInstance().setScreen(editorScreen);
        } else {
            // Edit existing filter
            int filterIndex = filters.indexOf(filterToEdit);
            if (filterIndex >= 0) {
                FilterEntry filterCopy = new FilterEntry(filterToEdit);
                FilterEditorScreen editorScreen = new FilterEditorScreen(this, filterCopy, index -> {
                    // Update the filter when saved
                    filters.set(filterIndex, filterCopy);
                    refreshFiltersList();
                });
                Minecraft.getInstance().setScreen(editorScreen);
            }
        }
    }
    
    /**
     * Refreshes the list of filters displayed in the scrollable container
     */
    private void refreshFiltersList() {
        filtersContainer.clearWidgets();
        
        if (filters.isEmpty()) {
            return;
        }
        
        int entryWidth = filtersContainer.getWidth() - 20; // Account for scrollbar
        int index = 0;
        
        for (FilterEntry filter : filters) {
            // Position relative to container's internal coordinate system
            int spacing = FILTER_ITEM_HEIGHT + 5; // 25 + 5 = 30
            int relativeY = 5 + (index * spacing);
            int absoluteX = filtersContainer.getX();
            int absoluteY = filtersContainer.getY() + relativeY;
            
            // Enable/disable toggle
            final FilterEntry finalFilter = filter;
            CycleButton<Boolean> toggleButton = CycleButton.<Boolean>builder(value ->
                    Component.literal(value ? "✓" : "✗")
                        .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED))
                .withValues(true, false)
                .withInitialValue(filter.isEnabled())
                .displayOnlyValue()
                .create(absoluteX + 5, absoluteY, 20, 20,
                        Component.empty(),
                        (cycleButton, newValue) -> finalFilter.setEnabled(newValue));
            
            // Filter display button
            Button filterButton = Button.builder(
                    filter.getDisplayName(),
                    button -> openFilterEditor(filter))
                    .pos(absoluteX + 30, absoluteY)
                    .size(entryWidth - 65, 20)
                    .build();
            
            // Delete button
            Button deleteButton = Button.builder(
                    Component.literal("🗑").withStyle(ChatFormatting.RED),
                    button -> {
                        filters.remove(filter);
                        refreshFiltersList();
                    })
                    .pos(absoluteX + entryWidth - 25, absoluteY)
                    .size(20, 20)
                    .build();
            
            filtersContainer.addWidget(toggleButton);
            filtersContainer.addWidget(filterButton);
            filtersContainer.addWidget(deleteButton);
            
            index++;
        }
    }
}