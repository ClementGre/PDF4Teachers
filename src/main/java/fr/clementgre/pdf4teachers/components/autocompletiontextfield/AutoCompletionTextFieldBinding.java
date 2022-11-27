/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components.autocompletiontextfield;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;

public class AutoCompletionTextFieldBinding<T> extends AutoCompletionBinding<T> {

    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(T t) {
                return t == null ? null : t.toString();
            }

            @SuppressWarnings("unchecked")
            @Override
            public T fromString(String string) {
                return (T) string;
            }
        };
    }

    private final StringConverter<T> converter;

    public AutoCompletionTextFieldBinding(final TextField textField, Callback<ISuggestionRequest, Collection<T>> suggestionProvider) {
        this(textField, suggestionProvider, AutoCompletionTextFieldBinding.defaultStringConverter());
    }
    
    public AutoCompletionTextFieldBinding(final TextField textField, Callback<ISuggestionRequest, Collection<T>> suggestionProvider, final StringConverter<T> converter) {
        super(textField, suggestionProvider, converter);
        this.converter = converter;
        
        getCompletionTarget().textProperty().addListener(textChangeListener);
        getCompletionTarget().focusedProperty().addListener(focusChangedListener);
    }

    
    
    @Override
    public TextField getCompletionTarget(){
        return (TextField) super.getCompletionTarget();
    }
    
    @Override
    public void dispose(){
        getCompletionTarget().textProperty().removeListener(textChangeListener);
        getCompletionTarget().focusedProperty().removeListener(focusChangedListener);
    }
    
    @Override
    protected void completeUserInput(T completion){
        String newText = converter.toString(completion);
        getCompletionTarget().setText(newText);
        getCompletionTarget().positionCaret(newText.length());
    }

    
    
    private final ChangeListener<String> textChangeListener = (obs, oldText, newText) -> {
        if(getCompletionTarget().isFocused()) setUserInput(newText);
    };
    
    private final ChangeListener<Boolean> focusChangedListener = (obs, oldFocused, newFocused) -> {
        if(!newFocused) hidePopup();
    };
}
