package fr.themsou.utils;

public class StringUtils {

    public static String removeBefore(String string, char rejex){

        int index = string.indexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(index + 1);
        return "";
    }
}
