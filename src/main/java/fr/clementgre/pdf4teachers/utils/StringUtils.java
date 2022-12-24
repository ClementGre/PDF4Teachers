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
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern FRENCH_LAYOUT_CHARACTERS = Pattern.compile("[&é\"'(\\-è_çà]");
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
    
    public static String removeBefore(String string, String rejex){
        if(rejex.isEmpty()) return string;
        int index = string.indexOf(rejex);
        
        if(index == -1) return string;
        if(index < string.length()) return string.substring(index + rejex.length());
        
        return "";
    }
    
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
    
    public static Entry<String, Integer> getLastInt(String expression){
        String stringResult = expression;
        StringBuilder result = new StringBuilder();
        
        for(int i = expression.length() - 1; i >= 0; i--){
            try{
                result.append(Integer.parseInt(expression.substring(i, i + 1)));
                stringResult = stringResult.substring(0, i);
            }catch(NumberFormatException ignored){
                break;
            }
        }
        
        if(result.toString().isEmpty()) return Map.entry(expression, -1);
        return Map.entry(stringResult, Integer.parseInt(result.reverse().toString()));
    }
    
    public static String incrementName(String name){
        
        Entry<String, Integer> lastIntData = getLastInt(name);
        
        if(lastIntData.getValue() != -1){
            return lastIntData.getKey() + (lastIntData.getValue() + 1);
        }
        
        if(name.length() == 1){
            if(name.replaceAll("^[A-Ya-y]", "").isEmpty()){
                return Character.toString(name.charAt(0) + 1);
            }
        }
        
        return name;
    }
    
    
    public static long countSpaces(String str){
        return str.codePoints().filter(c -> c == ' ').count();
    }
    public static long count(String str, char toCount){
        return str.codePoints().filter(c -> c == toCount).count();
    }
    
    public static void editTextArea(TextArea area, String newText){
        List<diff_match_patch.Diff> diffs = new diff_match_patch().diff_main(area.getText(), newText);
        
        int index = 0;
        for(diff_match_patch.Diff diff : diffs){
            if(diff.operation == diff_match_patch.Operation.INSERT){
                area.insertText(Math.min(index, area.getText().length()), diff.text);
            }else if(diff.operation == diff_match_patch.Operation.DELETE){
                area.deleteText(Math.min(index, area.getText().length()), Math.min(index + diff.text.length(), area.getText().length()));
            }
            index += diff.text.length();
        }
    }
    
    public static String removeBeforeLastOccurrence(String string, String match){
        if(match.isEmpty()) return string;
        int index = string.lastIndexOf(match);
        
        if(index == -1) return string;
        if(index < string.length()) return string.substring(index + match.length());
        
        return "";
    }
    
    public static String removeAfterLastOccurrence(String string, String match){
        if(match.isEmpty()) return string;
        int index = string.lastIndexOf(match);
        
        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);
        
        return "";
    }
    
    public static String removeAfterLastOccurrenceIgnoringCase(String string, String match){
        if(match.isEmpty()) return string;
        int index = string.toLowerCase().lastIndexOf(match.toLowerCase());
        
        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);
        
        return "";
    }
    public static String removeAfterLastOccurrenceIgnoringCase(String string, String[] matches){
        if(matches.length == 0) return string;
        
        HashMap<Integer, String> indices = new HashMap<>();
        for(String str : matches){
            int index = string.toLowerCase().lastIndexOf(str.toLowerCase());
            if(index < string.length() && index != -1){
                indices.put(index, str);
            }
        }
        
        return indices.entrySet()
                .stream()
                .max(Comparator.comparingInt(Entry::getKey))
                .map(optionalEntry -> string.substring(0, optionalEntry.getKey()))
                .orElse(string);
    }
    
    public static String removeAfter(String string, String rejex){
        if(rejex.isEmpty()) return "";
        int index = string.indexOf(rejex);
        
        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);
        
        return "";
    }
    
    
    public static List<Charset> getAvailableCharsets(){
        ArrayList<Charset> charsets = new ArrayList<>(Arrays.asList(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1, StandardCharsets.US_ASCII, StandardCharsets.UTF_16LE, StandardCharsets.UTF_16, StandardCharsets.UTF_16BE));
        if(!charsets.contains(Charset.defaultCharset())) charsets.add(0, Charset.defaultCharset());
        return charsets;
    }
    
    
    public static String[] cleanArray(String[] array){
        return Arrays.stream(array).filter(x -> !x.isBlank()).toArray(String[]::new);
    }
    
    public static boolean getAlwaysBoolean(String text){
        return "true".equalsIgnoreCase(text);
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

    public static boolean endsIn(final String[] array, String v, boolean kase){
        if(!kase) v = v.toLowerCase();
        for(final String e : array){
            if(kase){
                if(e != null && e.endsWith(v)) return true;
            }else{
                if(e != null && e.toLowerCase().endsWith(v)) return true;
            }
            
        }
        return false;
    }
    public static boolean contains(final String[] array, final String v, boolean kase){
        for(final String e : array){
            if(kase){
                if(e != null && e.equals(v)) return true;
            }else{
                if(e != null && e.equalsIgnoreCase(v)) return true;
            }
        }
        return false;
    }
    
    public static boolean startsIn(String[] array, String v, boolean kase){
        if(!kase) v = v.toLowerCase();
        for(final String e : array){
            if(kase){
                if(e != null && e.startsWith(v)) return true;
            }else{
                if(e != null && e.toLowerCase().startsWith(v)) return true;
            }
            
        }
        return false;
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
        InputContext is = InputContext.getInstance();
        return is.getLocale() != null && is.getLocale().getLanguage().equals("fr");
    }
    
    public static char getCsvSeparator(){
        if(TR.tr("chars.csvSeparator").charAt(0) == ';') return ';';
        return ',';
    }
    
}
