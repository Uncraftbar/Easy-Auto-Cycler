package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.AutomationManager;
import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Dashboard for managing trade filters. */
public class ConfigScreen extends Screen {
    private static final int HEADER_HEIGHT = 38;
    private static final int TOOLBAR_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 52;
    private static final int FILTER_ROW_HEIGHT = 38;

    @Nullable private final Screen previousScreen;
    private final List<FilterEntry> filters = new ArrayList<>();
    private final List<FilterEntry> originalFilters = new ArrayList<>();
    private final boolean originalMatchAny;
    private boolean matchAny;
    private boolean saved;

    private GuiLayout.Bounds panel;
    private ScrollableContainer filtersContainer;
    private CycleButton<Boolean> matchModeButton;

    public ConfigScreen(@Nullable Screen previousScreen, Component title) {
        super(title);
        this.previousScreen = previousScreen;
        AutomationManager.INSTANCE.getFilterEntries().forEach(filter -> filters.add(new FilterEntry(filter)));
        filters.forEach(filter -> originalFilters.add(new FilterEntry(filter)));
        this.originalMatchAny = AutomationManager.INSTANCE.isMatchAny();
        this.matchAny = originalMatchAny;
    }

    @Override
    protected void init() {
        super.init();
        panel = GuiLayout.centeredPanel(width, height, 380, height - GuiTheme.OUTER_MARGIN * 2);
        int left = panel.innerLeft();
        int contentWidth = panel.innerWidth();
        int toolbarY = panel.y() + HEADER_HEIGHT;

        int addWidth = Math.min(116, (contentWidth - GuiTheme.GAP) / 2);
        addRenderableWidget(Button.builder(
                Component.translatable("gui.easyautocycler.filters.add_compact"), button -> openFilterEditor(null))
            .pos(left, toolbarY + 4).size(addWidth, GuiTheme.CONTROL_HEIGHT).build());

        matchModeButton = CycleButton.<Boolean>builder(value -> Component.translatable(value
                ? "gui.easyautocycler.filters.match_any_compact"
                : "gui.easyautocycler.filters.match_all_compact"))
            .withValues(true, false)
            .withInitialValue(matchAny)
            .displayOnlyValue()
            .create(left + addWidth + GuiTheme.GAP, toolbarY + 4,
                contentWidth - addWidth - GuiTheme.GAP, GuiTheme.CONTROL_HEIGHT, Component.empty(),
                (button, value) -> matchAny = value);
        addRenderableWidget(matchModeButton);

        int listY = toolbarY + TOOLBAR_HEIGHT + 3;
        int footerTop = panel.bottom() - FOOTER_HEIGHT;
        filtersContainer = new ScrollableContainer(left, listY, contentWidth, Math.max(48, footerTop - listY - 5));
        addRenderableWidget(filtersContainer);

        int footerY = footerTop + 5;
        int third = (contentWidth - GuiTheme.GAP * 2) / 3;
        addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.save"), this::onSave)
            .pos(left, footerY).size(third, GuiTheme.CONTROL_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.clear_all"), this::onClear)
            .pos(left + third + GuiTheme.GAP, footerY).size(third, GuiTheme.CONTROL_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
            .pos(left + (third + GuiTheme.GAP) * 2, footerY).size(contentWidth - (third + GuiTheme.GAP) * 2,
                GuiTheme.CONTROL_HEIGHT).build());

        refreshFiltersList();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.flush();

        super.render(graphics, mouseX, mouseY, partialTick);

        // Text belongs to the foreground stratum, just like vanilla screen titles.
        graphics.drawString(font, title, panel.innerLeft(), panel.y() + 9, GuiTheme.TEXT, false);
        Component summary = Component.translatable("gui.easyautocycler.config.summary", filters.size());
        graphics.drawString(font, summary, panel.innerLeft(), panel.y() + 22, GuiTheme.MUTED, false);

        if (filters.isEmpty()) {
            int centerX = filtersContainer.getX() + filtersContainer.getWidth() / 2;
            int centerY = filtersContainer.getY() + filtersContainer.getHeight() / 2;
            graphics.drawCenteredString(font, Component.translatable("gui.easyautocycler.filters.empty_title"),
                centerX, centerY - 10, GuiTheme.TEXT);
            graphics.drawCenteredString(font, Component.translatable("gui.easyautocycler.filters.empty_hint"),
                centerX, centerY + 5, GuiTheme.MUTED);
        }

        graphics.flush();
    }

    private void onSave(Button ignored) {
        AutomationManager.INSTANCE.setMatchAny(matchAny);
        AutomationManager.INSTANCE.setFilterEntries(copyFilters(filters));
        saved = true;
        sendMessageToPlayer(Component.translatable("gui.easyautocycler.config.saved").withStyle(ChatFormatting.GREEN));
        onClose();
    }

    private void onClear(Button ignored) {
        filters.clear();
        matchAny = true;
        if (matchModeButton != null) matchModeButton.setValue(true);
        refreshFiltersList();
    }

    private void openFilterEditor(@Nullable FilterEntry filterToEdit) {
        if (filterToEdit == null) {
            FilterEntry newFilter = new FilterEntry();
            Minecraft.getInstance().setScreen(new FilterEditorScreen(this, newFilter, ignored -> {
                filters.add(newFilter);
                refreshFiltersList();
            }));
            return;
        }

        int index = filters.indexOf(filterToEdit);
        if (index >= 0) {
            FilterEntry copy = new FilterEntry(filterToEdit);
            Minecraft.getInstance().setScreen(new FilterEditorScreen(this, copy, ignored -> {
                filters.set(index, copy);
                refreshFiltersList();
            }));
        }
    }

    private void refreshFiltersList() {
        if (filtersContainer == null) return;
        filtersContainer.clearWidgets();
        int usableWidth = filtersContainer.getWidth() - 10;

        for (int index = 0; index < filters.size(); index++) {
            FilterEntry filter = filters.get(index);
            int x = filtersContainer.getX() + 5;
            int y = filtersContainer.getY() + 5 + index * FILTER_ROW_HEIGHT;

            CycleButton<Boolean> toggle = CycleButton.<Boolean>builder(value -> Component.literal(value ? "ON" : "OFF")
                    .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                .withValues(true, false).withInitialValue(filter.isEnabled()).displayOnlyValue()
                .create(x + 5, y + 7, 38, 20, Component.translatable("gui.easyautocycler.filters.enabled"),
                    (button, value) -> filter.setEnabled(value));

            int deleteWidth = 24;
            int editX = x + 48;
            Button edit = Button.builder(filter.getDisplayName(), button -> openFilterEditor(filter))
                .pos(editX, y + 3).size(usableWidth - 48 - deleteWidth - 8, 26).build();
            Button delete = Button.builder(Component.literal("×").withStyle(ChatFormatting.RED), button -> {
                filters.remove(filter);
                refreshFiltersList();
            }).pos(x + usableWidth - deleteWidth, y + 6).size(deleteWidth, 20).build();

            filtersContainer.addWidget(toggle);
            filtersContainer.addWidget(edit);
            filtersContainer.addWidget(delete);
        }
    }

    private void sendMessageToPlayer(Component message) {
        if (minecraft != null && minecraft.player != null) minecraft.player.sendSystemMessage(message);
    }

    private static List<FilterEntry> copyFilters(List<FilterEntry> source) {
        return source.stream().map(FilterEntry::new).toList();
    }

    @Override
    public void onClose() {
        if (!saved) {
            AutomationManager.INSTANCE.setMatchAny(originalMatchAny);
            AutomationManager.INSTANCE.setFilterEntries(copyFilters(originalFilters));
        }
        if (minecraft != null) minecraft.setScreen(previousScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
