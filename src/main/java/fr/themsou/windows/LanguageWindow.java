package fr.themsou.windows;

import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.FilesUtils;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LanguageWindow extends Stage{

    HashMap<String, ImageView> languages = new HashMap<>();

    public static final String[] LANGUAGES_NAMES = new String[]{};

    public LanguageWindow(){

        VBox root = new VBox();
        Scene scene = new Scene(root, 545, 720);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setWidth(545);
        setHeight(720);
        setTitle(TR.tr("PDF4Teachers - Langage"));
        setScene(scene);
        new JMetro(root, Style.LIGHT);

        if(Main.settings.getLanguage().isEmpty()) Main.settings.setLanguage("Français France (Defaut)");
        
        setupLanguages();
        setupPanel(root);

        show();
    }

    public void setupLanguages(){

        try{
            File dir = new File(Main.dataFolder + "translations" + File.separator);
            languages.put("Français France (Defaut)", Builders.buildImage(getClass().getResource("/translations/default.png")+"", 0, 50));

            for(File file : dir.listFiles()){
                if(FilesUtils.getExtension(file.getName()).equals("txt") && !file.getName().equals("template.txt")){
                    ImageView image = new ImageView();
                    if(new File(Main.dataFolder + "translations" + File.separator + StringUtils.removeAfterLastRejex(file.getName(), ".txt") + ".png").exists()) {
                        image = Builders.buildImage(new FileInputStream(new File(Main.dataFolder + "translations" + File.separator + StringUtils.removeAfterLastRejex(file.getName(), ".txt") + ".png")), 0, 50);
                    }
                    languages.put(StringUtils.removeAfterLastRejex(file.getName(), ".txt"), image);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void setupPanel(VBox root){

        Text info = new Text(TR.tr("Choisissez votre language"));

        ListView<HBox> languages = new ListView<>();
        languages.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        for(Map.Entry<String, ImageView> language : this.languages.entrySet()){
            HBox box = new HBox();
            box.setStyle("-fx-padding: -5;");
            System.out.println(language.getKey());
            Label label = new Label(language.getKey());
            label.setFont(new Font(14));
            label.setPrefHeight(50);
            HBox.setMargin(label, new Insets(0, 0, 0, 10));
            box.getChildren().addAll(language.getValue(), label);
            languages.getItems().add(box);
            if(Main.settings.getLanguage().equals(language.getKey())) languages.getSelectionModel().select(box);
        }

        HBox btns = new HBox();
        Button accept = new Button(TR.tr("Valider"));
        btns.getChildren().addAll(accept);
        btns.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(info, languages, btns);
        root.setStyle("-fx-padding: 10;");

        VBox.setMargin(info, new Insets(40, 10, 40, 0));
        HBox.setMargin(accept, new Insets(20, 0, 0, 5));

        accept.setOnAction((ActionEvent event) -> {
            Main.settings.setLanguage(((Label) languages.getSelectionModel().getSelectedItem().getChildren().get(1)).getText());
            close();
        });

    }

    public static void copyFiles(){

        try{
            File translationsDir = new File(Main.dataFolder + "translations" + File.separator);
            if(!translationsDir.exists()){
                translationsDir.mkdirs();

                InputStream res = LanguageWindow.class.getResourceAsStream("/translations/template.txt");
                File dest = new File(Main.dataFolder + "translations" + File.separator + "template.txt");
                Files.copy(res, dest.getAbsoluteFile().toPath(), REPLACE_EXISTING);

                res = LanguageWindow.class.getResourceAsStream("/translations/default.png");
                dest = new File(Main.dataFolder + "translations" + File.separator + "default.png");
                Files.copy(res, dest.getAbsoluteFile().toPath(), REPLACE_EXISTING);

                for(String languageName : LANGUAGES_NAMES){
                    res = LanguageWindow.class.getResourceAsStream("/translations/" + languageName + ".txt");
                    dest = new File(Main.dataFolder + "translations" + File.separator + languageName + ".txt");
                    Files.copy(res, dest.getAbsoluteFile().toPath(), REPLACE_EXISTING);

                    res = LanguageWindow.class.getResourceAsStream("/translations/" + languageName + ".png");
                    dest = new File(Main.dataFolder + "translations" + File.separator + languageName + ".png");
                    Files.copy(res, dest.getAbsoluteFile().toPath(), REPLACE_EXISTING);
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }

    }

}
