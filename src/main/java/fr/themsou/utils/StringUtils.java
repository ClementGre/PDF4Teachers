package fr.themsou.utils;

public class StringUtils {

    public static String removeBefore(String string, char rejex){

        int index = string.indexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(index + 1);
        return "";
    }
    public static String removeBeforeLastRejex(String string, String rejex){

        int index = string.lastIndexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()){
            return string.substring(index + rejex.length());
        }
        return "";
    }
    public static String removeAfterLastRejex(String string, String rejex){

        int index = string.lastIndexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()){
            return string.substring(0, index);
        }
        return "";
    }
}
