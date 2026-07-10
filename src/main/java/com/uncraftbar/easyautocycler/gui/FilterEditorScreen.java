package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Screen for editing an individual filter entry
 */
public class FilterEditorScreen extends Screen {

    @Nullable
    private final Screen previousScreen;
    private final FilterEntry filter;
    private final Consumer<Integer> onSave;
    private final List<String> enchantmentSuggestions;
    private final List<String> itemSuggestions;

    private SuggestingEditBox enchantmentIdInput;
    private SuggestingEditBox itemIdInput;
    private EditBox enchantmentLevelInput;
    private EditBox minCountInput;
    private SuggestingEditBox paymentItemInput;
    private EditBox maxPriceInput;
    private Component statusText = Component.empty();
    private boolean hasError = false;
    private String initialEnchantmentId;
    private String initialEnchantmentLevel;
    private String initialItemId;
    private String initialMinCount;
    private String initialPaymentItem;
    private String initialMaxPrice;

    private static final int PADDING = 12;
    private static final int INPUT_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int EDITOR_WIDTH = 390;
    private static final int EDITOR_HEIGHT = 220;
    private static final int HEADER_HEIGHT = 40;
    private static final int FIELD_STEP = 39;

    public FilterEditorScreen(@Nullable Screen previousScreen, FilterEntry filter,
                              List<String> enchantmentSuggestions, List<String> itemSuggestions,
                              Consumer<Integer> onSave) {
        super(Component.translatable("gui.easyautocycler.filter.title"));
        this.previousScreen = previousScreen;
        this.filter = filter;
        this.onSave = onSave;
        this.enchantmentSuggestions = enchantmentSuggestions;
        this.itemSuggestions = itemSuggestions;
    }

    @Override
    protected void init() {
        super.init();

        int contentWidth = Math.min(EDITOR_WIDTH - PADDING * 2, this.width - PADDING * 2);
        int left = (this.width - contentWidth) / 2;
        int top = Math.max(0, (this.height - EDITOR_HEIGHT) / 2);
        int gap = 12;
        int columnWidth = (contentWidth - gap) / 2;
        int fieldStep = this.height < 210 ? 34 : FIELD_STEP;
        int firstY = top + HEADER_HEIGHT + (this.height < 210 ? 8 : 13);

        enchantmentIdInput = new SuggestingEditBox(
                this.font, left, firstY, columnWidth, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.enchantment_id"),
                enchantmentSuggestions);
        enchantmentIdInput.setMaxLength(256);
        if (filter.getEnchantmentId() != null) {
            enchantmentIdInput.setValue(filter.getEnchantmentId().toString());
        }
        this.addRenderableWidget(enchantmentIdInput);

        enchantmentLevelInput = new EditBox(this.font, left + columnWidth + gap, firstY, columnWidth, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.enchantment_level"));
        enchantmentLevelInput.setValue(String.valueOf(filter.getEnchantmentLevel()));
        this.addRenderableWidget(enchantmentLevelInput);

        itemIdInput = new SuggestingEditBox(
                this.font, left, firstY + fieldStep, columnWidth, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.item_id"),
                itemSuggestions);
        itemIdInput.setMaxLength(256);
        if (filter.getItemId() != null) {
            itemIdInput.setValue(filter.getItemId().toString());
        }
        this.addRenderableWidget(itemIdInput);

        minCountInput = new EditBox(this.font, left + columnWidth + gap, firstY + fieldStep, columnWidth, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.min_count"));
        minCountInput.setValue(String.valueOf(filter.getMinCount()));
        this.addRenderableWidget(minCountInput);

        paymentItemInput = new SuggestingEditBox(
                this.font, left, firstY + fieldStep * 2, columnWidth, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.payment_item"),
                itemSuggestions);
        paymentItemInput.setMaxLength(256);
        if (filter.getPaymentItemId() != null) {
            paymentItemInput.setValue(filter.getPaymentItemId().toString());
        }
        this.addRenderableWidget(paymentItemInput);

        maxPriceInput = new EditBox(this.font, left + columnWidth + gap, firstY + fieldStep * 2, columnWidth, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.max_price"));
        maxPriceInput.setValue(String.valueOf(filter.getMaxPrice()));
        this.addRenderableWidget(maxPriceInput);
        paymentItemInput.setTooltip(Tooltip.create(
                Component.translatable("gui.easyautocycler.filter.payment_item.tooltip")));

        initialEnchantmentId = enchantmentIdInput.getValue();
        initialEnchantmentLevel = enchantmentLevelInput.getValue();
        initialItemId = itemIdInput.getValue();
        initialMinCount = minCountInput.getValue();
        initialPaymentItem = paymentItemInput.getValue();
        initialMaxPrice = maxPriceInput.getValue();

        int bottomY = Math.min(this.height - BUTTON_HEIGHT - 8, top + EDITOR_HEIGHT - 28);
        int buttonWidth = (contentWidth - gap) / 2;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.easyautocycler.filter.save"),
                        button -> saveFilter())
                .pos(left, bottomY).size(buttonWidth, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.easyautocycler.filter.cancel"),
                        button -> onClose())
                .pos(left + buttonWidth + gap, bottomY).size(buttonWidth, BUTTON_HEIGHT).build());
    }

    private void saveFilter() {
        String enchantmentIdStr = enchantmentIdInput.getValue().trim();
        if (!enchantmentIdStr.isEmpty()) {
            try {
                Identifier enchantmentId = Identifier.parse(enchantmentIdStr);
                boolean exists = Minecraft.getInstance().level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .get(ResourceKey.create(Registries.ENCHANTMENT, enchantmentId))
                        .isPresent();

                if (!exists) {
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
            filter.setEnchantmentLevel(1);
        }

        String itemIdStr = itemIdInput.getValue().trim();
        if (!itemIdStr.isEmpty()) {
            try {
                Identifier itemId = Identifier.parse(itemIdStr);
                if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
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
            filter.setMinCount(1);
        }

        String paymentItemStr = paymentItemInput.getValue().trim();
        if (!paymentItemStr.isEmpty()) {
            try {
                Identifier paymentItemId = Identifier.parse(paymentItemStr);
                if (!BuiltInRegistries.ITEM.containsKey(paymentItemId)) {
                    setError(Component.translatable("gui.easyautocycler.filter.error.invalid_payment_item", paymentItemStr));
                    return;
                }
                filter.setPaymentItemId(paymentItemId);
            } catch (Exception e) {
                setError(Component.translatable("gui.easyautocycler.filter.error.invalid_payment_item", paymentItemStr));
                return;
            }
        } else {
            filter.setPaymentItemId(null);
        }

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
            filter.setMaxPrice(64);
        }

        if (!filter.isValid()) {
            setError(Component.translatable("gui.easyautocycler.filter.error.no_criteria"));
            return;
        }

        onSave.accept(0);
        onClose();
    }

    private void setError(Component message) {
        statusText = message.copy().withStyle(ChatFormatting.RED);
        hasError = true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        int contentWidth = Math.min(EDITOR_WIDTH - PADDING * 2, this.width - PADDING * 2);
        int left = (this.width - contentWidth) / 2;
        int top = Math.max(0, (this.height - EDITOR_HEIGHT) / 2);
        boolean dirty = hasUnsavedChanges();
        Component renderedTitle = dirty ? this.title.copy().append(Component.literal(" *").withStyle(ChatFormatting.GOLD)) : this.title;
        graphics.text(this.font, renderedTitle, left, top + 9, 0xFFF4F6F8, false);
        Component subtitle = !statusText.getString().isEmpty() ? statusText
                : dirty ? Component.translatable("gui.easyautocycler.config.unsaved")
                : Component.translatable("gui.easyautocycler.filter.subtitle");
        graphics.text(this.font, subtitle, left, top + 22,
                !statusText.getString().isEmpty() ? 0xFFD9534F : dirty ? 0xFFFFC857 : 0xFFAAB2BF, false);

        drawLabel(graphics, enchantmentIdInput, "gui.easyautocycler.filter.enchantment_id_short");
        drawLabel(graphics, enchantmentLevelInput, "gui.easyautocycler.filter.enchantment_level");
        drawLabel(graphics, itemIdInput, "gui.easyautocycler.filter.item_id_short");
        drawLabel(graphics, minCountInput, "gui.easyautocycler.filter.min_count");
        drawLabel(graphics, paymentItemInput, "gui.easyautocycler.filter.payment_item_short");
        drawLabel(graphics, maxPriceInput, "gui.easyautocycler.filter.max_price");

        enchantmentIdInput.extractSuggestionList(graphics, mouseX, mouseY);
        itemIdInput.extractSuggestionList(graphics, mouseX, mouseY);
        paymentItemInput.extractSuggestionList(graphics, mouseX, mouseY);
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

    private void drawLabel(GuiGraphicsExtractor graphics, EditBox input, String key) {
        graphics.text(this.font, Component.translatable(key), input.getX(), input.getY() - 11,
                0xFFAAB2BF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (enchantmentIdInput != null && enchantmentIdInput.clickSuggestion(event)
                || itemIdInput != null && itemIdInput.clickSuggestion(event)
                || paymentItemInput != null && paymentItemInput.clickSuggestion(event)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
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
