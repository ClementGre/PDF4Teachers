package fr.themsou.utils;

public class StringUtils {

    public static String removeBefore(String string, String rejex){

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

            if(!string.startsWith("\\", index-1)){
                if(index < string.length()) return string.substring(index + rejex.length());
                return "";
            }else{
                fromIndex = index + 1;
            }

        }
    }
    public static String removeBeforeLastRejex(String string, String rejex){

        int index = string.lastIndexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(index + rejex.length());

        return "";
    }
    public static String removeAfterLastRejex(String string, String rejex){

        int index = string.lastIndexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }
    public static String removeAfter(String string, String rejex){

        int index = string.indexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }


}
