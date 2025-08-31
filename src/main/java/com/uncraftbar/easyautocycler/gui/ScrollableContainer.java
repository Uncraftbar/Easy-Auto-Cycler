package com.uncraftbar.easyautocycler.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrollable container widget that can hold and display multiple child widgets
 * with support for scrolling when content exceeds the visible area.
 */
public class ScrollableContainer extends AbstractWidget {
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private int scrollOffset = 0;
    private final int scrollBarWidth = 6;
    private int contentHeight = 0;
    private boolean isDraggingScrollbar = false;
    private static final int SCROLL_STEP = 15;

    public ScrollableContainer(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    /**
     * Adds a widget to this container
     */
    public void addWidget(AbstractWidget widget) {
        widgets.add(widget);
        updateContentHeight();
    }

    /**
     * Removes all widgets from this container
     */
    public void clearWidgets() {
        widgets.clear();
        scrollOffset = 0;
        contentHeight = 0;
    }

    /**
     * Updates the total height of content in this container
     */
    private void updateContentHeight() {
        contentHeight = 0;
        for (AbstractWidget widget : widgets) {
            int widgetBottom = widget.getY() - this.getY() + widget.getHeight();
            if (widgetBottom > contentHeight) {
                contentHeight = widgetBottom;
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            // Draw background
            guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0x80000000);
            
            // Set up scissor to clip widgets that are outside the visible area
            guiGraphics.enableScissor(
                getX(), 
                getY(),
                getX() + width, 
                getY() + height
            );
            
            // Draw children with scroll offset
            for (AbstractWidget widget : widgets) {
                if (isChildVisible(widget)) {
                    // Apply scroll offset by translating the graphics context
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, -scrollOffset, 0);
                    
                    // Render widget with adjusted mouse coordinates
                    widget.render(guiGraphics, mouseX, mouseY + scrollOffset, partialTick);
                    
                    guiGraphics.pose().popPose();
                }
            }
            
            // Disable scissor after drawing children
            guiGraphics.disableScissor();
            
            // Draw scrollbar if needed
            if (contentHeight > height) {
                int scrollBarHeight = Math.max(20, (height * height) / contentHeight);
                int scrollBarY = getY() + (int)((height - scrollBarHeight) * ((float)scrollOffset / (contentHeight - height)));
                
                // Draw scrollbar background
                guiGraphics.fill(getX() + width - scrollBarWidth, getY(), getX() + width, getY() + height, 0x40000000);
                
                // Draw scrollbar handle
                guiGraphics.fill(
                    getX() + width - scrollBarWidth,
                    scrollBarY,
                    getX() + width,
                    scrollBarY + scrollBarHeight,
                    isDraggingScrollbar ? 0xFFAAAAAA : 0xFFCCCCCC
                );
            }
        }
    }
    
    /**
     * Checks if a child widget is currently visible within the container's viewport
     */
    private boolean isChildVisible(AbstractWidget widget) {
        int childY = widget.getY() - this.getY() - scrollOffset;
        return (childY + widget.getHeight() > 0) && (childY < height);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.isHovered) {
            if (contentHeight > height) {
                scrollOffset = Math.max(0, Math.min(contentHeight - height, 
                    scrollOffset - (int)(scrollY * SCROLL_STEP)));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered) {
            // Check for scrollbar interaction
            if (contentHeight > height && mouseX >= getX() + width - scrollBarWidth && mouseX <= getX() + width) {
                isDraggingScrollbar = true;
                return true;
            }
            
            // Check for child widget interaction
            double adjustedMouseY = mouseY + scrollOffset;
            for (AbstractWidget widget : widgets) {
                if (isChildVisible(widget)) {
                    // Adjust mouse position for scrolling before checking child
                    if (widget.isMouseOver(mouseX, adjustedMouseY)) {
                        if (widget.mouseClicked(mouseX, adjustedMouseY, button)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDraggingScrollbar) {
            if (contentHeight > height) {
                // Calculate new scroll position based on drag
                float scrollPercent = (float)(mouseY - getY()) / height;
                scrollOffset = (int)((contentHeight - height) * scrollPercent);
                scrollOffset = Math.max(0, Math.min(contentHeight - height, scrollOffset));
                return true;
            }
        }
        
        // Check if any child widget wants to handle the drag
        double adjustedMouseY = mouseY + scrollOffset;
        for (AbstractWidget widget : widgets) {
            if (isChildVisible(widget) && widget.isMouseOver(mouseX, adjustedMouseY)) {
                if (widget.mouseDragged(mouseX, adjustedMouseY, button, dragX, dragY)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        
        // Check if any child widget wants to handle the release
        double adjustedMouseY = mouseY + scrollOffset;
        for (AbstractWidget widget : widgets) {
            if (isChildVisible(widget)) {
                if (widget.mouseReleased(mouseX, adjustedMouseY, button)) {
                    return true;
                }
            }
        }
        
        return false;
    }
      /**
     * Custom rendering method that can be called from the parent screen
     */
    public void customRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationOutput) {
        // Minimal narration for the container
        narrationOutput.add(NarratedElementType.TITLE, Component.translatable("narration.easyautocycler.scrollable_container"));
    }
    
    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
