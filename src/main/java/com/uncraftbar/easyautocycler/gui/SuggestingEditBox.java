package com.uncraftbar.easyautocycler.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class SuggestingEditBox extends EditBox {

    private final List<String> validSuggestions;
    private final Consumer<String> onAcceptSuggestion;
    private String currentSuggestion = null;


    public SuggestingEditBox(Font font, int x, int y, int width, int height, Component message,
                             List<String> validSuggestions, Consumer<String> onAcceptSuggestion) {
        super(font, x, y, width, height, message);
        this.validSuggestions = validSuggestions != null ? validSuggestions : List.of();
        this.onAcceptSuggestion = onAcceptSuggestion != null ? onAcceptSuggestion : (s) -> {};
    }


    @Override
    public void setSuggestion(@Nullable String suggestion) {
        super.setSuggestion(suggestion);
        this.currentSuggestion = suggestion;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isFocused() && keyCode == GLFW.GLFW_KEY_TAB && this.currentSuggestion != null && !this.currentSuggestion.isEmpty()) {

            String currentValue = this.getValue();
            String suggestionText = this.currentSuggestion;
            String potentialCompletion = currentValue + suggestionText;

            if (this.validSuggestions.contains(potentialCompletion)) {
                this.setValue(potentialCompletion);
                this.setSuggestion(null);
                this.onAcceptSuggestion.accept(this.getValue());
                return true;
            } else {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}