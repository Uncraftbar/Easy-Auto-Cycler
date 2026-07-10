package com.uncraftbar.easyautocycler.gui;

/** Pure layout math shared by screens and intentionally independent of Minecraft APIs. */
public final class GuiLayout {
    private static final int OUTER_MARGIN = 10;
    private static final int PANEL_PADDING = 12;

    private GuiLayout() {}

    public static Bounds centeredPanel(int screenWidth, int screenHeight, int preferredWidth, int preferredHeight) {
        int width = Math.max(240, Math.min(preferredWidth, screenWidth - OUTER_MARGIN * 2));
        int height = Math.max(180, Math.min(preferredHeight, screenHeight - OUTER_MARGIN * 2));
        return new Bounds((screenWidth - width) / 2, (screenHeight - height) / 2, width, height);
    }

    public record Bounds(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }
        public int innerLeft() { return x + PANEL_PADDING; }
        public int innerRight() { return right() - PANEL_PADDING; }
        public int innerWidth() { return width - PANEL_PADDING * 2; }
    }
}
