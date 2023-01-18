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
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static name.fraser.neil.plaintext.diff_match_patch.Operation.*;

public class StringUtils {

    private static final Pattern FRENCH_LAYOUT_CHARACTERS = Pattern.compile("[&é\"'(\\-è_çà]");
    private static final Pattern LETTERS = Pattern.compile("^[A-Ya-y]");
    private static final Map<String, String> FRENCH_LAYOUT_CHARACTER_REPLACEMENT_MAP = Map.of(
            "&", "1",
            "é", "2",
            "\"", "3",
            "'", "4",
            "(", "5",
            "-", "6",
            "è", "7",
            "_", "8",
            "ç", "9",
            "à", "0"
    );

    public static String removeBeforeNotEscaped(String string, String rejex){
        
        int fromIndex = 0;
        while(true){
            
            int index = string.indexOf(rejex, fromIndex);
            if(index == -1) return string;
            
            if(!string.startsWith("\\", index - 1)){
                if(index < string.length()) return string.substring(index + rejex.length());
                return "";
            }else{
                fromIndex = index + 1;
            }
            
        }
    }

    public static Entry<String, Integer> getLastInt(String expression) {
        int start = expression.length();

        for (int i = expression.length() - 1; i >= 0; i--) {
            if (Character.isDigit(expression.charAt(i))) {
                start = i;
            } else {
                break;
            }
        }

        if (start == expression.length()) return Map.entry(expression, -1);

        int end = expression.length();
        return Map.entry(expression.substring(0, start), Integer.parseInt(expression.substring(start, end)));
    }

    public static String incrementName(String name) {

        var lastIntData = getLastInt(name);

        if (lastIntData.getValue() != -1) {
            return lastIntData.getKey() + (lastIntData.getValue() + 1);
        }

        if (name.length() == 1) {
            if (LETTERS.matcher(name).replaceAll("").isEmpty()) {
                return Character.toString(name.charAt(0) + 1);
            }
        }

        return name;
    }
    
    public static long countSpaces(String str){
        return str.codePoints()
                .filter(codePoint -> codePoint == ' ')
                .count();
    }
    public static long count(String str, char toCount){
        return str.codePoints()
                .filter(codePoint -> codePoint == toCount)
                .count();
    }

    public static void editTextArea(TextArea area, String newText) {

        var index = new AtomicInteger();
        var diffs = new diff_match_patch().diff_main(area.getText(), newText);

        diffs.stream()
            .filter(diff -> diff.operation != EQUAL)
            .forEach(diff -> {

                var textAreaLength = area.getText().length();
                var start = Math.min(index.get(), textAreaLength);

                if (diff.operation == INSERT) {
                    area.insertText(start, diff.text);
                } else if (diff.operation == DELETE) {
                    var end = Math.min(index.get() + diff.text.length(), textAreaLength);
                    area.deleteText(start, end);
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

    public static List<Charset> getAvailableCharsets() {
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
        return Arrays.stream(array).filter(x -> !x.isBlank()).toArray(String[]::new);
    }

    public static Boolean getBoolean(String text) {
        try {
            return Boolean.parseBoolean(text.toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static <T> boolean contains(final T[] array, final T v) {
        return Arrays.stream(array).anyMatch(Predicate.isEqual(v));
    }

    public static boolean endsIn(final String[] array, String v, boolean kase) {
        var finalV = kase ? v : v.toLowerCase();
        return Arrays.stream(array)
                .filter(Objects::nonNull)
                .anyMatch(e -> kase ? e.endsWith(finalV) : e.toLowerCase().endsWith(finalV));
    }

    public static boolean contains(final String[] array, final String v, boolean kase) {
        return Arrays.stream(array)
                .filter(Objects::nonNull)
                .anyMatch(e -> kase ? e.equals(v) : e.equalsIgnoreCase(v));
    }


    public static String replaceSymbolsToDigitsIfFrenchLayout(String text) {

        if(!isAzertyLayout()) return text;

        var matcher = FRENCH_LAYOUT_CHARACTERS.matcher(text);
        var sb = new StringBuilder();
        while (matcher.find()) {
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
