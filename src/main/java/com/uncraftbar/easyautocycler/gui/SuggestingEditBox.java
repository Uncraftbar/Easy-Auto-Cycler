package com.uncraftbar.easyautocycler.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SuggestingEditBox extends EditBox {

    private List<String> suggestions;
    private @Nullable String currentSuggestion;

    public SuggestingEditBox(Font font, int x, int y, int width, int height, Component message,
                             List<String> validSuggestions) {
        super(font, x, y, width, height, message);
        this.suggestions = validSuggestions != null ? new ArrayList<>(validSuggestions) : new ArrayList<>();
        this.setResponder(this::updateSuggestions);
    }

    public void setSuggestions(List<String> newSuggestions) {
        this.suggestions = newSuggestions != null ? new ArrayList<>(newSuggestions) : new ArrayList<>();
        updateSuggestions(this.getValue());
    }

    @Override
    public void setSuggestion(@Nullable String suggestion) {
        super.setSuggestion(suggestion);
        this.currentSuggestion = suggestion;
    }

    private void updateSuggestions(String currentText) {
        if (currentText.isEmpty()) {
            setSuggestion(null);
            return;
        }

        String lowerText = currentText.toLowerCase();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(lowerText)) {
                setSuggestion(suggestion.substring(currentText.length()));
                return;
            }
        }

        setSuggestion(null);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.isFocused()
                && event.key() == GLFW.GLFW_KEY_TAB
                && this.currentSuggestion != null
                && !this.currentSuggestion.isEmpty()) {
            String currentValue = this.getValue();
            String suggestionText = this.currentSuggestion;
            String potentialCompletion = currentValue + suggestionText;

            for (String suggestion : suggestions) {
                if (suggestion.equalsIgnoreCase(potentialCompletion)) {
                    this.setValue(suggestion);
                    this.moveCursorToEnd(false);
                    this.setHighlightPos(this.getCursorPosition());
                    updateSuggestions(suggestion);
                    return true;
                }
            }
        }

        return super.keyPressed(event);
    }
}
