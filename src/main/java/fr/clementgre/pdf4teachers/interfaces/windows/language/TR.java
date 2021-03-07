package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TR {

    public static Locale locale;
    public static ResourceBundle bundle;

    public static Locale ENLocale;
    public static ResourceBundle ENBundle;

    /*private static HashMap<StringProperty, TranslationParams> translationsBindings = new HashMap<>();

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
    }*/

    public static void setup(){

        ENLocale = new Locale("en", "us");
        ENBundle = getBundleByLocale(ENLocale);

        locale = new Locale(getSettingsLocaleLanguage(), getSettingsLocaleCountry());
        bundle = getBundleByLocale(locale);

        Main.settings.language.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateLocale();
        });
    }

    public static File getLocaleFile(Locale locale){
        return new File(Main.dataFolder + "translations" + File.separator + "strings_" + getLocaleString(locale) + ".properties");
    }

    public static ResourceBundle getBundleByLocale(Locale locale){
        if(Main.TRANSLATIONS_IN_CODE){
            return ResourceBundle.getBundle("translations/strings", locale);
        }else{
            try{
                File file = getLocaleFile(locale);
                URL[] urls = new URL[]{file.getParentFile().toURI().toURL()};
                ClassLoader loader = new URLClassLoader(urls);
                return ResourceBundle.getBundle("strings", locale, loader);
            }catch(Exception e) {
                e.printStackTrace();
                return ResourceBundle.getBundle("translations/strings", ENLocale);
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
    public static String getLocaleString(Locale locale){
        return locale.getLanguage().toLowerCase() + "_" + locale.getCountry().toLowerCase();
    }

    public static void updateLocale(){
        //ResourceBundle oldBundle = bundle;
        locale = new Locale(getSettingsLocaleLanguage(), getSettingsLocaleCountry());
        bundle = getBundleByLocale(locale);

        /*for(Map.Entry<StringProperty, TranslationParams> translationBinding : translationsBindings.entrySet()){
            if(translationBinding.getValue().args == null){
                if(translationBinding.getKey().get().equals(tr(translationBinding.getValue().key, oldBundle, true)))
                    translationBinding.getKey().set(tr(translationBinding.getValue().key));
            }else{
                if(translationBinding.getKey().get().equals(tr(translationBinding.getValue().key, oldBundle, true, translationBinding.getValue().args)))
                    translationBinding.getKey().set(tr(translationBinding.getValue().key, translationBinding.getValue().args));
            }
        }*/

    }

    //
    public static String tr(String key, ResourceBundle bundle, boolean trEn){
        if(!bundle.containsKey(key) || bundle.getString(key).isBlank()){
            if(trEn) return tr(key, ENBundle, false);
            else return key;
        }
        return bundle.getString(key);
    }
    public static String tr(String key, ResourceBundle bundle, boolean trEn, String... args){
        if(!bundle.containsKey(key) || bundle.getString(key).isBlank()){
            if(!bundle.containsKey(key) || bundle.getString(key).isBlank()){
                if(trEn) return tr(key, ENBundle, false, args);
                else return key + " {" + String.join(", ", args) + "}";
            }
        }
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(locale);

        formatter.applyPattern(bundle.getString(key));
        return formatter.format(args);
    }

    // return translation
    public static String tr(String key){
        return tr(key, bundle, true);
    }
    public static String tr(String key, String... args){
        return tr(key, bundle, true, args);
    }

    // Update value and setup binding for locale changes
    /*public static void trB(StringProperty binding, String key){
        translationsBindings.put(binding, new TranslationParams(key));
        binding.set(tr(key));
    }
    public static void trB(StringProperty binding, String key, String... args){
        translationsBindings.put(binding, new TranslationParams(key, args));
        binding.set(tr(key, args));
    }

    // Get value and setup binding for locale changes
    public static String trG(StringProperty binding, String key, String... args){
        translationsBindings.put(binding, new TranslationParams(key, args));
        return tr(key, args);
    }
    public static String trG(StringProperty binding, String key){
        translationsBindings.put(binding, new TranslationParams(key));
        return tr(key);
    }*/


}
