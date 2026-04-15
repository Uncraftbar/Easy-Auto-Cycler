package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.filter.FilterEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
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

    private static final int PADDING = 10;
    private static final int INPUT_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int INPUT_WIDTH = 220;

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

        int left = (this.width - INPUT_WIDTH) / 2;
        int yPos = PADDING * 3;

        enchantmentIdInput = new SuggestingEditBox(
                this.font, left, yPos, INPUT_WIDTH, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.enchantment_id"),
                enchantmentSuggestions);
        enchantmentIdInput.setMaxLength(256);
        if (filter.getEnchantmentId() != null) {
            enchantmentIdInput.setValue(filter.getEnchantmentId().toString());
        }
        this.addRenderableWidget(enchantmentIdInput);

        yPos += INPUT_HEIGHT + PADDING;
        enchantmentLevelInput = new EditBox(this.font, left, yPos, INPUT_WIDTH, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.enchantment_level"));
        enchantmentLevelInput.setValue(String.valueOf(filter.getEnchantmentLevel()));
        this.addRenderableWidget(enchantmentLevelInput);

        yPos += INPUT_HEIGHT + PADDING;
        itemIdInput = new SuggestingEditBox(
                this.font, left, yPos, INPUT_WIDTH, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.item_id"),
                itemSuggestions);
        itemIdInput.setMaxLength(256);
        if (filter.getItemId() != null) {
            itemIdInput.setValue(filter.getItemId().toString());
        }
        this.addRenderableWidget(itemIdInput);

        yPos += INPUT_HEIGHT + PADDING;
        minCountInput = new EditBox(this.font, left, yPos, INPUT_WIDTH, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.min_count"));
        minCountInput.setValue(String.valueOf(filter.getMinCount()));
        this.addRenderableWidget(minCountInput);

        yPos += INPUT_HEIGHT + PADDING;
        paymentItemInput = new SuggestingEditBox(
                this.font, left, yPos, INPUT_WIDTH, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.payment_item"),
                itemSuggestions);
        paymentItemInput.setMaxLength(256);
        if (filter.getPaymentItemId() != null) {
            paymentItemInput.setValue(filter.getPaymentItemId().toString());
        }
        this.addRenderableWidget(paymentItemInput);

        yPos += INPUT_HEIGHT + PADDING;
        maxPriceInput = new EditBox(this.font, left, yPos, INPUT_WIDTH, INPUT_HEIGHT,
                Component.translatable("gui.easyautocycler.filter.max_price"));
        maxPriceInput.setValue(String.valueOf(filter.getMaxPrice()));
        this.addRenderableWidget(maxPriceInput);

        int bottomY = this.height - PADDING - BUTTON_HEIGHT;
        int buttonWidth = 100;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.easyautocycler.filter.save"),
                        button -> saveFilter())
                .pos(left, bottomY).size(buttonWidth, BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.easyautocycler.filter.cancel"),
                        button -> onClose())
                .pos(left + INPUT_WIDTH - buttonWidth, bottomY).size(buttonWidth, BUTTON_HEIGHT).build());
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

        int titleX = this.width / 2 - this.font.width(this.title) / 2;
        graphics.text(this.font, this.title, titleX, PADDING, -1, true);

        int left = (this.width - INPUT_WIDTH) / 2;
        int offset = PADDING * 2;

        String[] labels = {
                "gui.easyautocycler.filter.enchantment_id",
                "gui.easyautocycler.filter.enchantment_level",
                "gui.easyautocycler.filter.item_id",
                "gui.easyautocycler.filter.min_count",
                "gui.easyautocycler.filter.payment_item",
                "gui.easyautocycler.filter.max_price"
        };
        for (String key : labels) {
            graphics.text(this.font, Component.translatable(key), left, offset, -1, true);
            offset += INPUT_HEIGHT + PADDING;
        }

        int statusY = this.height - PADDING * 2 - BUTTON_HEIGHT - 15;
        if (!statusText.getString().isEmpty()) {
            int statusX = this.width / 2 - this.font.width(statusText) / 2;
            graphics.text(this.font, statusText, statusX, statusY, hasError ? 0xFFFF5555 : 0xFF55FF55, true);
        } else {
            Component helpText = Component.translatable("gui.easyautocycler.filter.help")
                    .withStyle(ChatFormatting.GRAY);
            int helpX = this.width / 2 - this.font.width(helpText) / 2;
            graphics.text(this.font, helpText, helpX, statusY, 0xFFAAAAAA, true);
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
