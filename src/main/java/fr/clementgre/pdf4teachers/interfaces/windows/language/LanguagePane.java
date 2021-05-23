package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LanguagePane extends HBox{
    
    private String shortName;
    private String name;
    private int perMilleCompleted = -1;
    private int version;
    private ImageView image = null;
    
    public LanguagePane(File txtFile){
        shortName = txtFile.getName().replace(".properties", "");
        name = TR.getLanguageName(shortName);
        version = TR.getLanguageVersion(shortName);
        
        if(!shortName.equals("fr_fr")){
            int[] stats = TROld.getTranslationFileStats(txtFile);
            if(stats[0] != 0){
                perMilleCompleted = (int) (stats[1] / ((double) stats[0]) * 1000d);
            }
        }
        
        if(getFile(".png").exists()){
            try{
                image = ImageUtils.buildImage(new FileInputStream(getPath(".png")), 88, 50);
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
        
        composePane();
    }
    
    private void composePane(){
        setStyle("-fx-padding: -5;");
    
        Label name = new Label(getName());
        if(getPerMilleCompleted() != -1 && getPerMilleCompleted() != 1000)
            name.setText(name.getText() + " (" + TR.tr("language.chooseLanguageWindow.translationPercentageInfo", String.valueOf(getPerMilleCompleted() / 10d)) + ")");
    
        name.setPrefHeight(50);
        HBox.setMargin(name, new Insets(0, 5, 0, 10));
    
        Label shortName = new Label(getShortName());
        shortName.setPrefHeight(50);
        shortName.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");
        HBox.setMargin(shortName, new Insets(0, 3, 0, 0));
    
        Label version = new Label("v" + getVersion());
        version.setPrefHeight(50);
        version.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");
    
        if(getImage() != null){
            getChildren().addAll(getImage(), name, shortName, version);
        }else{
            Region spacer = new Region();
            spacer.setPrefWidth(88);
            getChildren().addAll(spacer, name, shortName, version);
        }
    }
    
    public File getFile(String extension){
        return new File(getPath(extension));
    }
    
    public String getPath(String extension){
        return Main.dataFolder + "translations" + File.separator + shortName + extension;
    }
    
    public String getShortName(){
        return shortName;
    }
    
    public String getName(){
        return name;
    }
    
    public int getPerMilleCompleted(){
        return perMilleCompleted;
    }
    
    public int getVersion(){
        return version;
    }
    
    public ImageView getImage(){
        return image;
    }
    
}
