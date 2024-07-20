/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class UpdateWindow extends AlternativeWindow<VBox> {
    
    public static String version = "";
    public static String description = "";
    public static boolean newVersion;
    public static boolean newPre;
    public static boolean error;
    
    public static boolean checkVersion(){
        
        ////////// GET LAST RELEASE INCLUDING PRE //////////
        String parsedVersion = null;
        try{
            URL url = new URL("https://api.github.com/repos/clementgre/PDF4Teachers/tags");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            
            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(con.getInputStream());
            
            JsonToken token;
            while((token = jParser.nextToken()) != null){
                String key = jParser.currentName();
                
                if("name".equals(key)){
                    jParser.nextToken();
                    parsedVersion = jParser.getText();
                    break;
                }
            }
            jParser.close();
            
            if(parsedVersion.equals(Main.VERSION)){
                // Up to date with the last release
                return false;
            }
            
        }catch(IOException e){
            Log.eNotified(e);
            error = true;
            return false;
        }
        
        ////////// GET LAST RELEASE DETAILS ////////// (isn't up to date -> search details about the last version)
        String parsedDescription = null;
        try{
            URL url = new URL("https://api.github.com/repos/clementgre/PDF4Teachers/releases/tags/" + parsedVersion);
            
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            
            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(con.getInputStream());
            
            boolean parsedPre = false;
            
            JsonToken token;
            while((token = jParser.nextToken()) != null){
                String fieldname = jParser.currentName();
                
                if("prerelease".equals(fieldname)){
                    jParser.nextToken();
                    parsedPre = jParser.getBooleanValue();
                }else if("body".equals(fieldname)){
                    jParser.nextToken();
                    parsedDescription = jParser.getText();
                    break;
                }
            }
            jParser.close();
            
            if(parsedDescription == null){
                error = true;
                return false;
            }
            
            if(!parsedPre){
                UpdateWindow.version = parsedVersion;
                UpdateWindow.description = parsedDescription;
                UpdateWindow.newVersion = true;
                return true;
            }
            // Is pre, verify if the user has already the last non-pre release before propose it.
            // Else, we will propose the last non-pre release
            
        }catch(IOException e){
            Log.eNotified(e);
            error = true;
            return false;
        }
        
        ////////// GET LAST RELEASE WITHOUT PRE ////////// (isn't up to date && last is pre -> find the latest non-pre version)
        try{
            URL url = new URL("https://api.github.com/repos/clementgre/PDF4Teachers/releases/latest");
            
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            
            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(con.getInputStream());
            
            String parsedLatestVersion = null;
            String parsedLatestDescription = null;
            
            JsonToken token;
            while((token = jParser.nextToken()) != null){
                String fieldname = jParser.currentName();
                
                if("tag_name".equals(fieldname)){
                    jParser.nextToken();
                    parsedLatestVersion = jParser.getText();
                    
                }
                if("body".equals(fieldname)){
                    jParser.nextToken();
                    parsedLatestDescription = jParser.getText();
                    break;
                }
            }
            jParser.close();
            
            if(parsedLatestVersion == null){
                error = true;
                return false;
            }
            
            if(parsedLatestVersion.equals(Main.VERSION)){
                // Up to date with the latest release -> let's propose the pre release
                UpdateWindow.version = parsedVersion;
                UpdateWindow.description = parsedDescription;
                UpdateWindow.newPre = true;
            }else{
                // User don't have the latest release -> propose it before proposing the pre
                UpdateWindow.version = parsedLatestVersion;
                UpdateWindow.description = parsedLatestDescription;
                UpdateWindow.newVersion = true;
            }
            return true;
        }catch(IOException e){
            Log.eNotified(e);
            error = true;
            return false;
        }
    }
    
    public UpdateWindow(){
        super(new VBox(), StageWidth.NORMAL, TR.tr("updateWindow.title"), TR.tr("updateWindow.title"));
    }
    @Override
    public void setupSubClass(){
        Label info;
        
        if(newPre){
            setSubHeaderText(TR.tr("aboutWindow.version.update.preRelease") + "\n" + TR.tr("updateWindow.preReleaseInfo"));
            info = new Label(TR.tr("updateWindow.details.preRelease", Main.VERSION, UpdateWindow.version));
        }else{
            setSubHeaderText(TR.tr("aboutWindow.version.update.available"));
            info = new Label(TR.tr("updateWindow.details", Main.VERSION, UpdateWindow.version));
        }
        
        info.setWrapText(true);
        VBox.setMargin(info, new Insets(10, 0, 10, 0));
        
        VBox description = new VBox();
        generateDescription(description);
        
        root.getChildren().addAll(info, description);
        
        setupButtons();
    }
    
    public void setupButtons(){
        Button ignore = new Button(TR.tr("actions.ignore"));
        ignore.setOnAction((e) -> close());
        
        Button see = new Button(TR.tr("updateWindow.buttons.openDownloadPage"));
        see.setOnAction(t -> Main.hostServices.showDocument("https://pdf4teachers.org/Download/?v=" + UpdateWindow.version));
        
        String platform = "Linux";
        String extension = "deb";
        if(PlatformUtils.isWindows()){
            platform = "Windows";
            extension = "msi";
        }else if(PlatformUtils.isMac()){
            platform = "MacOSX";
            extension = "dmg";
            if(PlatformUtils.isMacAArch64()){
                platform = "MacOSX-Aarch64";
            }
        }
        String url = "https://github.com/ClementGre/PDF4Teachers/releases/download/" +
                UpdateWindow.version + "/PDF4Teachers-" + platform + "-" + UpdateWindow.version + "." + extension;
        
        Button maj = new Button(TR.tr("updateWindow.buttons.directDownload"));
        maj.setOnAction(t -> Main.hostServices.showDocument(url));
        
        setButtons(ignore, see, maj);
    }
    
    @Override
    public void afterShown(){
    
    }
    
    public void generateDescription(VBox root){
        final String currentLanguageAcronym = Main.settings.language.getValue().split("_")[0];
        
        final String[] languagesTexts = description.split(Pattern.quote("\r\n\r\n# "));
        String englishText = "";
        String langText = "";
        
        for(String languageText : languagesTexts){
            if(languageText.startsWith("# ")) languageText = languageText.replaceFirst(Pattern.quote("# "), "");
            final String langAcronym = languageText.substring(0, languageText.indexOf(' '));
            
            final String futureLanguageText = languageText.substring(languageText.indexOf("\r\n") + 2);
            if(langAcronym.equals(currentLanguageAcronym)){
                langText = futureLanguageText;
            }else if(langAcronym.equals("en")){
                englishText = futureLanguageText;
            }
        }
        
        if(langText.isEmpty()) langText = englishText;
        
        boolean first = true;
        for(String line : langText.split(Pattern.quote("## \uD83C\uDF10"))[0].split(Pattern.quote("\r\n"))){
            if(first && line.isBlank()) continue;
            
            if(line.startsWith("##")){
                Label label = new Label(line.replace("##", ""));
                label.setWrapText(true);
                if(first) label.setStyle("-fx-padding: 0; -fx-font-size: 16; -fx-font-weight: 700;");
                else label.setStyle("-fx-padding: 10 0 0 0; -fx-font-size: 16; -fx-font-weight: 700;");
                root.getChildren().add(label);
            }else if(line.isEmpty()){
                Region spacer = new Region();
                spacer.setPrefHeight(5);
                root.getChildren().add(spacer);
            }else{
                Label label = new Label(line.replace("##", ""));
                label.setWrapText(true);
                
                if(line.startsWith("- ")){
                    label.setStyle("-fx-padding: 0 0 0 30;");
                }else{
                    label.setStyle("-fx-padding: 0 0 0 15;");
                }
                
                root.getChildren().add(label);
            }
            first = false;
        }
    }
}
