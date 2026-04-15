package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.AutomationManager;
import com.uncraftbar.easyautocycler.EasyAutoCyclerMod;
import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigScreen extends Screen {

    @Nullable
    private final Screen previousScreen;

    private CycleButton<Integer> delayCycleButton;

    private List<String> enchantmentSuggestions = List.of();
    private List<String> itemSuggestions = List.of();

    private List<FilterEntry> filters = new ArrayList<>();
    private final List<FilterEntry> originalFilters = new ArrayList<>();
    private boolean originalMatchAny;
    private int originalDelay;
    private CycleButton<Boolean> matchModeCycleButton;
    private boolean matchAny = true;

    private static final int PADDING = 6;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FILTER_ROW_HEIGHT = 22;

    public ConfigScreen(@Nullable Screen previousScreen, Component title) {
        super(title);
        this.previousScreen = previousScreen;

        this.filters.addAll(AutomationManager.INSTANCE.getFilterEntries());
        this.originalFilters.clear();
        this.originalFilters.addAll(this.filters.stream().map(FilterEntry::new).collect(Collectors.toList()));
        this.originalMatchAny = AutomationManager.INSTANCE.isMatchAny();
        this.originalDelay = AutomationManager.INSTANCE.getClickDelay();
        this.matchAny = AutomationManager.INSTANCE.isMatchAny();
    }

    List<String> enchantmentSuggestions() {
        return enchantmentSuggestions;
    }

    List<String> itemSuggestions() {
        return itemSuggestions;
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) {
            this.onClose();
            return;
        }

        try {
            this.enchantmentSuggestions = this.minecraft.level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .listElementIds()
                    .map(k -> k.identifier().toString())
                    .sorted()
                    .collect(Collectors.toList());

            this.itemSuggestions = BuiltInRegistries.ITEM.keySet().stream()
                    .map(Identifier::toString)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            EasyAutoCyclerMod.LOGGER.error("Failed to load registry for suggestions", e);
            this.enchantmentSuggestions = List.of();
            this.itemSuggestions = List.of();
        }

        int contentWidth = 300;
        int guiLeft = (this.width - contentWidth) / 2;
        int currentY = PADDING * 4 + 10;

        int currentDelay = AutomationManager.INSTANCE.getClickDelay();

        int topButtonWidth = 120;
        int buttonSpacing = 10;
        int totalButtonsWidth = (topButtonWidth * 2) + buttonSpacing;
        int buttonsStartX = guiLeft + (contentWidth - totalButtonsWidth) / 2;

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.easyautocycler.filters.add"),
                button -> openFilterEditor(null))
                .pos(buttonsStartX, currentY)
                .size(topButtonWidth, BUTTON_HEIGHT)
                .build());

        this.matchModeCycleButton = CycleButton.<Boolean>builder(value ->
                        Component.translatable(value
                                ? "gui.easyautocycler.filters.match_any"
                                : "gui.easyautocycler.filters.match_all"), matchAny)
                .withValues(true, false)
                .displayOnlyValue()
                .create(buttonsStartX + topButtonWidth + buttonSpacing, currentY, topButtonWidth, BUTTON_HEIGHT,
                        Component.empty(),
                        (cycleButton, newValue) -> matchAny = newValue);
        this.addRenderableWidget(this.matchModeCycleButton);

        currentY += BUTTON_HEIGHT + PADDING + 5;

        int bottomMargin = PADDING + 5;
        int cancelButtonY = this.height - bottomMargin - BUTTON_HEIGHT;
        int saveButtonY = cancelButtonY - BUTTON_HEIGHT - PADDING;
        int delayY = saveButtonY - BUTTON_HEIGHT - PADDING;

        int filtersListStartY = currentY;
        int filtersListMaxHeight = delayY - currentY - PADDING;
        int maxFiltersShown = Math.max(1, filtersListMaxHeight / FILTER_ROW_HEIGHT);

        int filterRowWidth = contentWidth;
        int visibleFilters = Math.min(filters.size(), maxFiltersShown);
        for (int i = 0; i < visibleFilters; i++) {
            FilterEntry filter = filters.get(i);
            int rowY = filtersListStartY + (i * FILTER_ROW_HEIGHT);
            addFilterRowWidgets(filter, guiLeft, rowY, filterRowWidth);
        }

        this.delayCycleButton = CycleButton.<Integer>builder(value ->
                        Component.translatable("gui.easyautocycler.config.delay.value", value), currentDelay)
                .withValues(AutomationManager.MIN_CLICK_DELAY, 2, 3, 4, 5)
                .displayOnlyValue()
                .create(guiLeft, delayY, contentWidth, BUTTON_HEIGHT,
                        Component.translatable("gui.easyautocycler.config.delay"),
                        (cycleButton, newValue) -> AutomationManager.INSTANCE.configureSpeed(newValue));
        this.addRenderableWidget(this.delayCycleButton);

        int bottomButtonWidth = (contentWidth - PADDING) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.save"), this::onSave)
                .pos(guiLeft, saveButtonY).size(bottomButtonWidth, BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.clear"), this::onClear)
                .pos(guiLeft + bottomButtonWidth + PADDING, saveButtonY).size(bottomButtonWidth, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                .pos(guiLeft, cancelButtonY).size(contentWidth, BUTTON_HEIGHT).build());
    }

    private void addFilterRowWidgets(FilterEntry filter, int rowX, int rowY, int rowWidth) {
        CycleButton<Boolean> toggleButton = CycleButton.<Boolean>builder(value ->
                        Component.literal(value ? "✓" : "✗")
                                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED),
                        filter.isEnabled())
                .withValues(true, false)
                .displayOnlyValue()
                .create(rowX + 2, rowY, 20, 20,
                        Component.empty(),
                        (cycleButton, newValue) -> filter.setEnabled(newValue));
        this.addRenderableWidget(toggleButton);

        AbstractWidget filterButton = Button.builder(
                        filter.getDisplayName(),
                        button -> openFilterEditor(filter))
                .pos(rowX + 26, rowY)
                .size(rowWidth - 52, 20)
                .build();
        this.addRenderableWidget(filterButton);

        AbstractWidget deleteButton = Button.builder(
                        Component.literal("X").withStyle(ChatFormatting.RED),
                        button -> {
                            filters.remove(filter);
                            this.rebuildWidgets();
                        })
                .pos(rowX + rowWidth - 22, rowY)
                .size(20, 20)
                .build();
        this.addRenderableWidget(deleteButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int titleX = this.width / 2 - this.font.width(this.title) / 2;
        graphics.text(this.font, this.title, titleX, PADDING * 2, 0xFFFFFF, true);

        if (filters.isEmpty()) {
            Component noFiltersMsg = Component.translatable("gui.easyautocycler.filters.no_filters")
                    .withStyle(ChatFormatting.GRAY);
            int msgX = this.width / 2 - this.font.width(noFiltersMsg) / 2;
            int msgY = PADDING * 4 + 10 + BUTTON_HEIGHT + PADDING + 30;
            graphics.text(this.font, noFiltersMsg, msgX, msgY, 0xAAAAAA, true);
        }

        int hiddenCount = filters.size() - visibleFilterCount();
        if (hiddenCount > 0) {
            Component moreMsg = Component.translatable("gui.easyautocycler.filters.more", hiddenCount)
                    .withStyle(ChatFormatting.GRAY);
            int msgX = this.width / 2 - this.font.width(moreMsg) / 2;
            int msgY = this.delayCycleButton.getY() - 14;
            graphics.text(this.font, moreMsg, msgX, msgY, 0xAAAAAA, true);
        }
    }

    private int visibleFilterCount() {
        int filtersListMaxHeight = computeFiltersListMaxHeight();
        int maxFiltersShown = Math.max(1, filtersListMaxHeight / FILTER_ROW_HEIGHT);
        return Math.min(filters.size(), maxFiltersShown);
    }

    private int computeFiltersListMaxHeight() {
        int topSectionBottom = PADDING * 4 + 10 + BUTTON_HEIGHT + PADDING + 5;
        int bottomMargin = PADDING + 5;
        int cancelButtonY = this.height - bottomMargin - BUTTON_HEIGHT;
        int saveButtonY = cancelButtonY - BUTTON_HEIGHT - PADDING;
        int delayY = saveButtonY - BUTTON_HEIGHT - PADDING;
        return delayY - topSectionBottom - PADDING;
    }

    private void onSave(Button button) {
        AutomationManager.INSTANCE.setMatchAny(matchModeCycleButton.getValue());
        AutomationManager.INSTANCE.setFilterEntries(filters);

        this.sendMessageToPlayer(Component.literal("Configuration saved!").withStyle(ChatFormatting.GREEN));
        this.onClose();
    }

    private void onClear(Button button) {
        int defaultDelay = AutomationManager.DEFAULT_CLICK_DELAY;
        filters.clear();
        this.rebuildWidgets();

        if (this.matchModeCycleButton != null) this.matchModeCycleButton.setValue(true);
        if (this.delayCycleButton != null) this.delayCycleButton.setValue(defaultDelay);

        this.sendMessageToPlayer(Component.literal("Configuration cleared (unsaved).").withStyle(ChatFormatting.YELLOW));
    }

    private void sendMessageToPlayer(Component message) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(message);
        }
    }

    @Override
    public void onClose() {
        AutomationManager.INSTANCE.setMatchAny(this.originalMatchAny);
        AutomationManager.INSTANCE.setFilterEntries(this.originalFilters.stream().map(FilterEntry::new).collect(Collectors.toList()));
        AutomationManager.INSTANCE.configureSpeed(this.originalDelay);

        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void openFilterEditor(@Nullable FilterEntry filterToEdit) {
        if (filterToEdit == null) {
            FilterEntry newFilter = new FilterEntry();
            FilterEditorScreen editorScreen = new FilterEditorScreen(this, newFilter, enchantmentSuggestions, itemSuggestions, index -> {
                filters.add(newFilter);
                this.rebuildWidgets();
            });
            Minecraft.getInstance().setScreen(editorScreen);
        } else {
            int filterIndex = filters.indexOf(filterToEdit);
            if (filterIndex >= 0) {
                FilterEntry filterCopy = new FilterEntry(filterToEdit);
                FilterEditorScreen editorScreen = new FilterEditorScreen(this, filterCopy, enchantmentSuggestions, itemSuggestions, index -> {
                    filters.set(filterIndex, filterCopy);
                    this.rebuildWidgets();
                });
                Minecraft.getInstance().setScreen(editorScreen);
            }
        }
    }
}
