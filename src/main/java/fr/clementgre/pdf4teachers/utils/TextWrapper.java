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

    private String getWrappedLine() {
        return wrappedLine.toString();
    }

    private boolean exceedsMaxWidth(String line) {
        var toTest = new Text(line);
        toTest.setFont(font);
        return toTest.getBoundsInParent().getWidth() >= maxWidth;
    }

    private String[] fillLine(String text, boolean splitByWords) {
        var parts = splitByWords ?
                Arrays.asList(text.split(" ", -1)) :
                text.chars().mapToObj(Character::toString).collect(Collectors.toList());

        var line = new StringBuilder(parts.get(0));

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
    
    public static String wrapFirstLineWithEllipsis(String text, Font font, int maxWidth){
        
        String wrappedText = new TextWrapper(text, font, (int) (maxWidth - font.getSize() * 1.1)).wrapFirstLine();
        text = text.replaceFirst(Pattern.quote(wrappedText), "");
        
        // SECOND LINE
        if(!text.isBlank()){
            return wrappedText.trim() + "...";
        }
        return wrappedText.trim();
    }
    
    public static String wrapTwoFirstLinesWithEllipsis(String text, Font font, int maxWidth){
    
        String wrappedText = new TextWrapper(text, font, maxWidth).wrapFirstLine();
        text = text.replaceFirst(Pattern.quote(wrappedText), "");
    
        // SECOND LINE
        if(!text.isBlank()){
            String wrapped = new TextWrapper(text, font, (int) (maxWidth - font.getSize() * 1.1)).wrapFirstLine();
            wrappedText = wrappedText.trim() + "\n" + wrapped.trim();
            if(!text.replaceFirst(Pattern.quote(wrapped), "").isBlank()) wrappedText += "...";
        }
        
        return wrappedText.trim();
    }
    
    public boolean doHasWrapped(){
        return hasWrapped;
    }
    
    private void appendLine(String text){
        if(wrappedLine == null) wrappedLine = text;
        else wrappedLine += "\n" + text;
    }
    private String getWrappedLine(){
        return wrappedLine == null ? "" : wrappedLine;
    }
    
    private boolean test(String line){
        ScratchText toTest = new ScratchText(line);
        toTest.setFont(font);
        return toTest.getBoundsInParent().getWidth() < maxWidth;
    }
    
    private String[] fillLineWithWord(String text){
        
        String[] splitted = text.split(" ", -1);
        StringBuilder line = new StringBuilder(splitted[0]);
        
        for(int i = 1; i < splitted.length; i++){ // Remplis la ligne avec le maximum de mots puis renvoie la ligne
            
            String lastLine = String.valueOf(line);
            line.append(" ").append(splitted[i]);
            
            if(!test(String.valueOf(line))){
                StringBuilder remaining = new StringBuilder(splitted[i]);
                for(i++; i < splitted.length; i++){ // Remplis la ligne avec le maximum de mots puis renvoie la ligne
                    remaining.append(" ").append(splitted[i]);
                }
                return new String[]{lastLine, remaining.toString()};
            }
        }
        
        return new String[]{String.valueOf(line), ""};
    }
    
    private String[] fillLineWithChar(String word){
        
        if(word.isEmpty()) return new String[]{"", ""};
        String line = word.substring(0, 1);
        
        for(int i = 1; i < word.length(); i++){ // Remplis la ligne avec le maximum de mots puis renvoie la ligne
            String lastLine = line;
            line = word.substring(0, i + 1);
            
            if(!test(line)){
                return new String[]{lastLine, word.substring(i)};
            }
        }
        return new String[]{line, ""};
        
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
