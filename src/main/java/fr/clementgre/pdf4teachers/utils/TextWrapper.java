/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.components.ScratchText;
import javafx.scene.text.Font;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TextWrapper {

    private final Font font;
    private final int maxWidth;

    private String text;
    private final StringBuilder wrappedLine = new StringBuilder();
    private boolean hasWrapped;
    
    public TextWrapper(String text, Font font, int maxWidth){
        this.font = font;
        this.text = text;
        this.maxWidth = maxWidth;
    }

    public boolean hasWrapped(){
        return hasWrapped;
    }

    private void appendLine(String text) {
        if (wrappedLine.isEmpty()) {
            wrappedLine.append(text);
        } else {
            wrappedLine.append('\n').append(text);
        }
    }

    private boolean exceedsMaxWidth(String line) {
        var toTest = new ScratchText(line);
        toTest.setFont(font);
        return toTest.getBoundsInParent().getWidth() >= maxWidth;
    }

    private String[] fillLine(String text, boolean splitByWords) {
        var parts = splitByWords ?
                Arrays.asList(text.split(" ", -1)) :
                text.chars().mapToObj(Character::toString).collect(Collectors.toList());

        var line = new StringBuilder(parts.getFirst());

        for (int i = 1; i < parts.size(); i++) {
            String lastLine = line.toString();
            line.append(splitByWords ? " " : "").append(parts.get(i));

            if (exceedsMaxWidth(line.toString())) {
                String remaining = String.join(splitByWords ? " " : "", parts.subList(i, parts.size()));
                return new String[]{lastLine, remaining};
            }
        }

        return new String[]{line.toString(), ""};
    }

    public String wrapFirstLine() {
        var results = wrapTextLineByWordsOrChars(text);
        if (!results[1].isEmpty()) hasWrapped = true;
        return results[0];
    }

    public String wrap() {
        if (text == null) return "";

        while (!text.isEmpty()) {
            var results = wrapTextLineByWordsOrChars(text);
            if (!results[1].isEmpty()) hasWrapped = true;

            appendLine(results[0]);
            text = results[1];
        }
        return getWrappedLine();
    }
    
    private String getWrappedLine(){
        return wrappedLine.isEmpty() ? "" : wrappedLine.toString();
    }
    
    private String[] wrapTextLineByWordsOrChars(String text) {
        String firstWord = text.split(" ", -1)[0];
        
        // Split between chars
        if (exceedsMaxWidth(firstWord)) {
            return fillLine(text, false);
        }
        
        // Split between words
        return fillLine(text, true);
    }

}
