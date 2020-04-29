package fr.themsou.windows;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.utils.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LanguageWindow extends Stage{

    HashMap<String, ImageView> languages = new HashMap<>();

    public static final String[] LANGUAGES_NAMES = new String[]{"English"};


    CallBack<String> callBack;
    public LanguageWindow(CallBack<String> callBack){
        this.callBack = callBack;

        VBox root = new VBox();
        Scene scene = new Scene(root, 545, Main.SCREEN_BOUNDS.getHeight()-100 >= 675 ? 675 : Main.SCREEN_BOUNDS.getHeight()-100);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setWidth(545);
        setHeight(720);
        setTitle(TR.tr("PDF4Teachers - Langage"));
        setScene(scene);
        setOnCloseRequest(event -> {
            callBack.call("");
        });
        new JMetro(root, Style.LIGHT);

        if(Main.settings.getLanguage().isEmpty()) Main.settings.setLanguage("English");
        
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

        Text info = new Text(TR.tr("Choisissez votre langage"));

        ListView<HBox> languages = new ListView<>();
        languages.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        for(Map.Entry<String, ImageView> language : this.languages.entrySet()){
            HBox box = new HBox();
            box.setStyle("-fx-padding: -5;");
            Label label = new Label(language.getKey());
            label.setFont(new Font(14));
            label.setPrefHeight(50);
            HBox.setMargin(label, new Insets(0, 0, 0, 10));
            box.getChildren().addAll(language.getValue(), label);
            languages.getItems().add(box);
            if(Main.settings.getLanguage().equals(language.getKey())) languages.getSelectionModel().select(box);
        }

        VBox.setVgrow(languages, Priority.ALWAYS);

        HBox btns = new HBox();

        Button newTrans = new Button(TR.tr("Créer une nouvelle traduction"));
        Button accept = new Button(TR.tr("Valider"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btns.getChildren().addAll(newTrans, spacer, accept);

        root.getChildren().addAll(info, languages, btns);
        root.setStyle("-fx-padding: 10;");

        VBox.setMargin(info, new Insets(40, 10, 40, 0));
        HBox.setMargin(accept, new Insets(20, 0, 0, 5));
        HBox.setMargin(newTrans, new Insets(20, 0, 0, 5));

        accept.setOnAction((ActionEvent event) -> {
            TR.updateTranslation();
            close();
            callBack.call(((Label) languages.getSelectionModel().getSelectedItem().getChildren().get(1)).getText());
        });
        newTrans.setOnAction((ActionEvent event) -> {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            alert.setTitle(TR.tr("Télécharger le fichier de traduction"));
            alert.setHeaderText(TR.tr("Télécharger un fichier de traduction pour traduire la langue d'origine de l'application (Français) en une autre langue"));
            alert.setContentText(TR.tr("Vous pourez ensuite déposer ce fichier dans AppData/Roaming/PDF4Teachers/translations/ sous Windows ou Dossier_D'utilisateur/.PDF4Teachers/translations/ sous OSX et Linux pour voir la traduction dans la liste des langues." +
                    " Vous pouvez aussi nous envoyer le fichier pour que nous puissions intégrer ce langage à l'application."));
            ButtonType originFile = new ButtonType(TR.tr("Enregistrer le fichier"), ButtonBar.ButtonData.YES);
            ButtonType englishFile = new ButtonType(TR.tr("Enregistrer le fichier déjà traduit en Anglais"), ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(originFile, englishFile, cancelButton);
            Builders.secureAlert(alert);
            Optional<ButtonType> option = alert.showAndWait();

            File file;
            if(option.get() == originFile){
                file = new File(Main.dataFolder + "translations" + File.separator + "template.txt");
            }else if(option.get() == englishFile){
                file = new File(Main.dataFolder + "translations" + File.separator + "Anglais.txt");
            }else{
                return;
            }

            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("Selectionner un dossier"));
            chooser.setInitialDirectory((UserData.lastOpenDir.exists() ? UserData.lastOpenDir : new File(System.getProperty("user.home"))));

            File dir = chooser.showDialog(Main.window);
            if(dir != null) {
                try {
                    Files.copy(file.toPath(), new File(dir.getAbsoluteFile() + File.separator + file.getName()).toPath(), REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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
