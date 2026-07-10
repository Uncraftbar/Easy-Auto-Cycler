package com.uncraftbar.easyautocycler.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Registry-ID input with command-style completion.
 *
 * <p>Queries without a namespace match the path portion of every ID, so
 * {@code mend} suggests {@code minecraft:mending}. Queries containing a colon
 * continue to match the complete namespaced ID.</p>
 */
public class SuggestingEditBox extends EditBox {
    private static final int MAX_VISIBLE_SUGGESTIONS = 6;
    private static final int SUGGESTION_ROW_HEIGHT = 12;

    private final Font font;
    private final Consumer<String> onAcceptSuggestion;
    private List<String> suggestions = new ArrayList<>();
    private List<String> matches = List.of();
    private int selectedSuggestion;

    private int dropdownX;
    private int dropdownY;
    private int dropdownWidth;
    private int dropdownHeight;

    public SuggestingEditBox(Font font, int x, int y, int width, int height, Component message) {
        this(font, x, y, width, height, message, List.of(), null);
    }

    public SuggestingEditBox(Font font, int x, int y, int width, int height, Component message,
                             List<String> validSuggestions, @Nullable Consumer<String> onAcceptSuggestion) {
        super(font, x, y, width, height, message);
        this.font = font;
        this.suggestions = validSuggestions == null ? new ArrayList<>() : new ArrayList<>(validSuggestions);
        this.onAcceptSuggestion = onAcceptSuggestion == null ? ignored -> {} : onAcceptSuggestion;
        setResponder(this::updateSuggestions);
    }

    public void setSuggestions(List<String> newSuggestions) {
        suggestions = newSuggestions == null ? new ArrayList<>() : new ArrayList<>(newSuggestions);
        updateSuggestions(getValue());
    }

    private void updateSuggestions(String currentText) {
        String query = currentText.trim().toLowerCase(Locale.ROOT);
        selectedSuggestion = 0;

        if (query.isEmpty()) {
            matches = List.of();
            setSuggestion(null);
            return;
        }

        boolean namespaced = query.indexOf(':') >= 0;
        matches = suggestions.stream()
            .filter(id -> matches(id, query, namespaced))
            .filter(id -> !id.equalsIgnoreCase(query))
            .limit(MAX_VISIBLE_SUGGESTIONS)
            .toList();

        if (matches.isEmpty()) {
            setSuggestion(null);
            return;
        }

        String selected = matches.getFirst();
        String comparison = namespaced ? selected : path(selected);
        setSuggestion(comparison.regionMatches(true, 0, query, 0, query.length())
            ? comparison.substring(query.length()) : null);
    }

    private static boolean matches(String id, String query, boolean namespaced) {
        String lowerId = id.toLowerCase(Locale.ROOT);
        if (namespaced) return lowerId.startsWith(query);
        return path(lowerId).startsWith(query) || lowerId.startsWith(query);
    }

    private static String path(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isFocused() && !matches.isEmpty()) {
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                selectedSuggestion = (selectedSuggestion + 1) % matches.size();
                updateInlineSuggestion();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP) {
                selectedSuggestion = (selectedSuggestion - 1 + matches.size()) % matches.size();
                updateInlineSuggestion();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER
                    || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                acceptSuggestion(selectedSuggestion);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateInlineSuggestion() {
        String query = getValue().trim();
        String selected = matches.get(selectedSuggestion);
        String comparison = query.indexOf(':') >= 0 ? selected : path(selected);
        setSuggestion(comparison.regionMatches(true, 0, query, 0, query.length())
            ? comparison.substring(query.length()) : null);
    }

    private void acceptSuggestion(int index) {
        if (index < 0 || index >= matches.size()) return;
        setValue(matches.get(index));
        moveCursorToEnd(false);
        setHighlightPos(getCursorPosition());
        onAcceptSuggestion.accept(getValue());
    }

    /** Render after the screen's normal widgets so the popup stays above neighboring fields. */
    public void renderSuggestionList(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isFocused() || matches.isEmpty()) {
            dropdownHeight = 0;
            return;
        }

        dropdownWidth = getWidth();
        for (String match : matches) dropdownWidth = Math.max(dropdownWidth, font.width(match) + 10);
        dropdownWidth = Math.min(dropdownWidth, graphics.guiWidth() - getX() - 4);
        dropdownHeight = matches.size() * SUGGESTION_ROW_HEIGHT + 2;
        dropdownX = getX();
        dropdownY = getY() + getHeight() + 2;
        if (dropdownY + dropdownHeight > graphics.guiHeight() - 4) {
            dropdownY = getY() - dropdownHeight - 2;
        }

        graphics.fill(dropdownX - 1, dropdownY - 1, dropdownX + dropdownWidth + 1,
            dropdownY + dropdownHeight + 1, 0xFF050607);
        graphics.fill(dropdownX, dropdownY, dropdownX + dropdownWidth,
            dropdownY + dropdownHeight, 0xF01A1D24);

        for (int index = 0; index < matches.size(); index++) {
            int rowY = dropdownY + 1 + index * SUGGESTION_ROW_HEIGHT;
            boolean hovered = mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth
                && mouseY >= rowY && mouseY < rowY + SUGGESTION_ROW_HEIGHT;
            if (index == selectedSuggestion || hovered) {
                graphics.fill(dropdownX + 1, rowY, dropdownX + dropdownWidth - 1,
                    rowY + SUGGESTION_ROW_HEIGHT, 0xFF3B4250);
            }
            graphics.drawString(font, matches.get(index), dropdownX + 4, rowY + 2,
                index == selectedSuggestion ? 0xFFFFFFFF : 0xFFD5DAE2, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isFocused() && dropdownHeight > 0
                && mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth
                && mouseY >= dropdownY && mouseY < dropdownY + dropdownHeight) {
            int index = ((int) mouseY - dropdownY - 1) / SUGGESTION_ROW_HEIGHT;
            acceptSuggestion(Math.max(0, Math.min(matches.size() - 1, index)));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
