package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class TR {

    private static HashMap<String, String> defaultsTranslations = new HashMap<>();
    private static HashMap<String, String> translations = new HashMap<>();

    public static String tr(String text){

        if(translations.size() >= 1){
            String translated = translations.get(text);
            if(translated != null){
                return translated;
            }
        }

        if(Main.settings.getLanguage().equals("fr-fr")) return text;

        if(defaultsTranslations.size() >= 1){
            String translated = defaultsTranslations.get(text);
            if(translated != null){
                return translated;
            }
        }

        return text;
    }

    public static void setup(){
        TR.loadTranslationFile("en-us", true);
    }

    public static void updateTranslation(){
        TR.loadTranslationFile(Main.settings.getLanguage(), false);
    }

    public static boolean loadTranslationFile(String fileName, boolean defaultTranslation){

        if(defaultTranslation) defaultsTranslations = new HashMap<>();
        else translations = new HashMap<>();
        File file = new File(Main.dataFolder + "translations" + File.separator + fileName + ".txt");

        if(file.exists()){
            try{
                return loadFileTranslationsData(file, defaultTranslation);
            }catch(IOException e){ e.printStackTrace(); }
        }
        return false;
    }

    private static boolean loadFileTranslationsData(File file, boolean defaultTranslations) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

        String line; int i = 0;
        while((line = reader.readLine()) != null){

            if(!line.isBlank()){
                if(line.startsWith("#")) continue;

                String key = line.split(Pattern.quote("="))[0];
                String value = StringUtils.removeBeforeNotEscaped(line, "=");

                if(key != null){
                    if(!key.isBlank() && !value.isBlank()){
                        if(defaultTranslations){
                            defaultsTranslations.put(key.replaceAll(Pattern.quote("\\n"), "\n"), value.replaceAll(Pattern.quote("\\n"), "\n"));
                        }else{
                            translations.put(key.replaceAll(Pattern.quote("\\n"), "\n"), value.replaceAll(Pattern.quote("\\n"), "\n"));
                        }
                        i++;
                    }
                }

            }
        }
        reader.close();
        return i >= 1;
    }

}
