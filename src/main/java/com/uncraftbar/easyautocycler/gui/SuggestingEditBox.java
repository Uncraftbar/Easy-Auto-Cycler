package com.uncraftbar.easyautocycler.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SuggestingEditBox extends EditBox {

    private List<String> suggestions = new ArrayList<>();
    private final Consumer<String> onAcceptSuggestion;
    private String currentSuggestion = null;

    public SuggestingEditBox(Font font, int x, int y, int width, int height, Component message) {
        this(font, x, y, width, height, message, List.of(), null);
    }

    public SuggestingEditBox(Font font, int x, int y, int width, int height, Component message,
                             List<String> validSuggestions, Consumer<String> onAcceptSuggestion) {
        super(font, x, y, width, height, message);
        this.suggestions = validSuggestions != null ? new ArrayList<>(validSuggestions) : new ArrayList<>();
        this.onAcceptSuggestion = onAcceptSuggestion != null ? onAcceptSuggestion : (s) -> {};
        
        // Set up a value change listener to update suggestions
        this.setResponder(this::updateSuggestions);
    }
    
    /**
     * Sets the list of valid suggestions for this edit box
     */
    public void setSuggestions(List<String> newSuggestions) {
        this.suggestions = newSuggestions != null ? new ArrayList<>(newSuggestions) : new ArrayList<>();
        updateSuggestions(this.getValue());
    }

    @Override
    public void setSuggestion(@Nullable String suggestion) {
        super.setSuggestion(suggestion);
        this.currentSuggestion = suggestion;
    }
    
    /**
     * Update the displayed suggestion based on current text
     */
    private void updateSuggestions(String currentText) {
        if (currentText.isEmpty()) {
            setSuggestion(null);
            return;
        }
        
        currentText = currentText.toLowerCase();
        
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(currentText.toLowerCase())) {
                String remainingText = suggestion.substring(currentText.length());
                setSuggestion(remainingText);
                return;
            }
        }
        
        setSuggestion(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isFocused() && keyCode == GLFW.GLFW_KEY_TAB && this.currentSuggestion != null && !this.currentSuggestion.isEmpty()) {
            String currentValue = this.getValue();
            String suggestionText = this.currentSuggestion;
            String potentialCompletion = currentValue + suggestionText;

            for (String suggestion : suggestions) {
                if (suggestion.equalsIgnoreCase(potentialCompletion)) {
                    this.setValue(suggestion); // Use the original case from suggestions
                    this.moveCursorToEnd();
                    this.setHighlightPos(this.getCursorPosition());
                    updateSuggestions(suggestion);
                    if (onAcceptSuggestion != null) {
                        onAcceptSuggestion.accept(this.getValue());
                    }
                    return true;
                }
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}