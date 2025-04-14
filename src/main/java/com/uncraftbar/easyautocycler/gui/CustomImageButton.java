package com.uncraftbar.easyautocycler.gui; // Your gui package

import com.mojang.blaze3d.systems.RenderSystem; // Needed for rendering setup
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer; // For setting shader
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

// Our custom button mimicking Easy Villagers' rendering approach
public class CustomImageButton extends AbstractButton {

    private final ResourceLocation textureNormal;
    private final ResourceLocation textureHover; // Texture to use when hovered
    private final OnPress onPress; // Action to perform on click
    private final Component tooltip; // Tooltip text

    public CustomImageButton(int x, int y, int width, int height,
                             ResourceLocation textureNormal, ResourceLocation textureHover,
                             Component tooltip, OnPress onPress) {
        // Call super with empty message as we draw the image, not text
        super(x, y, width, height, Component.empty());
        this.textureNormal = textureNormal;
        // Use hover texture if provided, otherwise default to normal
        this.textureHover = (textureHover != null) ? textureHover : textureNormal;
        this.tooltip = tooltip;
        this.onPress = onPress;
    }

    /**
     * Called by the screen's render loop to draw the button.
     */
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Choose texture based on hover state
        ResourceLocation texture = this.isHoveredOrFocused() ? this.textureHover : this.textureNormal;

        // Set up render system (similar to Easy Villagers button)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture); // Bind the chosen texture
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha); // Use button alpha
        RenderSystem.enableBlend(); // Enable transparency
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // Draw the texture using blit
        // Draw the whole texture (width x height) from texture coords (0,0)
        // Texture dimensions are assumed to match button dimensions here
        guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        // Render tooltip if hovered (after drawing button)
        if (this.isHovered) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
        }
    }

    /**
     * Called when the button is clicked.
     */
    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    /**
     * Play the default click sound.
     */
    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /**
     * Provide narration data (accessibility).
     */
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Use the tooltip for narration
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    /**
     * Functional interface for the button's click action.
     */
    @FunctionalInterface
    public interface OnPress {
        void onPress(CustomImageButton button);
    }
}