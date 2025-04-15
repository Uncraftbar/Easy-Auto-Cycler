package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.AutomationManager;
import com.uncraftbar.easyautocycler.EasyAutoCyclerMod;
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
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ConfigScreen extends Screen {

    @Nullable
    private final Screen previousScreen;

    private SuggestingEditBox enchantmentIdInput;
    private EditBox levelInput;
    private EditBox priceInput;
    private CycleButton<Integer> delayCycleButton;

    private List<String> enchantmentSuggestions = List.of();

    private static final int PADDING = 6;
    private static final int INPUT_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;

    private static final Component DELAY_TOOLTIP = Component.translatable("gui.easyautocycler.config.delay.tooltip",
                    AutomationManager.MIN_CLICK_DELAY, AutomationManager.MAX_CLICK_DELAY)
            .withStyle(ChatFormatting.GRAY);


    public ConfigScreen(@Nullable Screen previousScreen, Component title) {
        super(title);
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        super.init();
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) { this.onClose(); return; }

        try {
            Registry<Enchantment> enchantmentRegistry = this.minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            this.enchantmentSuggestions = enchantmentRegistry.keySet().stream().map(ResourceLocation::toString).sorted().collect(Collectors.toList());
        } catch (Exception e) { EasyAutoCyclerMod.LOGGER.error("Failed to load enchantment registry for suggestions", e); this.enchantmentSuggestions = List.of(); }

        int contentWidth = 220; int guiLeft = (this.width - contentWidth) / 2; int inputWidthFull = contentWidth; int inputWidthSmall = (contentWidth / 2) - (PADDING / 2);
        int currentY = this.height / 2 - 60; // Start Y

        ResourceLocation currentId = AutomationManager.INSTANCE.getTargetEnchantmentId(); String currentIdString = (currentId != null) ? currentId.toString() : ""; int currentLevel = AutomationManager.INSTANCE.getTargetLevel(); int currentPrice = AutomationManager.INSTANCE.getMaxEmeraldCost(); int currentDelay = AutomationManager.INSTANCE.getClickDelay();

        currentY += 10; // Label space
        this.enchantmentIdInput = new SuggestingEditBox( this.font, guiLeft, currentY, inputWidthFull, INPUT_HEIGHT, Component.translatable("gui.easyautocycler.config.enchantment_id"), this.enchantmentSuggestions, (t) -> {}); this.enchantmentIdInput.setMaxLength(128); this.enchantmentIdInput.setValue(currentIdString);  this.enchantmentIdInput.setResponder(this::onEnchantmentInputChanged); this.addRenderableWidget(this.enchantmentIdInput); currentY += INPUT_HEIGHT + PADDING + 10;
        this.levelInput = new EditBox( this.font, guiLeft, currentY, inputWidthSmall, INPUT_HEIGHT, Component.translatable("gui.easyautocycler.config.level")); this.levelInput.setValue(String.valueOf(currentLevel)); this.levelInput.setFilter(s -> s.matches("[0-9]*")); this.addRenderableWidget(this.levelInput);
        this.priceInput = new EditBox( this.font, guiLeft + inputWidthSmall + PADDING, currentY, inputWidthSmall, INPUT_HEIGHT, Component.translatable("gui.easyautocycler.config.price")); this.priceInput.setValue(String.valueOf(currentPrice)); this.priceInput.setFilter(s -> s.matches("[0-9]*")); this.addRenderableWidget(this.priceInput); currentY += INPUT_HEIGHT + PADDING + 10;

        this.delayCycleButton = CycleButton.<Integer>builder(value -> Component.translatable("gui.easyautocycler.config.delay.value", value)) // How value is displayed
                .withValues(AutomationManager.MIN_CLICK_DELAY, 2, 3, 4, 5)
                .withInitialValue(currentDelay)
                .displayOnlyValue()
                .create(guiLeft, currentY, inputWidthFull, BUTTON_HEIGHT,
                        Component.translatable("gui.easyautocycler.config.delay"),
                        (cycleButton, newValue) -> {
                            AutomationManager.INSTANCE.configureSpeed(newValue);
                        });

        this.addRenderableWidget(this.delayCycleButton);
        currentY += BUTTON_HEIGHT + PADDING + 10; // Move Y down

        int buttonY = currentY;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.save"), this::onSave).pos(guiLeft, buttonY).size(inputWidthSmall, BUTTON_HEIGHT).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.clear"), this::onClear).pos(guiLeft + inputWidthSmall + PADDING, buttonY).size(inputWidthSmall, BUTTON_HEIGHT).build()); currentY += BUTTON_HEIGHT + PADDING;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose()).pos(guiLeft, currentY).size(inputWidthFull, BUTTON_HEIGHT).build());
    }

    private void onEnchantmentInputChanged(String text) { if (this.enchantmentIdInput != null) { this.enchantmentIdInput.setSuggestion(calculateSuggestions(text)); } }
    private String calculateSuggestions(String currentText) { if (currentText.isEmpty() || this.enchantmentSuggestions.isEmpty()) { return null; } String lowerCaseText = currentText.toLowerCase(); for (String suggestion : this.enchantmentSuggestions) { if (suggestion.toLowerCase().startsWith(lowerCaseText)) { if (suggestion.length() > currentText.length()) { return suggestion.substring(currentText.length()); } else { return null; } } } return null; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // Renders widgets
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, PADDING * 2, 0xFFFFFF);

        int labelYOffset = -10;
        guiGraphics.drawString(this.font, Component.translatable("gui.easyautocycler.config.enchantment_id"), this.enchantmentIdInput.getX(), this.enchantmentIdInput.getY() + labelYOffset, 0xA0A0A0);
        guiGraphics.drawString(this.font, Component.translatable("gui.easyautocycler.config.level"), this.levelInput.getX(), this.levelInput.getY() + labelYOffset, 0xA0A0A0);
        guiGraphics.drawString(this.font, Component.translatable("gui.easyautocycler.config.price"), this.priceInput.getX(), this.priceInput.getY() + labelYOffset, 0xA0A0A0);
        guiGraphics.drawString(this.font, Component.translatable("gui.easyautocycler.config.delay"), this.delayCycleButton.getX(), this.delayCycleButton.getY() + labelYOffset, 0xA0A0A0);

        if (this.delayCycleButton != null && this.delayCycleButton.isHovered() && this.delayCycleButton.active) {
            guiGraphics.renderTooltip(this.font, DELAY_TOOLTIP, mouseX, mouseY);
        }

    }

    private void onSave(Button button) {
        if (this.enchantmentIdInput == null || this.levelInput == null || this.priceInput == null || this.minecraft == null || this.minecraft.level == null) { EasyAutoCyclerMod.LOGGER.error("Cannot save, GUI components not initialized correctly."); this.sendMessageToPlayer(Component.literal("Error saving configuration!").withStyle(ChatFormatting.RED)); return; }
        String idString = this.enchantmentIdInput.getValue().trim(); String levelString = this.levelInput.getValue().trim(); String priceString = this.priceInput.getValue().trim();
        ResourceLocation enchantmentId = ResourceLocation.tryParse(idString); int level = 1; int price = 64; boolean valid = true;
        if (enchantmentId == null || idString.isEmpty()) { this.sendMessageToPlayer(Component.translatable("gui.easyautocycler.config.error.invalid_id", idString).withStyle(ChatFormatting.RED)); valid = false; }
        try { level = Integer.parseInt(levelString); if (level <= 0) throw new NumberFormatException(); } catch (NumberFormatException e) { this.sendMessageToPlayer(Component.translatable("gui.easyautocycler.config.error.invalid_level", levelString).withStyle(ChatFormatting.RED)); valid = false; }
        try { price = Integer.parseInt(priceString); if (price <= 0 || price > 64) throw new NumberFormatException(); } catch (NumberFormatException e) { this.sendMessageToPlayer(Component.translatable("gui.easyautocycler.config.error.invalid_price", priceString).withStyle(ChatFormatting.RED)); valid = false; }

        if (!valid) return;
        try {
            Registry<Enchantment> enchantmentRegistry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Enchantment enchantment = enchantmentRegistry.getOptional(enchantmentId).orElseThrow(() -> new IllegalArgumentException("Unknown enchantment ID: " + enchantmentId));
            if (level > enchantment.getMaxLevel()) { this.sendMessageToPlayer(Component.translatable("gui.easyautocycler.config.error.level_too_high", level, enchantment.getMaxLevel()).withStyle(ChatFormatting.RED)); return; }

            AutomationManager.INSTANCE.configureTarget(enchantment, enchantmentId, level, price);

            EasyAutoCyclerMod.LOGGER.info("configureTarget called successfully from GUI (Speed set via CycleButton).");

            sendMessageToPlayer(Component.translatable("gui.easyautocycler.config.success.saved", idString, level, price).withStyle(ChatFormatting.GREEN)); // Adjusted key
            this.onClose();
        } catch (Exception e) {EasyAutoCyclerMod.LOGGER.error("Failed to validate or save config from GUI", e); String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(); if (e instanceof IllegalArgumentException && errorMsg.contains("Unknown enchantment ID")) { this.sendMessageToPlayer(Component.translatable("gui.easyautocycler.config.error.invalid_id", idString).withStyle(ChatFormatting.RED)); } else { this.sendMessageToPlayer(Component.translatable("gui.easyautocycler.config.error.validation_failed", errorMsg).withStyle(ChatFormatting.RED)); } }
    }

    private void onClear(Button button) {
        AutomationManager.INSTANCE.clearTarget();
        int defaultDelay = AutomationManager.DEFAULT_CLICK_DELAY;
        AutomationManager.INSTANCE.configureSpeed(defaultDelay);

        if(this.enchantmentIdInput != null) this.enchantmentIdInput.setValue("");
        if(this.levelInput != null) this.levelInput.setValue("1");
        if(this.priceInput != null) this.priceInput.setValue("64");
        if(this.delayCycleButton != null) this.delayCycleButton.setValue(defaultDelay);
    }

    private void sendMessageToPlayer(Component message) { if (this.minecraft != null && this.minecraft.player != null) { this.minecraft.player.sendSystemMessage(message); } }
    @Override public void onClose() { if (this.minecraft != null) { this.minecraft.setScreen(this.previousScreen); } }
    @Override public boolean isPauseScreen() { return false; }
}