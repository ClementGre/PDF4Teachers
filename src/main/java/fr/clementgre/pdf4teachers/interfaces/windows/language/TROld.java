package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Pattern;

public class TROld{
    
    private static HashMap<String, String> defaultsTranslations = new HashMap<>();
    private static HashMap<String, String> translations = new HashMap<>();
    
    // Just a function to ask TranslationFilesGenerator to add a translation line for the text
    public static String ctO(String text){
        return text;
    }
    
    // Translate
    public static String trO(String text){
        
        if(translations.size() >= 1){
            String translated = translations.get(text);
            if(translated != null){
                return translated;
            }
        }
        
        if(Main.settings.language.getValue().equals("fr-fr")) return text;
        
        if(defaultsTranslations.size() >= 1){
            String translated = defaultsTranslations.get(text);
            if(translated != null){
                return translated;
            }
        }
        
        return text;
    }

    /*public static void setup(){
        TR.loadTranslationFile("en-us", true);
    }

    public static void updateTranslation(){
        TR.loadTranslationFile(Main.settings.language.getValue(), false);
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

        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader inputStream = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStream);

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
        inputStream.close();
        fileInputStream.close();
        return i >= 1;
    }*/
    
    // Return numbers :
    // 1 : total translations
    // 2 : translated translations
    public static int[] getTranslationFileStats(File file){
        
        try{
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStream = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStream);
            
            int total = 0;
            int translated = 0;
            
            String line;
            while((line = reader.readLine()) != null){
                
                if(!line.isBlank()){
                    if(line.startsWith("#")) continue;
                    
                    String key = line.split(Pattern.quote("="))[0];
                    String value = StringUtils.removeBeforeNotEscaped(line, "=");
                    
                    if(key != null){
                        if(!key.isBlank()){
                            total++;
                            if(!value.isBlank()){
                                translated++;
                            }
                        }
                    }
                    
                }
            }
            reader.close();
            inputStream.close();
            fileInputStream.close();
            
            return new int[]{total, translated};
            
        }catch(IOException e){
            e.printStackTrace();
        }
        return new int[]{0, 0};
        
    }
}
