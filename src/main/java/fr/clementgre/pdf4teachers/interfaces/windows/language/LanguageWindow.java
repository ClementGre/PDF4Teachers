package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
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
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LanguageWindow extends Stage{

    HashMap<String, ImageView> languagesComponents = new HashMap<>();
    private static HashMap<String, Object> languages = getLanguagesDefaultConfig();

    CallBackArg<String> callBack;
    public LanguageWindow(CallBackArg<String> callBack){
        this.callBack = callBack;

        new LanguagesUpdater().update((hasDownloadedLanguage) -> {
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

            if(Main.settings.language.getValue().isEmpty()) Main.settings.language.setValue("en-us");

            setupLanguages();
            setupPanel(root);

            show();
            if(Main.window != null){
                Main.window.centerWindowIntoMe(this);
            }
        }, false, false);
    }

    public static String getLanguageFromComputerLanguage(){
        String language = System.getProperty("user.language").toLowerCase();
        String country = System.getProperty("user.country").toLowerCase();

        if(language.equals("fr")){
            return "fr-fr";
        }else if(language.equals("en")){
            return "en-us";
        }else if(language.equals("it")){
            return "it-it";
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
                        image = ImageUtils.buildImage(new FileInputStream(Main.dataFolder + "translations" + File.separator + StringUtils.removeAfterLastRejex(file.getName(), ".txt") + ".png"), 88, 50);
                    }
                    languagesComponents.put(StringUtils.removeAfterLastRejex(file.getName(), ".txt"), image);
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

        for(Map.Entry<String, ImageView> language : this.languagesComponents.entrySet()){
            HBox box = new HBox();
            box.setStyle("-fx-padding: -5;");

            Label label = new Label(getLanguageName(language.getKey()));
            label.setPrefHeight(50);
            HBox.setMargin(label, new Insets(0, 5, 0, 10));

            Label shortName = new Label(language.getKey());
            shortName.setPrefHeight(50);
            shortName.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");
            HBox.setMargin(shortName, new Insets(0, 3, 0, 0));

            Label version = new Label("v" + getLanguageVersion(language.getKey()));
            version.setPrefHeight(50);
            version.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");

            box.getChildren().addAll(language.getValue(), label, shortName, version);
            languages.getItems().add(box);
            if(Main.settings.language.getValue().equals(language.getKey())) languages.getSelectionModel().select(box);
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
            callBack.call(((Label) languages.getSelectionModel().getSelectedItem().getChildren().get(2)).getText());
        });
        newTrans.setOnAction((ActionEvent event) -> {

            Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Télécharger les fichier de traduction"));

            alert.setHeaderText(TR.tr("Télécharger un fichier de traduction pour traduire la langue d'origine de l'application (Français) en une autre langue." +
                    "\nVous enregistrerez :\n- Un fichier .txt pour les traductions de l'interface de PDF4Teachers\n- Un fichier .odt pour la traduction de la documentation"));

            alert.setContentText(TR.tr("Vous pourrez ensuite placer ces fichiers dans <AppData>/Roaming/PDF4Teachers/translations/ sous Windows ou dans <Dossier d'utilisateur>/.PDF4Teachers/translations/ sous OSX et Linux, pour voir la traduction dans la liste des langues. Vous pouvez aussi ajouter un drapeau en .png. Tous les fichiers doivent avoir le même nom (Sans compter l'extension).\n" +
                    "Vous pouvez aussi nous envoyer le fichier pour que nous puissions intégrer ce langage à l'application."));

            ButtonType originFile = new ButtonType(TR.tr("Enregistrer les fichiers"), ButtonBar.ButtonData.YES);
            ButtonType englishFile = new ButtonType(TR.tr("Enregistrer les fichiers déjà traduits en Anglais"), ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(originFile, englishFile, cancelButton);

            Optional<ButtonType> option = alert.showAndWait();

            String name;
            if(option.get() == originFile) name = "fr-fr";
            else if(option.get() == englishFile) name = "rn-us";
            else return;


            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("Sélectionner un dossier"));
            chooser.setInitialDirectory(( new File(MainWindow.userData.lastOpenDir).exists() ?  new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home"))));

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

    public static String getLanguageName(String shortName){
        HashMap<String, Object> languages = getLanguagesConfig();
        for(Map.Entry<String, Object> language : languages.entrySet()){
            if(shortName.equals(language.getKey())){
                HashMap<String, Object> data = (HashMap<String, Object>) language.getValue();
                return (String) data.get("name");
            }
        }
        return shortName;
    }
    public static int getLanguageVersion(String shortName){
        HashMap<String, Object> languages = getLanguagesConfig();
        for(Map.Entry<String, Object> language : languages.entrySet()){
            if(shortName.equals(language.getKey())){
                HashMap<String, Object> data = (HashMap<String, Object>) language.getValue();
                return (int) data.get("version");
            }
        }
        return 0;
    }
    public static void setup(){
        if(Main.settings.getSettingsVersion().equals("1.2.0") || Main.settings.getSettingsVersion().startsWith("1.1") || Main.settings.getSettingsVersion().startsWith("1.0")){
            for (File file : new File(Main.dataFolder + "translations").listFiles()) file.delete();
        }
        LanguageWindow.copyFiles(true); // test : force always
        LanguageWindow.copyFiles(!Main.settings.getSettingsVersion().equals(Main.VERSION));
        if(Main.settings.language.getValue().equals("Français France (Defaut)")){
            Main.settings.language.setValue("fr-fr");
        }else if(Main.settings.language.getValue().equals("English US")){
            Main.settings.language.setValue("en-us");
        }
        TR.setup();
    }
    public static void copyFiles(boolean force){
        try{
            File translationsDir = new File(Main.dataFolder + "translations" + File.separator);
            translationsDir.mkdirs();

            for(String name : LanguageWindow.getLanguagesDefaultConfig().keySet()){
                copyFile(name + ".txt", force);
                copyFile(name + ".pdf", force);
                copyFile(name + ".png", force);
                copyFile(name + ".odt", force);
            }
        }catch(IOException e){ e.printStackTrace(); }
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
            return new File(Main.dataFolder + "translations" + File.separator + "en-us.pdf");
        }
        return doc;

    }
    public static HashMap<String, Object> getLanguagesConfig(){
        return languages;
    }
    public static void loadLanguagesConfig(HashMap<String, Object> data){
        for(Map.Entry<String, Object> language : LanguageWindow.getLanguagesDefaultConfig().entrySet()){
            if(!data.containsKey(language.getKey())) data.put(language.getKey(), language.getValue());
        }
        languages = data;
    }
    public static HashMap<String, Object> getLanguagesDefaultConfig(){
        HashMap<String, Object> data = new HashMap<>();

        Config.set(data, "fr-fr.version", 0);
        Config.set(data, "fr-fr.name", "Français France");

        Config.set(data, "en-us.version", 0);
        Config.set(data, "en-us.name", "English US");

        Config.set(data, "it-it.version", 0);
        Config.set(data, "it-it.name", "Italiano");

        return data;
    }
    public static void addLanguageToConfig(String name, String displayName, int version){
        Config.set(languages, name + ".version", version);
        Config.set(languages, name + ".name", displayName);
    }

}
