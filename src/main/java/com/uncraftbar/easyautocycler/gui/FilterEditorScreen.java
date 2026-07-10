package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/** Compact two-column editor for one trade filter. */
public class FilterEditorScreen extends Screen {
    private static final int HEADER_HEIGHT = 40;
    private static final int FIELD_HEIGHT = 20;
    private static final int FIELD_STEP = 39;
    private static final int FOOTER_HEIGHT = 36;

    @Nullable private final Screen previousScreen;
    private final FilterEntry filter;
    private final Consumer<Integer> onSave;

    private GuiLayout.Bounds panel;
    private SuggestingEditBox enchantmentIdInput;
    private EditBox enchantmentLevelInput;
    private SuggestingEditBox itemIdInput;
    private EditBox minCountInput;
    private SuggestingEditBox paymentItemInput;
    private EditBox maxPriceInput;
    private Component statusText = Component.empty();
    private String initialEnchantmentId;
    private String initialEnchantmentLevel;
    private String initialItemId;
    private String initialMinCount;
    private String initialPaymentItem;
    private String initialMaxPrice;

    private List<String> enchantmentSuggestions = List.of();
    private List<String> itemSuggestions = List.of();

    public FilterEditorScreen(@Nullable Screen previousScreen, FilterEntry filter, Consumer<Integer> onSave) {
        super(Component.translatable("gui.easyautocycler.filter.title"));
        this.previousScreen = previousScreen;
        this.filter = filter;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        super.init();
        initSuggestions();
        panel = GuiLayout.centeredPanel(width, height, 390, 220);

        int left = panel.innerLeft();
        int gap = 12;
        int columnWidth = (panel.innerWidth() - gap) / 2;
        int right = left + columnWidth + gap;
        int fieldStep = panel.height() < 210 ? 34 : FIELD_STEP;
        int firstY = panel.y() + HEADER_HEIGHT + (panel.height() < 210 ? 8 : 13);

        enchantmentIdInput = suggesting(left, firstY, columnWidth,
            Component.translatable("gui.easyautocycler.filter.enchantment_id"), enchantmentSuggestions,
            idString(filter.getEnchantmentId()));
        enchantmentLevelInput = edit(right, firstY, columnWidth,
            Component.translatable("gui.easyautocycler.filter.enchantment_level"), String.valueOf(filter.getEnchantmentLevel()));
        itemIdInput = suggesting(left, firstY + fieldStep, columnWidth,
            Component.translatable("gui.easyautocycler.filter.item_id"), itemSuggestions, idString(filter.getItemId()));

        minCountInput = edit(right, firstY + fieldStep, columnWidth,
            Component.translatable("gui.easyautocycler.filter.min_count"), String.valueOf(filter.getMinCount()));
        paymentItemInput = suggesting(left, firstY + fieldStep * 2, columnWidth,
            Component.translatable("gui.easyautocycler.filter.payment_item_short"), itemSuggestions,
            idString(filter.getPaymentItemId()));
        maxPriceInput = edit(right, firstY + fieldStep * 2, columnWidth,
            Component.translatable("gui.easyautocycler.filter.max_price"), String.valueOf(filter.getMaxPrice()));
        paymentItemInput.setTooltip(Tooltip.create(
            Component.translatable("gui.easyautocycler.filter.payment_item.tooltip")));

        initialEnchantmentId = enchantmentIdInput.getValue();
        initialEnchantmentLevel = enchantmentLevelInput.getValue();
        initialItemId = itemIdInput.getValue();
        initialMinCount = minCountInput.getValue();
        initialPaymentItem = paymentItemInput.getValue();
        initialMaxPrice = maxPriceInput.getValue();

        int footerY = panel.bottom() - FOOTER_HEIGHT + 8;
        int buttonWidth = (panel.innerWidth() - gap) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.filter.save"), button -> saveFilter())
            .pos(left, footerY).size(buttonWidth, GuiTheme.CONTROL_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.filter.cancel"), button -> onClose())
            .pos(left + buttonWidth + gap, footerY).size(buttonWidth, GuiTheme.CONTROL_HEIGHT).build());
    }

    private SuggestingEditBox suggesting(int x, int y, int width, Component label, List<String> suggestions,
                                         String value) {
        SuggestingEditBox input = new SuggestingEditBox(font, x, y, width, FIELD_HEIGHT, label, suggestions, ignored -> {});
        input.setMaxLength(256);
        input.setValue(value);
        addRenderableWidget(input);
        return input;
    }

    private EditBox edit(int x, int y, int width, Component label, String value) {
        EditBox input = new EditBox(font, x, y, width, FIELD_HEIGHT, label);
        input.setValue(value);
        addRenderableWidget(input);
        return input;
    }

    private void initSuggestions() {
        if (minecraft == null || minecraft.level == null) return;
        try {
            enchantmentSuggestions = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .keySet().stream().map(ResourceLocation::toString).sorted().toList();
            itemSuggestions = minecraft.level.registryAccess().registryOrThrow(Registries.ITEM)
                .keySet().stream().map(ResourceLocation::toString).sorted().toList();
        } catch (RuntimeException ignored) {
            enchantmentSuggestions = List.of();
            itemSuggestions = List.of();
        }
    }

    private void saveFilter() {
        if (minecraft == null || minecraft.level == null) return;
        statusText = Component.empty();

        ResourceLocation enchantmentId = parseEnchantment(enchantmentIdInput.getValue());
        if (!statusText.getString().isEmpty()) return;
        ResourceLocation itemId = parseItem(itemIdInput.getValue(), "gui.easyautocycler.filter.error.invalid_item_id");
        if (!statusText.getString().isEmpty()) return;
        ResourceLocation paymentId = parseItem(paymentItemInput.getValue(), "gui.easyautocycler.filter.error.invalid_payment_item");
        if (!statusText.getString().isEmpty()) return;

        Integer level = parseNumber(enchantmentLevelInput.getValue(), 1, Integer.MAX_VALUE, 1,
            "gui.easyautocycler.filter.error.invalid_level");
        if (level == null) return;
        Integer count = parseNumber(minCountInput.getValue(), 1, Integer.MAX_VALUE, 1,
            "gui.easyautocycler.filter.error.invalid_count");
        if (count == null) return;
        Integer price = parseNumber(maxPriceInput.getValue(), 1, 64, 64,
            "gui.easyautocycler.filter.error.invalid_price");
        if (price == null) return;

        filter.setEnchantmentId(enchantmentId);
        filter.setEnchantmentLevel(level);
        filter.setItemId(itemId);
        filter.setMinCount(count);
        filter.setPaymentItemId(paymentId);
        filter.setMaxPrice(price);

        if (!filter.isValid()) {
            setError(Component.translatable("gui.easyautocycler.filter.error.no_criteria"));
            return;
        }

        onSave.accept(0);
        onClose();
    }

    @Nullable
    private ResourceLocation parseEnchantment(String raw) {
        String value = raw.trim();
        if (value.isEmpty()) return null;
        try {
            ResourceLocation id = ResourceLocation.parse(value);
            Enchantment entry = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getOptional(id).orElse(null);
            if (entry != null) return id;
        } catch (RuntimeException ignored) {}
        setError(Component.translatable("gui.easyautocycler.filter.error.invalid_enchantment_id", value));
        return null;
    }

    @Nullable
    private ResourceLocation parseItem(String raw, String errorKey) {
        String value = raw.trim();
        if (value.isEmpty()) return null;
        try {
            ResourceLocation id = ResourceLocation.parse(value);
            Item entry = minecraft.level.registryAccess().registryOrThrow(Registries.ITEM).getOptional(id).orElse(null);
            if (entry != null) return id;
        } catch (RuntimeException ignored) {}
        setError(Component.translatable(errorKey, value));
        return null;
    }

    @Nullable
    private Integer parseNumber(String raw, int minimum, int maximum, int fallback, String errorKey) {
        String value = raw.trim();
        if (value.isEmpty()) return fallback;
        try {
            int number = Integer.parseInt(value);
            if (number >= minimum && number <= maximum) return number;
        } catch (NumberFormatException ignored) {}
        setError(Component.translatable(errorKey, value));
        return null;
    }

    private void setError(Component message) {
        statusText = message.copy().withStyle(ChatFormatting.RED);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.flush();

        super.render(graphics, mouseX, mouseY, partialTick);

        boolean dirty = hasUnsavedChanges();
        Component renderedTitle = dirty
            ? title.copy().append(Component.literal(" *").withStyle(ChatFormatting.GOLD))
            : title;
        graphics.drawString(font, renderedTitle, panel.innerLeft(), panel.y() + 9, GuiTheme.TEXT, false);
        Component subtitle = !statusText.getString().isEmpty()
            ? statusText
            : dirty ? Component.translatable("gui.easyautocycler.config.unsaved")
            : Component.translatable("gui.easyautocycler.filter.subtitle");
        graphics.drawString(font, subtitle, panel.innerLeft(), panel.y() + 22,
            !statusText.getString().isEmpty() ? GuiTheme.DANGER : dirty ? GuiTheme.WARNING : GuiTheme.MUTED, false);

        drawLabel(graphics, enchantmentIdInput, "gui.easyautocycler.filter.enchantment_id_short");
        drawLabel(graphics, enchantmentLevelInput, "gui.easyautocycler.filter.enchantment_level");
        drawLabel(graphics, itemIdInput, "gui.easyautocycler.filter.item_id_short");
        drawLabel(graphics, minCountInput, "gui.easyautocycler.filter.min_count");
        drawLabel(graphics, paymentItemInput, "gui.easyautocycler.filter.payment_item_short");
        drawLabel(graphics, maxPriceInput, "gui.easyautocycler.filter.max_price");

        enchantmentIdInput.renderSuggestionList(graphics, mouseX, mouseY);
        itemIdInput.renderSuggestionList(graphics, mouseX, mouseY);
        paymentItemInput.renderSuggestionList(graphics, mouseX, mouseY);

        graphics.flush();
    }

    private void drawLabel(GuiGraphics graphics, EditBox input, String key) {
        graphics.drawString(font, Component.translatable(key), input.getX(), input.getY() - 11, GuiTheme.MUTED, false);
    }

    private static String idString(@Nullable ResourceLocation id) {
        return id == null ? "" : id.toString();
    }

    private boolean hasUnsavedChanges() {
        return initialEnchantmentId != null && (
            !initialEnchantmentId.equals(enchantmentIdInput.getValue())
                || !initialEnchantmentLevel.equals(enchantmentLevelInput.getValue())
                || !initialItemId.equals(itemIdInput.getValue())
                || !initialMinCount.equals(minCountInput.getValue())
                || !initialPaymentItem.equals(paymentItemInput.getValue())
                || !initialMaxPrice.equals(maxPriceInput.getValue()));
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
