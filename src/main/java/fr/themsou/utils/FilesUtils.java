package fr.themsou.utils;

import java.io.File;

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
}
