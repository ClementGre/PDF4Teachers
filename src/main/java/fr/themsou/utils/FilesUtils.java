package fr.themsou.utils;

import java.io.File;
import java.util.regex.Pattern;

public class FilesUtils {

    public static long getSize(File f) {
        if(f.isFile()){
            return (f.length());
        }

        // DIR

        File[] files = f.listFiles();
        if(files == null) return 0;

        long x = 0L;
        for(int i = 0; i < files.length ; i++){
            x += getSize(files[i]);
        }
        return (x);
    }

    public static float convertOctetToMo(long octet){

        return (float) (octet / 1000) / 1000f;

    }

    public static String getExtension(String fileName){
        String[] splited = fileName.split(Pattern.quote("."));
        return splited[splited.length-1];
    }
}
