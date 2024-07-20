/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.scene.control.TextArea;
import name.fraser.neil.plaintext.diff_match_patch;

import java.awt.im.InputContext;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static name.fraser.neil.plaintext.diff_match_patch.Operation.*;

public final class StringUtils {
    
    private static final Pattern FRENCH_LAYOUT_CHARACTERS = Pattern.compile("[&é\"'(\\-§è_!çà]");
    private static final Pattern LETTERS = Pattern.compile("^[A-Ya-y]");
    private static final Map<String, String> FRENCH_LAYOUT_CHARACTER_REPLACEMENT_MAP = Map.ofEntries(
            Map.entry("&", "1"),
            Map.entry("é", "2"),
            Map.entry("\"", "3"),
            Map.entry("'", "4"),
            Map.entry("(", "5"),
            Map.entry("-", "6"),
            Map.entry("§", "6"),
            Map.entry("è", "7"),
            Map.entry("_", "8"),
            Map.entry("!", "8"),
            Map.entry("ç", "9"),
            Map.entry("à", "0")
    );
    
    public static String removeBeforeNotEscaped(String string, String rejex){
        
        int fromIndex = 0;
        while(true){
            
            int index = string.indexOf(rejex, fromIndex);
            if(index == -1) return string;
            
            if(!string.startsWith("\\", index - 1)){
                if(index < string.length()) return string.substring(index + rejex.length());
                return "";
            }
            fromIndex = index + 1;
            
        }
    }
    
    public static Map.Entry<String, Integer> getLastInt(String expression){
        int index = expression.length() - 1;
        int lastInt = 0;
        int multiplier = 1;
        int lastIndexNonDigit = index;
        
        while(index >= 0 && Character.isDigit(expression.charAt(index))){
            lastInt += Character.getNumericValue(expression.charAt(index)) * multiplier;
            multiplier *= 10;
            lastIndexNonDigit = index - 1;
            index--;
        }
        
        if(lastIndexNonDigit == expression.length() - 1){
            return Map.entry(expression, -1);
        }
        
        var prefix = expression.substring(0, lastIndexNonDigit + 1);
        return Map.entry(prefix, lastInt);
    }
    
    public static String incrementName(String name){
        
        var lastIntData = getLastInt(name);
        
        if(lastIntData.getValue() != -1){
            return lastIntData.getKey() + (lastIntData.getValue() + 1);
        }
        
        if(name.length() == 1){
            if(LETTERS.matcher(name).replaceAll("").isEmpty()){
                return Character.toString(name.charAt(0) + 1);
            }
        }
        
        return name;
    }
    
    public static long countSpaces(String str){
        return count(str, ' ');
    }
    
    public static long count(String str, char toCount){
        return str.codePoints()
                .filter(codePoint -> codePoint == toCount)
                .count();
    }
    
    public static void editTextArea(TextArea area, String newText){
        
        AtomicInteger index = new AtomicInteger();
        LinkedList<diff_match_patch.Diff> diffs = new diff_match_patch().diff_main(area.getText(), newText);

        diffs.forEach(diff -> {
            if(diff.operation != EQUAL){
                int textAreaLength = area.getText().length();
                int start = Math.min(index.get(), textAreaLength);
                
                if(diff.operation == INSERT){
                    area.insertText(start, diff.text);
                }else if(diff.operation == DELETE){
                    int end = Math.min(index.get() + diff.text.length(), textAreaLength);
                    area.deleteText(start, end);
                }
            }
            index.addAndGet(diff.text.length());

        });
    }
    
    public static String removeBeforeLastOccurrence(String string, String match){
        if(match.isEmpty()) return string;
        
        int index = string.lastIndexOf(match);
        return index == -1 ? string : string.substring(index + match.length());
    }
    
    public static String removeAfterLastOccurrence(String string, String match){
        if(match.isEmpty()) return string;
        
        int index = string.lastIndexOf(match);
        return index == -1 ? string : string.substring(0, index);
        
    }
    
    public static String removeAfterLastOccurrenceIgnoringCase(String string, String match){
        if(match.isEmpty()) return string;
        
        int index = string.toLowerCase().lastIndexOf(match.toLowerCase());
        return index == -1 ? string : string.substring(0, index);
    }
    public static String removeAfterLastOccurrenceIgnoringCase(String string, String[] matches){
        return Arrays.stream(matches)
                .map(match -> string.toLowerCase().lastIndexOf(match.toLowerCase()))
                .filter(index -> index != -1)
                .max(Comparator.naturalOrder())
                .map(index -> string.substring(0, index))
                .orElse(string);
    }
    
    public static List<Charset> getAvailableCharsets(){
        var charsets = Set.of(StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16BE,
                Charset.defaultCharset());
        return List.of(charsets.toArray(new Charset[0]));
    }
    
    public static String[] cleanArray(String[] array){
        return Arrays.stream(array).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
    }
    
    public static Boolean getBoolean(String text){
        try{
            return Boolean.parseBoolean(text.toLowerCase());
        }catch(IllegalArgumentException e){
            return null;
        }
    }
    
    public static boolean endsIn(final String[] array, String v, boolean caseSensitive){
        var finalV = caseSensitive ? v : v.toLowerCase();
        return Arrays.stream(array)
                .filter(Objects::nonNull)
                .anyMatch(e -> caseSensitive ? e.endsWith(finalV) : e.toLowerCase().endsWith(finalV));
    }

    public static boolean containsIgnoreCase(final String[] array, final String v){
        return contains(array, v, false);
    }
    
    public static boolean contains(final String[] array, final String v, boolean caseSensitive){
        return Arrays.stream(array)
                .filter(Objects::nonNull)
                .anyMatch(e -> caseSensitive ? e.equals(v) : e.equalsIgnoreCase(v));
    }
    
    public static String replaceSymbolsToDigitsIfFrenchLayout(String text){
        
        if(!isAzertyLayout()) return text;
        
        var matcher = FRENCH_LAYOUT_CHARACTERS.matcher(text);
        var sb = new StringBuilder();
        while(matcher.find()){
            matcher.appendReplacement(sb, FRENCH_LAYOUT_CHARACTER_REPLACEMENT_MAP.get(matcher.group()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    public static boolean isAzertyLayout(){
        var instance = InputContext.getInstance();
        return instance.getLocale() != null && instance.getLocale().getLanguage().equals("fr");
    }
    
    public static char getCsvSeparator(){
        if(TR.tr("chars.csvSeparator").charAt(0) == ';') return ';';
        return ',';
    }
    
}
