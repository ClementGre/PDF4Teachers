package fr.themsou.windows;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.utils.*;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
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

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LanguageWindow extends Stage{

    HashMap<String, ImageView> languages = new HashMap<>();

    public static final String[] TO_COPY_FILES = new String[]{
            "Français France (Defaut).txt", "Français France (Defaut).png", "Français France (Defaut).pdf", "Français France (Defaut).odt",
            "English US.txt", "English US.png", "English US.pdf", "English US.odt",
            "English GB.png"
    };


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
        StyleManager.putStyle(root, Style.DEFAULT);

        if(Main.settings.getLanguage().isEmpty()) Main.settings.setLanguage("English US");
        
        setupLanguages();
        setupPanel(root);

        show();
    }

    public static String detectLanguage() {
        String country = System.getProperty("user.country").toLowerCase();
        String language = System.getProperty("user.language").toLowerCase();

        if(language.equals("fr")){
            return "Français France (Defaut)";
        }else if(language.equals("en")){
            return "English US";
        }

        return null;
    }

    public void setupLanguages(){

        try{
            File dir = new File(Main.dataFolder + "translations" + File.separator);

            for(File file : dir.listFiles()){
                if(FilesUtils.getExtension(file.getName()).equals("txt")){
                    ImageView image = new ImageView();
                    if(new File(Main.dataFolder + "translations" + File.separator + StringUtils.removeAfterLastRejex(file.getName(), ".txt") + ".png").exists()) {
                        image = Builders.buildImage(new FileInputStream(new File(Main.dataFolder + "translations" + File.separator + StringUtils.removeAfterLastRejex(file.getName(), ".txt") + ".png")), 88, 50);
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

            Alert alert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Télécharger les fichier de traduction"));

            alert.setHeaderText(TR.tr("Télécharger un fichier de traduction pour traduire la langue d'origine de l'application (Français) en une autre langue." +
                    "\nVous enregistrerez :\n- Un fichier .txt pour les traductions de l'interface de PDF4Teachers\n- Un fichier .odt pour la traduction de la documentation"));

            alert.setContentText(TR.tr("Vous pourrez ensuite placer ces fichiers dans <AppData>/Roaming/PDF4Teachers/translations/ sous Windows ou dans <Dossier d'utilisateur>/.PDF4Teachers/translations/ sous OSX et Linux, pour voir la traduction dans la liste des langues. Vous pouvez aussi ajouter un drapeau en .png. Tous les fichiers doivent avoir le même nom (Sans compter l'extension).\n" +
                    "Vous pouvez aussi nous envoyer le fichier pour que nous puissions intégrer ce langage à l'application."));

            ButtonType originFile = new ButtonType(TR.tr("Enregistrer les fichiers"), ButtonBar.ButtonData.YES);
            ButtonType englishFile = new ButtonType(TR.tr("Enregistrer les fichiers déjà traduits en Anglais"), ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(originFile, englishFile, cancelButton);

            Optional<ButtonType> option = alert.showAndWait();

            String name = "";
            if(option.get() == originFile){
                name = "Français France (Defaut)";
            }else if(option.get() == englishFile){
                name = "English US";
            }else{
                return;
            }

            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("Sélectionner un dossier"));
            chooser.setInitialDirectory((UserData.lastOpenDir.exists() ? UserData.lastOpenDir : new File(System.getProperty("user.home"))));

            File dir = chooser.showDialog(Main.window);
            if(dir != null) {
                try {
                    Files.copy(new File(Main.dataFolder + "translations" + File.separator + name + ".txt").toPath(), new File(dir.getAbsoluteFile() + File.separator + name + ".txt").toPath(), REPLACE_EXISTING);
                    Files.copy(new File(Main.dataFolder + "translations" + File.separator + name + ".odt").toPath(), new File(dir.getAbsoluteFile() + File.separator + name + ".odt").toPath(), REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    public static void copyFiles(boolean force){
        try{
            File translationsDir = new File(Main.dataFolder + "translations" + File.separator);
            translationsDir.mkdirs();

            for(String fileName : TO_COPY_FILES){
                File dest = new File(Main.dataFolder + "translations" + File.separator + fileName);
                if(!dest.exists() || force){
                    InputStream res = LanguageWindow.class.getResourceAsStream("/translations/" + fileName);
                    Files.copy(res, dest.getAbsoluteFile().toPath(), REPLACE_EXISTING);
                }
            }
        }catch(IOException e){ e.printStackTrace(); }
    }

    public static File getDocFile(){

        File doc = new File(Main.dataFolder + "translations" + File.separator + Main.settings.getLanguage() + ".pdf");
        if(!doc.exists()){
            return new File(Main.dataFolder + "translations" + File.separator + "Français France (Defaut).pdf");
        }
        return doc;

    }

}
