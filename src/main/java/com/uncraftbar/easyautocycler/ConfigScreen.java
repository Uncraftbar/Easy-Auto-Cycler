package com.uncraftbar.easyautocycler.gui;

import com.uncraftbar.easyautocycler.AutomationManager;
import com.uncraftbar.easyautocycler.EasyAutoCyclerMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import javax.annotation.Nullable; // Import Nullable

public class ConfigScreen extends Screen {

    // Field to store the screen we came from
    @Nullable // Mark as nullable in case something goes wrong
    private final Screen previousScreen;

    private EditBox enchantmentIdInput;
    private EditBox levelInput;
    private EditBox priceInput;

    // --- Updated Constructor ---
    public ConfigScreen(@Nullable Screen previousScreen, Component title) {
        super(title);
        this.previousScreen = previousScreen; // Store the previous screen
    }

    @Override
    protected void init() {
        // Keep super.init() call
        super.init();
        if (this.minecraft == null) { this.onClose(); return; } // Simplified null check

        // --- Widget layout (keep as before, adjust positions if needed) ---
        int screenWidth = this.width;
        int screenHeight = this.height;
        int inputWidth = 150;
        int inputHeight = 20;
        int verticalSpacing = 24;

        ResourceLocation currentId = AutomationManager.INSTANCE.getTargetEnchantmentId();
        String currentIdString = (currentId != null) ? currentId.toString() : "";
        int currentLevel = AutomationManager.INSTANCE.getTargetLevel();
        int currentPrice = AutomationManager.INSTANCE.getMaxEmeraldCost();

        // Enchantment ID Input
        this.enchantmentIdInput = new EditBox(this.font, (screenWidth / 2) - (inputWidth / 2), screenHeight / 2 - verticalSpacing, inputWidth, inputHeight, Component.translatable("gui.easyautocycler.config.enchantment_id"));
        this.enchantmentIdInput.setValue(currentIdString);
        this.addRenderableWidget(this.enchantmentIdInput);

        // Level Input
        this.levelInput = new EditBox( this.font, (screenWidth / 2) - (inputWidth / 2), screenHeight / 2, inputWidth / 2 - 2, inputHeight, Component.translatable("gui.easyautocycler.config.level"));
        this.levelInput.setValue(String.valueOf(currentLevel));
        this.levelInput.setFilter(s -> s.matches("[0-9]*"));
        this.addRenderableWidget(this.levelInput);

        // Price Input
        this.priceInput = new EditBox( this.font, (screenWidth / 2) + 2, screenHeight / 2, inputWidth / 2 - 2, inputHeight, Component.translatable("gui.easyautocycler.config.price"));
        this.priceInput.setValue(String.valueOf(currentPrice));
        this.priceInput.setFilter(s -> s.matches("[0-9]*"));
        this.addRenderableWidget(this.priceInput);

        // Save Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.save"), this::onSave)
                .pos((screenWidth / 2) - inputWidth / 2, screenHeight / 2 + verticalSpacing + 5)
                .size(inputWidth / 2 - 2, 20)
                .build());

        // Clear Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.easyautocycler.config.clear"), this::onClear)
                .pos((screenWidth / 2) + 2, screenHeight / 2 + verticalSpacing + 5)
                .size(inputWidth / 2 - 2, 20)
                .build());

        // Done Button (now calls our modified onClose)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .pos((screenWidth / 2) - 100 / 2, screenHeight / 2 + verticalSpacing * 2 + 10)
                .size(100, 20)
                .build());
    }

    // --- onSave method (logic remains the same) ---
    private void onSave(Button button) {
        String idString = this.enchantmentIdInput.getValue();
        String levelString = this.levelInput.getValue();
        String priceString = this.priceInput.getValue();
        ResourceLocation enchantmentId = ResourceLocation.tryParse(idString);
        int level = 1; int price = 64;

        if (enchantmentId == null) { sendMessage(Component.translatable("gui.easyautocycler.config.error.invalid_id", idString).withStyle(net.minecraft.ChatFormatting.RED)); return; }
        try { level = Integer.parseInt(levelString); if (level <= 0) throw new NumberFormatException(); } catch (NumberFormatException e) { sendMessage(Component.translatable("gui.easyautocycler.config.error.invalid_level", levelString).withStyle(net.minecraft.ChatFormatting.RED)); return; }
        try { price = Integer.parseInt(priceString); if (price <= 0 || price > 64) throw new NumberFormatException(); } catch (NumberFormatException e) { sendMessage(Component.translatable("gui.easyautocycler.config.error.invalid_price", priceString).withStyle(net.minecraft.ChatFormatting.RED)); return; }

        try {
            Registry<Enchantment> enchantmentRegistry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Enchantment enchantment = enchantmentRegistry.getOptional(enchantmentId).orElseThrow(() -> new IllegalArgumentException("Unknown enchantment ID"));
            if (level > enchantment.getMaxLevel()) { sendMessage(Component.translatable("gui.easyautocycler.config.error.level_too_high", level, enchantment.getMaxLevel()).withStyle(net.minecraft.ChatFormatting.RED)); return; }

            AutomationManager.INSTANCE.configureTarget(enchantment, enchantmentId, level, price);
            sendMessage(Component.translatable("gui.easyautocycler.config.success.saved", idString, level, price).withStyle(net.minecraft.ChatFormatting.GREEN));
            this.onClose(); // Close after successful save

        } catch (Exception e) {
            EasyAutoCyclerMod.LOGGER.error("Failed to validate or save config from GUI", e);
            sendMessage(Component.translatable("gui.easyautocycler.config.error.validation_failed", e.getMessage()).withStyle(net.minecraft.ChatFormatting.RED));
        }
    }

    // --- onClear method (logic remains the same) ---
    private void onClear(Button button) {
        AutomationManager.INSTANCE.clearTarget();
        this.enchantmentIdInput.setValue("");
        this.levelInput.setValue("1");
        this.priceInput.setValue("64");
        sendMessage(Component.translatable("gui.easyautocycler.config.success.cleared").withStyle(net.minecraft.ChatFormatting.YELLOW));
    }

    // --- Render method (logic remains the same, add labels etc) ---
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // Renders widgets
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Draw Labels
        int labelX = (this.width / 2) - (150 / 2);
        int labelYOffset = -10;
        guiGraphics.drawString(this.font, Component.translatable("gui.easyautocycler.config.enchantment_id"), labelX, this.enchantmentIdInput.getY() + labelYOffset, 0xA0A0A0);
        guiGraphics.drawString(this.font, Component.translatable("gui.easyautocycler.config.level"), labelX, this.levelInput.getY() + labelYOffset, 0xA0A0A0);
        guiGraphics.drawString(this.font, Component.translatable("gui.easyautocycler.config.price"), this.priceInput.getX(), this.priceInput.getY() + labelYOffset, 0xA0A0A0);
    }

    // --- sendMessage helper (logic remains the same) ---
    private void sendMessage(Component message) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(message);
        }
    }

    // --- Updated onClose ---
    @Override
    public void onClose() {
        // Set the screen back to the previous one (MerchantScreen) instead of null
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen); // Go back!
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}