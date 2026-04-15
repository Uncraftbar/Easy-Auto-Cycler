package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.AutomationManager;
import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

/**
 * Screen that displays and manages multiple trade filters.
 */
public class FilterListScreen extends Screen {

    @Nullable
    private final Screen previousScreen;
    private List<FilterEntry> filters = new ArrayList<>();
    private CycleButton<Boolean> matchModeCycleButton;
    private boolean matchAny = true;
    private List<String> enchantmentSuggestions = List.of();
    private List<String> itemSuggestions = List.of();

    private static final int PADDING = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FILTER_ROW_HEIGHT = 22;

    public FilterListScreen(@Nullable Screen previousScreen) {
        super(Component.translatable("gui.easyautocycler.filters.title"));
        this.previousScreen = previousScreen;
        for (FilterEntry original : AutomationManager.INSTANCE.getFilterEntries()) {
            this.filters.add(new FilterEntry(original));
        }
        this.matchAny = AutomationManager.INSTANCE.isMatchAny();
    }

    @Override
    protected void init() {
        super.init();

        try {
            if (this.minecraft != null && this.minecraft.level != null) {
                this.enchantmentSuggestions = this.minecraft.level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .listElementIds()
                        .map(k -> k.identifier().toString())
                        .sorted()
                        .collect(Collectors.toList());
            }
            this.itemSuggestions = BuiltInRegistries.ITEM.keySet().stream()
                    .map(Identifier::toString)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        int buttonWidth = 110;
        int screenMiddle = width / 2;
        int contentWidth = width - (PADDING * 4);
        int contentLeft = PADDING * 2;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.easyautocycler.filters.add"),
                        button -> openFilterEditor(null))
                .pos(screenMiddle - buttonWidth - 5, PADDING)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());

        this.matchModeCycleButton = CycleButton.<Boolean>builder(value ->
                        Component.translatable(value
                                ? "gui.easyautocycler.filters.match_any"
                                : "gui.easyautocycler.filters.match_all"), matchAny)
                .withValues(true, false)
                .displayOnlyValue()
                .create(screenMiddle + 5, PADDING, buttonWidth, BUTTON_HEIGHT,
                        Component.empty(),
                        (cycleButton, newValue) -> matchAny = newValue);
        this.addRenderableWidget(this.matchModeCycleButton);

        int listStartY = PADDING * 2 + BUTTON_HEIGHT;
        int bottomY = height - PADDING - BUTTON_HEIGHT;
        int listMaxHeight = bottomY - listStartY - PADDING;
        int maxFiltersShown = Math.max(1, listMaxHeight / FILTER_ROW_HEIGHT);
        int visibleFilters = Math.min(filters.size(), maxFiltersShown);

        for (int i = 0; i < visibleFilters; i++) {
            FilterEntry filter = filters.get(i);
            int rowY = listStartY + (i * FILTER_ROW_HEIGHT);
            addFilterRowWidgets(filter, contentLeft, rowY, contentWidth);
        }

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.easyautocycler.filters.save"),
                        button -> saveFilters())
                .pos(screenMiddle - buttonWidth - 5, bottomY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.easyautocycler.filters.back"),
                        button -> onClose())
                .pos(screenMiddle + 5, bottomY)
                .size(buttonWidth, BUTTON_HEIGHT)
                .build());
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

        this.addRenderableWidget(Button.builder(
                        filter.getDisplayName(),
                        button -> openFilterEditor(filter))
                .pos(rowX + 26, rowY)
                .size(rowWidth - 52, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.literal("X").withStyle(ChatFormatting.RED),
                        button -> {
                            filters.remove(filter);
                            this.rebuildWidgets();
                        })
                .pos(rowX + rowWidth - 22, rowY)
                .size(20, 20)
                .build());
    }

    private void saveFilters() {
        AutomationManager.INSTANCE.setFilterEntries(filters);
        AutomationManager.INSTANCE.setMatchAny(matchModeCycleButton.getValue());
        onClose();
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

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        int titleX = this.width / 2 - this.font.width(this.title) / 2;
        graphics.text(this.font, this.title, titleX, PADDING / 2, 0xFFFFFF, true);

        if (filters.isEmpty()) {
            Component noFiltersMsg = Component.translatable("gui.easyautocycler.filters.no_filters")
                    .withStyle(ChatFormatting.GRAY);
            int msgX = this.width / 2 - this.font.width(noFiltersMsg) / 2;
            graphics.text(this.font, noFiltersMsg, msgX, this.height / 2 - 10, 0xAAAAAA, true);
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
