package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.AutomationManager;
import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen that displays and manages multiple trade filters
 */
public class FilterListScreen extends Screen {

    @Nullable
    private final Screen previousScreen;
    private ScrollableContainer filtersContainer;
    private List<FilterEntry> filters = new ArrayList<>();
    private CycleButton<Boolean> matchModeCycleButton;
    private boolean matchAny = true; // true = OR logic, false = AND logic
    
    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FILTER_ITEM_HEIGHT = 25;
    
    public FilterListScreen(@Nullable Screen previousScreen) {
        super(Component.translatable("gui.easyautocycler.filters.title"));
        this.previousScreen = previousScreen;
        // Create deep copies of filters to avoid modifying originals when toggling enabled state
        for (FilterEntry original : AutomationManager.INSTANCE.getFilterEntries()) {
            this.filters.add(new FilterEntry(original));
        }
        this.matchAny = AutomationManager.INSTANCE.isMatchAny();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int buttonWidth = 110; 
        int screenMiddle = width / 2;
        int containerWidth = width - (PADDING * 4);
        int containerHeight = height - (PADDING * 3) - (BUTTON_HEIGHT * 3);
        
        // Add filter button at the top
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.easyautocycler.filters.add"),
                button -> openFilterEditor(null))
                .pos(screenMiddle - buttonWidth - 5, PADDING)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());
                
        // Match mode cycle button
        this.matchModeCycleButton = CycleButton.<Boolean>builder(value -> 
                Component.translatable(value ? 
                    "gui.easyautocycler.filters.match_any" : 
                    "gui.easyautocycler.filters.match_all"))
                .withValues(true, false)
                .withInitialValue(matchAny)
                .displayOnlyValue()
                .create(screenMiddle + 5, PADDING, buttonWidth, BUTTON_HEIGHT,
                        Component.empty(),
                        (cycleButton, newValue) -> {
                            matchAny = newValue;
                        });
        
        this.addRenderableWidget(this.matchModeCycleButton);
        
        // Scrollable container for filters
        filtersContainer = new ScrollableContainer(
            PADDING * 2, 
            PADDING * 2 + BUTTON_HEIGHT, 
            containerWidth, 
            containerHeight);
        this.addRenderableWidget(filtersContainer);
        
        // Bottom buttons
        int bottomY = height - PADDING - BUTTON_HEIGHT;
        
        // Save button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.easyautocycler.filters.save"),
                button -> saveFilters())
                .pos(screenMiddle - buttonWidth - 5, bottomY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());
        
        // Back button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.easyautocycler.filters.back"),
                button -> onClose())
                .pos(screenMiddle + 5, bottomY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());
                
        // Refresh the filter list
        refreshFiltersList();
    }
    
    /**
     * Save all filters to the AutomationManager
     */
    private void saveFilters() {
        AutomationManager.INSTANCE.setFilterEntries(filters);
        AutomationManager.INSTANCE.setMatchAny(matchModeCycleButton.getValue());
        onClose();
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
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render title
        guiGraphics.drawCenteredString(
            this.font, 
            this.title, 
            this.width / 2, 
            PADDING / 2, 
            0xFFFFFF);
        
        // If no filters, display a message
        if (filters.isEmpty()) {
            Component noFiltersMsg = Component.translatable("gui.easyautocycler.filters.no_filters")
                .withStyle(ChatFormatting.GRAY);
            
            guiGraphics.drawCenteredString(
                this.font,
                noFiltersMsg,
                this.width / 2,
                this.height / 2 - 10,
                0xAAAAAA);
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return filtersContainer.mouseScrolled(mouseX, mouseY, 0, scrollDelta) || super.mouseScrolled(mouseX, mouseY, scrollDelta);
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
    public void onClose() {
        Minecraft.getInstance().setScreen(previousScreen);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
