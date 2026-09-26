package com.uncraftbar.easyautocycler.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Real widget/event logic against 26.3 classes; only font/rendering and focus are isolated. */
class SuggestingEditBoxTest {
    private TestBox box;
    private GuiGraphicsExtractor graphics;

    private static class TestBox extends SuggestingEditBox {
        private boolean focused = true;

        TestBox(Font font) {
            super(font, 20, 30, 150, 20, Component.literal("Item"),
                    List.of("minecraft:diamond", "minecraft:diamond_axe", "minecraft:diamond_sword"));
            setMaxLength(256);
        }

        // Avoid needing a native window/IME just to exercise event handling.
        @Override public boolean isFocused() { return focused; }
        @Override public void setFocused(boolean value) { focused = value; }
    }

    @BeforeEach
    void setup() {
        Font font = mock(Font.class);
        when(font.plainSubstrByWidth(anyString(), anyInt())).thenAnswer(i -> i.getArgument(0));
        when(font.plainSubstrByWidth(anyString(), anyInt(), anyBoolean())).thenAnswer(i -> i.getArgument(0));
        when(font.width(anyString())).thenAnswer(i -> ((String) i.getArgument(0)).length() * 6);
        box = new TestBox(font);
        graphics = mock(GuiGraphicsExtractor.class);
        when(graphics.guiWidth()).thenReturn(400);
        when(graphics.guiHeight()).thenReturn(300);
        box.setValue("dia");
    }

    private KeyEvent key(int scancode, int keycode) {
        return new KeyEvent(scancode, keycode, 0);
    }

    private MouseButtonEvent click(double x, double y, int button) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0));
    }

    @Test void tabAcceptsInsteadOfFallingThroughToFocusNavigation() {
        assertTrue(box.keyPressed(key(InputConstants.KEY_TAB, InputConstants.KEYCODE_TAB)));
        assertEquals("minecraft:diamond", box.getValue());
        assertTrue(box.isFocused());
        assertEquals(box.getValue().length(), box.getCursorPosition());
        assertEquals("", box.getHighlighted());
    }

    @Test void arrowsSelectAndBothEnterKeysAccept() {
        assertTrue(box.keyPressed(key(InputConstants.KEY_DOWN, InputConstants.KEYCODE_DOWN)));
        assertTrue(box.keyPressed(key(InputConstants.KEY_RETURN, InputConstants.KEYCODE_RETURN)));
        assertEquals("minecraft:diamond_axe", box.getValue());
        box.setValue("dia");
        assertTrue(box.keyPressed(key(InputConstants.KEY_UP, InputConstants.KEYCODE_UP)));
        assertTrue(box.keyPressed(key(InputConstants.KEY_NUMPADENTER, InputConstants.KEYCODE_NUMPADENTER)));
        assertEquals("minecraft:diamond_sword", box.getValue());
    }

    @Test void tabWithoutMatchesRemainsAvailableForNormalNavigation() {
        box.setValue("no_such_item");
        assertFalse(box.keyPressed(key(InputConstants.KEY_TAB, InputConstants.KEYCODE_TAB)));
        box.setValue("");
        assertFalse(box.keyPressed(key(InputConstants.KEY_TAB, InputConstants.KEYCODE_TAB)));
    }

    @Test void screenDispatchesTabToFocusedAutocompleteBeforeNavigating() {
        FilterEditorScreen screen = mock(FilterEditorScreen.class, CALLS_REAL_METHODS);
        when(screen.getFocused()).thenReturn(box);
        assertTrue(screen.keyPressed(key(InputConstants.KEY_TAB, InputConstants.KEYCODE_TAB)));
        assertEquals("minecraft:diamond", box.getValue());
        verify(screen, never()).clearFocus();
    }

    @Test void leftClickAcceptsEachRenderedRowOutsideTheEditBox() {
        List<String> expected = List.of("minecraft:diamond", "minecraft:diamond_axe", "minecraft:diamond_sword");
        for (int row = 0; row < expected.size(); row++) {
            box.setValue("dia");
            box.extractSuggestionList(graphics, 0, 0);
            assertTrue(box.clickSuggestion(click(25, 54 + row * 12, InputConstants.MOUSE_BUTTON_LEFT)));
            assertEquals(expected.get(row), box.getValue());
            assertTrue(box.isFocused());
        }
    }

    @Test void popupAboveFieldAlsoAcceptsClicks() {
        box.setY(270);
        box.extractSuggestionList(graphics, 0, 0);
        assertTrue(box.clickSuggestion(click(25, 232, InputConstants.MOUSE_BUTTON_LEFT)));
        assertEquals("minecraft:diamond", box.getValue());
    }

    @Test void ignoresWrongButtonsOutsideClicksAndUnfocusedOrEmptyPopup() {
        box.extractSuggestionList(graphics, 0, 0);
        assertFalse(box.clickSuggestion(click(25, 54, 0))); // obsolete GLFW left button
        assertFalse(box.clickSuggestion(click(25, 54, InputConstants.MOUSE_BUTTON_RIGHT)));
        assertFalse(box.clickSuggestion(click(19, 54, InputConstants.MOUSE_BUTTON_LEFT)));
        box.setFocused(false);
        assertFalse(box.clickSuggestion(click(25, 54, InputConstants.MOUSE_BUTTON_LEFT)));
        box.setFocused(true);
        box.setValue(""); // stale last-render bounds must not consume this click
        assertFalse(box.clickSuggestion(click(25, 54, InputConstants.MOUSE_BUTTON_LEFT)));
    }

    @Test void editorRoutesPopupClicksBeforeOverlappingWidgetsForAllThreeFields() throws Exception {
        // Bypass Screen's native-client constructor, but run its actual mouseClicked override.
        FilterEditorScreen screen = mock(FilterEditorScreen.class, CALLS_REAL_METHODS);
        for (String fieldName : List.of("enchantmentIdInput", "itemIdInput", "paymentItemInput")) {
            Field field = FilterEditorScreen.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(screen, box);
            box.setValue("dia");
            box.extractSuggestionList(graphics, 0, 0);
            assertTrue(screen.mouseClicked(click(25, 66, InputConstants.MOUSE_BUTTON_LEFT), false));
            assertEquals("minecraft:diamond_axe", box.getValue());
            field.set(screen, null);
        }
    }
}
