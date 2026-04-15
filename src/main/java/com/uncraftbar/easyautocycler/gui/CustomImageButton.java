package com.uncraftbar.easyautocycler.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class CustomImageButton extends AbstractButton {

    private final ResourceLocation textureNormal;
    private final ResourceLocation textureHover;
    private final OnPress onPress;
    private final Component tooltip;

    public CustomImageButton(int x, int y, int width, int height,
                             ResourceLocation textureNormal, ResourceLocation textureHover,
                             Component tooltip, OnPress onPress) {
        super(x, y, width, height, Component.empty());
        this.textureNormal = textureNormal;
        this.textureHover = (textureHover != null) ? textureHover : textureNormal;
        this.tooltip = tooltip;
        this.onPress = onPress;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ResourceLocation texture = this.isHovered() && this.active ? this.textureHover : this.textureNormal;

        float colorMult = this.active ? 1.0F : 0.5F;
        int argb = ((int) (this.alpha * 255) << 24)
                | ((int) (colorMult * 255) << 16)
                | ((int) (colorMult * 255) << 8)
                | (int) (colorMult * 255);

        guiGraphics.blit(RenderType::guiTextured, texture,
                this.getX(), this.getY(),
                0.0F, 0.0F,
                this.width, this.height,
                this.width, this.height,
                argb);

        if (this.isHovered() && this.active) {
            guiGraphics.renderTooltip(mc.font, this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        if (this.active) {
            this.onPress.onPress(this);
        }
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager handler) {
        if (this.active) {
            handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @FunctionalInterface
    public interface OnPress {
        void onPress(CustomImageButton button);
    }
}
