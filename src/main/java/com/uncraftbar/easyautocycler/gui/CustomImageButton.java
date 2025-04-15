package com.uncraftbar.easyautocycler.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
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

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        float colorMult = this.active ? 1.0F : 0.5F;
        RenderSystem.setShaderColor(colorMult, colorMult, colorMult, this.alpha);

        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.isHovered() && this.active) {
            guiGraphics.renderTooltip(mc.font, this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        if(this.active) {
            this.onPress.onPress(this);
        }
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager handler) {
        if(this.active) {
            handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @FunctionalInterface
    public interface OnPress { void onPress(CustomImageButton button); }
}