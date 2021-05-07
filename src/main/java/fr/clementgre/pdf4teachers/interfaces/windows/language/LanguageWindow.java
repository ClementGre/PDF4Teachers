package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class LanguageWindow extends Stage{
    
    ArrayList<Language> languagesComponents = new ArrayList<>();
    
    CallBackArg<String> callBack;
    
    public LanguageWindow(CallBackArg<String> callBack){
        this.callBack = callBack;
        
        new LanguagesUpdater().update((downloaded) -> {
            VBox root = new VBox();
            Scene scene = new Scene(root, 545, Main.SCREEN_BOUNDS.getHeight() - 100 >= 675 ? 675 : Main.SCREEN_BOUNDS.getHeight() - 100);
            
            initOwner(Main.window);
            initModality(Modality.WINDOW_MODAL);
            getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
            setWidth(545);
            setHeight(720);
            setTitle(TR.tr("language.chooseLanguageWindow.title"));
            setScene(scene);
            setOnCloseRequest(event -> {
                callBack.call("");
            });
            StyleManager.putStyle(scene, Style.DEFAULT);
            root.getStyleClass().add(JMetroStyleClass.BACKGROUND);
            
            if(Main.settings.language.getValue().isEmpty()) Main.settings.language.setValue("en_us");
            
            setupLanguages();
            setupPanel(root);
            
            show();
            if(Main.window != null){
                Main.window.centerWindowIntoMe(this);
            }
        }, false, false);
    }
    
    private static class Language{
        private String shortName;
        private String name;
        private int perMilleCompleted = -1;
        private int version;
        private ImageView image = null;
        
        public Language(File txtFile){
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
    
    public void setupLanguages(){
        
        try{
            File dir = new File(Main.dataFolder + "translations" + File.separator);
            
            for(File file : dir.listFiles()){
                if(FilesUtils.getExtension(file.getName()).equals("properties")){
                    languagesComponents.add(new Language(file));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    public void setupPanel(VBox root){
        
        Text info = new Text(TR.tr("language.chooseLanguageWindow.header"));
        
        ListView<HBox> languages = new ListView<>();
        languages.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        
        for(Language language : this.languagesComponents){
            HBox box = new HBox();
            box.setStyle("-fx-padding: -5;");
            
            Label label = new Label(language.getName());
            if(language.getPerMilleCompleted() != -1 && language.getPerMilleCompleted() != 1000)
                label.setText(label.getText() + " (" + TR.tr("language.chooseLanguageWindow.translationPercentageInfo", String.valueOf(language.getPerMilleCompleted() / 10d)) + ")");
            
            label.setPrefHeight(50);
            HBox.setMargin(label, new Insets(0, 5, 0, 10));
            
            Label shortName = new Label(language.getShortName());
            shortName.setPrefHeight(50);
            shortName.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");
            HBox.setMargin(shortName, new Insets(0, 3, 0, 0));
            
            Label version = new Label("v" + language.getVersion());
            version.setPrefHeight(50);
            version.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");
            
            if(language.getImage() != null){
                box.getChildren().addAll(language.getImage(), label, shortName, version);
            }else{
                Region spacer = new Region();
                spacer.setPrefWidth(88);
                box.getChildren().addAll(spacer, label, shortName, version);
            }
            
            languages.getItems().add(box);
            if(Main.settings.language.getValue().equals(language.getShortName()))
                languages.getSelectionModel().select(box);
        }
        
        VBox.setVgrow(languages, Priority.ALWAYS);
        
        HBox btns = new HBox();
        
        Button newTrans = new Button(TR.tr("language.chooseLanguageWindow.contributeButton"));
        Button accept = new Button(TR.tr("actions.apply"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        btns.getChildren().addAll(newTrans, spacer, accept);
        
        root.getChildren().addAll(info, languages, btns);
        root.setStyle("-fx-padding: 10;");
        
        VBox.setMargin(info, new Insets(40, 10, 40, 0));
        HBox.setMargin(accept, new Insets(20, 0, 0, 5));
        HBox.setMargin(newTrans, new Insets(20, 0, 0, 5));
        
        accept.setOnAction((ActionEvent event) -> {
            TR.updateLocale();
            close();
            callBack.call(((Label) languages.getSelectionModel().getSelectedItem().getChildren().get(2)).getText());
        });
        newTrans.setOnAction((ActionEvent event) -> Main.hostServices.showDocument("https://pdf4teachers.org/Contribute/"));
        
    }
    
}
