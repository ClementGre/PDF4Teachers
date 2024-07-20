/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TR {
    
    public static Locale locale;
    public static ResourceBundle bundle;
    
    public static Locale ENLocale;
    public static ResourceBundle ENBundle;
    private static HashMap<String, Object> languages = getLanguagesDefaultConfig();
    
    public static void setup(){
        
        // Translate translations from 1.2.0- naming system
        if(Main.settings.language.getValue().equals("Français France (Defaut)")){
            Main.settings.language.setValue("fr_fr");
            Main.settings.saveSettings();
        }else if(Main.settings.language.getValue().equals("English US")){
            Main.settings.language.setValue("en_us");
            Main.settings.saveSettings();
        }
        
        // Delete Old files
        if(Stream.of("1.2", "1.1", "1.0").anyMatch(s -> Main.settings.getSettingsVersionCode().startsWith(s))){
            for(File file : Objects.requireNonNull(new File(Main.dataFolder + "translations").listFiles()))
                file.delete();
        }
        // Copy files if version has changed
        copyFiles(Main.settings.hasVersionChanged() || Main.COPY_TRANSLATIONS_AT_START);
        
        // Load locales
        
        ENLocale = Locale.of("en", "us");
        ENBundle = getBundleByLocale(ENLocale);
        
        locale = Locale.of(getSettingsLocaleLanguage(), getSettingsLocaleCountry());
        bundle = getBundleByLocale(locale);
        
    }
    
    public static ResourceBundle getBundleByLocale(Locale locale){
        if(Main.TRANSLATIONS_IN_CODE){ // Load the locale from ressource
            return getBundleByLocaleInCode(locale);
        }
        try{ // Load the locale from user files
            Log.d("Loading locale " + locale.toString() + " from user files...");
            FileInputStream fis = new FileInputStream(getLocaleFile(locale));
            return new PropertyResourceBundle(fis);
        }catch(Exception e){
            Log.eNotified(e, "Unable to load translation in user files, trying to load from ressource...");
            return getBundleByLocaleInCode(locale);
        }
    }
    
    public static ResourceBundle getBundleByLocaleInCode(Locale locale){
        try{ // Load the locale from ressource
            Log.d("Loading locale " + locale.toString() + " from code...");
            return new PropertyResourceBundle(getCodeLocaleFile(locale));
        }catch(IOException ex){
            Log.eNotified(ex, "Unable to load translation in code");
            
            if(locale != ENLocale){ // Load the EN locale
                Log.e("Trying to load the English locale from user files...");
                return getBundleByLocale(ENLocale);
            }
            Log.e("Return empty ressourceBundle...");
            return new ResourceBundle() {
                @Override
                protected Object handleGetObject(String key){
                    return null;
                }
                @Override
                public Enumeration<String> getKeys(){
                    return Collections.emptyEnumeration();
                }
            };
        }
    }
    
    public static String getLanguageFromComputerLanguage(){
        String language = System.getProperty("user.language").toLowerCase();
        //String country = System.getProperty("user.country").toLowerCase();
        switch(language){
            case "fr" -> {return "fr_fr";}
            case "en" -> {return "en_us";}
            case "it" -> {return "it_it";}
            default -> {return null;}
        }
    }
    
    public static String getSettingsLocaleLanguage(){
        if(Main.settings.language.getValue().split("[-_]").length < 2) return "en";
        return Main.settings.language.getValue().split("[-_]")[0].toLowerCase();
    }
    
    public static String getSettingsLocaleCountry(){
        if(Main.settings.language.getValue().split("[-_]").length < 2) return "us";
        return Main.settings.language.getValue().split("[-_]")[1].toLowerCase();
    }
    
    public static File getLocaleFile(Locale locale){
        return new File(Main.dataFolder + "translations" + File.separator + getLocaleString(locale) + ".properties");
    }
    
    public static InputStream getCodeLocaleFile(Locale locale){
        return TR.class.getResourceAsStream("/translations/" + getLocaleString(locale) + ".properties");
    }
    
    public static String getLocaleString(Locale locale){
        return locale.getLanguage().toLowerCase() + "_" + locale.getCountry().toLowerCase();
    }
    
    
    
    public static void updateLocale(){
        locale = Locale.of(getSettingsLocaleLanguage(), getSettingsLocaleCountry());
        bundle = getBundleByLocale(locale);
    }
    
    // translate with arguments
    public static String tr(String key, ResourceBundle bundle, boolean trEn){
        if(!bundle.containsKey(key) || bundle.getString(key).isBlank()){
            if(trEn) return tr(key, ENBundle, false);
            return key;
        }
        return bundle.getString(key);
    }
    
    public static String tr(String key, ResourceBundle bundle, boolean trEn, String... args){
        if(!bundle.containsKey(key) || bundle.getString(key).isBlank()){
            if(!bundle.containsKey(key) || bundle.getString(key).isBlank()){
                if(trEn) return tr(key, ENBundle, false, args);
                return key + " {" + String.join(", ", args) + "}";
            }
        }
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(locale);
        
        formatter.applyPattern(bundle.getString(key).replace("'", "''"));
        return formatter.format(args);
    }
    
    // return translation
    public static boolean trBoolean(String key){
        return Boolean.parseBoolean(tr(key));
    }
    
    public static String tr(String key){
        return tr(key, bundle, true);
    }
    
    public static String tr(String key, String... args){
        return tr(key, bundle, true, args);
    }
    
    public static String tr(String key, int... args){
        return tr(key, bundle, true, Arrays.stream(args).mapToObj(String::valueOf).toArray(String[]::new));
    }
    
    public static String trO(String text){
        return "[" + text + "]";
    }
    
    public static String getColons(){
        return (TR.trBoolean("chars.doPutSpaceBeforeDoublePunctuation") ? " :" : ":");
    }
    
    //////////////////////////////////////////////////////////
    ////////////// Language config system ////////////////////
    //////////////////////////////////////////////////////////
    
    public static String getLanguageName(String shortName){
        return getLanguagesConfig().entrySet()
                .stream()
                .filter(language -> shortName.equals(language.getKey()))
                .map(language -> (HashMap<?, ?>) language.getValue())
                .findFirst()
                .map(data -> (String) data.get("name"))
                .orElse(shortName);
    }
    
    public static int getLanguageVersion(String shortName){
        return getLanguagesConfig().entrySet()
                .stream()
                .filter(language -> shortName.equals(language.getKey()))
                .map(language -> (HashMap<?, ?>) language.getValue())
                .findFirst()
                .map(data -> (int) data.get("version"))
                .orElse(0);
    }
    
    public static void copyFiles(boolean force){
        try{
            File translationsDir = new File(Main.dataFolder + "translations" + File.separator);
            translationsDir.mkdirs();
            
            for(String name : getLanguagesDefaultConfig().keySet()){
                copyFile(name + ".properties", force);
                copyFile(name + ".pdf", force);
                copyFile(name + ".png", force);
                copyFile(name + ".odt", force);
            }
        }catch(IOException e){
            Log.eNotified(e);
        }
    }
    
    private static void copyFile(String fileName, boolean force) throws IOException{
        if(LanguageWindow.class.getResource("/translations/" + fileName) == null) return;
        
        File dest = new File(Main.dataFolder + "translations" + File.separator + fileName);
        if(!dest.exists() || force){
            InputStream res = LanguageWindow.class.getResourceAsStream("/translations/" + fileName);
            Files.copy(res, dest.getAbsoluteFile().toPath(), REPLACE_EXISTING);
        }
    }
    
    public static File getDocFile(){
        File doc = new File(Main.dataFolder + "translations" + File.separator + Main.settings.language.getValue() + ".pdf");
        if(!doc.exists()){
            return new File(Main.dataFolder + "translations" + File.separator + "en_us.pdf");
        }
        return doc;
    }
    
    public static HashMap<String, Object> getLanguagesConfig(){
        return languages;
    }
    
    public static void loadLanguagesConfig(HashMap<String, Object> data){
        // if the version has changed, keep the default values
        if(Main.settings.hasVersionChanged()) return;
        
        // Add default languages if they were deleted
        for(Map.Entry<String, Object> language : getLanguagesDefaultConfig().entrySet()){
            if(!data.containsKey(language.getKey())) data.put(language.getKey(), language.getValue());
        }
        languages = data;
    }
    
    public static HashMap<String, Object> getLanguagesDefaultConfig(){
        HashMap<String, Object> data = new HashMap<>();
        
        Config.set(data, "fr_fr.version", 0);
        Config.set(data, "fr_fr.name", "Français France");
        
        Config.set(data, "en_us.version", 0);
        Config.set(data, "en_us.name", "English US");
        
        Config.set(data, "it_it.version", 0);
        Config.set(data, "it_it.name", "Italiano Italia");
        
        return data;
    }
    
    public static void addLanguageToConfig(String name, String displayName, int version){
        Config.set(languages, name + ".version", version);
        Config.set(languages, name + ".name", displayName);
    }
    
    // Return values :
    // [1] : total translations
    // [2] : translated translations
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
            Log.eNotified(e);
        }
        return new int[]{0, 0};
        
    }
}
