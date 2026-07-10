package com.uncraftbar.easyautocycler.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Shared, texture-free visual language for the configuration screens.
 *
 * <p>Keeping colors, spacing and primitive drawing in one class makes the UI
 * straightforward to port: version-specific screens only need to adapt the
 * handful of GuiGraphics calls in this file.</p>
 */
public final class GuiTheme {
    public static final int PANEL = 0xF21A1D24;
    public static final int PANEL_ALT = 0xF2242832;
    public static final int CARD = 0xE82D323D;
    public static final int CARD_HOVER = 0xF2383E4B;
    public static final int BORDER = 0xFF454C5B;
    public static final int ACCENT = 0xFF50C878;
    public static final int ACCENT_DIM = 0xFF28643C;
    public static final int DANGER = 0xFFD9534F;
    public static final int TEXT = 0xFFF4F6F8;
    public static final int MUTED = 0xFFAAB2BF;
    public static final int FAINT = 0xFF747D8C;

    public static final int OUTER_MARGIN = 10;
    public static final int PANEL_PADDING = 12;
    public static final int GAP = 6;
    public static final int CONTROL_HEIGHT = 20;

    private GuiTheme() {}

    public static void panel(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, 0xB0000000);
        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, left + 3, bottom, ACCENT);
    }

    public static void section(GuiGraphics graphics, int left, int top, int right, int bottom) {
        graphics.fill(left, top, right, bottom, PANEL_ALT);
        outline(graphics, left, top, right, bottom, BORDER);
    }

    public static void card(GuiGraphics graphics, int left, int top, int right, int bottom, boolean hovered,
                            boolean enabled) {
        graphics.fill(left, top, right, bottom, hovered ? CARD_HOVER : CARD);
        graphics.fill(left, top, left + 3, bottom, enabled ? ACCENT : FAINT);
        outline(graphics, left, top, right, bottom, hovered ? ACCENT_DIM : BORDER);
    }

    public static void outline(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.fill(left, top, right, top + 1, color);
        graphics.fill(left, bottom - 1, right, bottom, color);
        graphics.fill(left, top, left + 1, bottom, color);
        graphics.fill(right - 1, top, right, bottom, color);
    }
}
