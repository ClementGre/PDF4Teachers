package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class TranslationsManager {

    public static Locale locale;
    public static ResourceBundle bundle;

    public static Locale ENLocale;
    public static ResourceBundle ENBundle;

    private static HashMap<StringProperty, TranslationParams> translationsBindings = new HashMap<>();

    public static class TranslationParams{
        public TranslationParams(String key, String[] args) {
            this.key = key;
            this.args = args;
        }
        public TranslationParams(String key){
            this.key = key;
        }
        public String key;
        public String[] args;
    }

    public static void setup(){
        locale = new Locale(getSettingsLocaleLanguage(), getSettingsLocaleCountry());
        bundle = getBundleByLocale(locale);

        ENLocale = new Locale("en", "us");
        ENBundle = getBundleByLocale(ENLocale);

        Main.settings.language.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateLocale();
        });
    }

    public static File getLocaleFile(Locale locale){
        return new File(Main.dataFolder + "translations" + File.separator + "strings_" + getLocaleString() + ".properties");
    }

    public static ResourceBundle getBundleByLocale(Locale locale){
        if(Main.TRANSLATIONS_IN_CODE){
            return ResourceBundle.getBundle("strings", locale);
        }else{
            try{
                File file = getLocaleFile(locale);
                URL[] urls = new URL[]{file.toURI().toURL()};
                ClassLoader loader = new URLClassLoader(urls);
                return ResourceBundle.getBundle("myResource", Locale.getDefault(), loader);
            }catch(MalformedURLException e) {
                e.printStackTrace();
                return ResourceBundle.getBundle("strings", ENLocale);
            }
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
    public static String getLocaleString(){
        return locale.getLanguage().toLowerCase() + "_" + locale.getCountry().toLowerCase();
    }

    public static void updateLocale(){
        locale = new Locale(getSettingsLocaleLanguage(), getSettingsLocaleCountry());
        bundle = ResourceBundle.getBundle("strings", locale);

        for(Map.Entry<StringProperty, TranslationParams> translationBinding : translationsBindings.entrySet()){
            if(translationBinding.getValue().args == null){
                translationBinding.getKey().set(tr(translationBinding.getValue().key));
            }else{
                translationBinding.getKey().set(tr(translationBinding.getValue().key, translationBinding.getValue().args));
            }
        }

    }

    // return translation with EN Bundle
    public static String trEN(String key){
        if(!ENBundle.containsKey(key)) return key;

        return ENBundle.getString(key);
    }
    public static String trEN(String key, String... args){
        if(!ENBundle.containsKey(key)) return key;

        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(ENLocale);

        formatter.applyPattern(ENBundle.getString(key));
        return formatter.format(args);
    }

    // return translation
    public static String tr(String key){
        if(!bundle.containsKey(key)) return trEN(key);
        return bundle.getString(key);
    }
    public static String tr(String key, String... args){
        if(!bundle.containsKey(key)) return trEN(key, args);

        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(locale);

        formatter.applyPattern(bundle.getString(key));
        return formatter.format(args);
    }


    // Update value and setup binding for locale changes
    public static void trA(StringProperty binding, String key){
        translationsBindings.put(binding, new TranslationParams(key));
        binding.set(tr(key));
    }
    public static void trA(StringProperty binding, String key, String... args){
        translationsBindings.put(binding, new TranslationParams(key, args));
        binding.set(tr(key, args));
    }

    // Get value and setup binding for locale changes
    public static String trB(StringProperty binding, String key, String... args){
        translationsBindings.put(binding, new TranslationParams(key, args));
        return tr(key, args);
    }
    public static String trB(StringProperty binding, String key){
        translationsBindings.put(binding, new TranslationParams(key));
        return tr(key);
    }


}
