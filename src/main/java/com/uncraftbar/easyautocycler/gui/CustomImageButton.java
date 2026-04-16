package com.uncraftbar.easyautocycler.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

public class CustomImageButton extends AbstractButton {

    private final Identifier textureNormal;
    private final Identifier textureHover;
    private final OnPress onPress;
    private final Component tooltip;

    public CustomImageButton(int x, int y, int width, int height,
                             Identifier textureNormal, Identifier textureHover,
                             Component tooltip, OnPress onPress) {
        super(x, y, width, height, Component.empty());
        this.textureNormal = textureNormal;
        this.textureHover = (textureHover != null) ? textureHover : textureNormal;
        this.tooltip = tooltip;
        this.onPress = onPress;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Identifier texture = this.isHovered() && this.active ? this.textureHover : this.textureNormal;

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                this.getX(), this.getY(),
                0.0F, 0.0F,
                this.width, this.height,
                this.width, this.height);

        if (this.isHovered() && this.active) {
            graphics.setTooltipForNextFrame(this.tooltip, mouseX, mouseY);
        }
    }

    @Override
    public void onPress(InputWithModifiers input) {
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
