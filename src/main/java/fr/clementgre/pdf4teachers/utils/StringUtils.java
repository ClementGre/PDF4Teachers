/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import javafx.scene.control.TextArea;
import name.fraser.neil.plaintext.diff_match_patch;

import java.awt.im.InputContext;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class StringUtils {
    
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
        StringBuffer result = new StringBuffer();
        
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
        
        Optional<Map.Entry<Integer, String>> first = indices.entrySet().stream().max(Comparator.comparingInt(Entry::getKey));
        if(first.isPresent()){
            return string.substring(0, first.get().getKey());
        }
        return string;
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
    
    public static Boolean getBoolean(String text){
        if("true".equalsIgnoreCase(text)){
            return true;
        }else if("false".equalsIgnoreCase(text)){
            return false;
        }else{
            return null;
        }
    }
    
    public static <T> boolean contains(final T[] array, final T v){
        if(v == null){
            for(final T e : array)
                if(e == null)
                    return true;
        }else{
            for(final T e : array)
                if(e == v || v.equals(e))
                    return true;
        }
        
        return false;
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
                if(e != null && e.endsWith(v)) return true;
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
    
    public static String replaceSymbolsToDigitsIfFrenchLayout(String text){
        if(!isAzertyLayout()) return text;
        
        return text.replace("&", "1").replace("é", "2").replace("\"", "3").replace("'", "4").replace("(", "5")
                .replace("-", "6").replace("è", "7").replace("_", "8").replace("ç", "9").replace("à", "0");
    }
    
    public static boolean isAzertyLayout(){
        InputContext is = InputContext.getInstance();
        return is.getLocale() != null && is.getLocale().getLanguage().equals("fr");
    }
}
