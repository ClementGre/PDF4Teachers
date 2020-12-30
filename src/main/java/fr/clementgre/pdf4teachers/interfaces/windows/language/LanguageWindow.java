package fr.clementgre.pdf4teachers.interfaces.windows.language;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LanguageWindow extends Stage{

    ArrayList<Language> languagesComponents = new ArrayList<>();
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

    private static class Language{
        private String shortName;
        private String name;
        private int perMilleCompleted = -1;
        private int version;
        private ImageView image = null;

        public Language(File txtFile) {
            shortName = StringUtils.removeAfterLastRegex(txtFile.getName(), ".txt");
            name = getLanguageName(shortName);
            version = getLanguageVersion(shortName);

            if(!shortName.equals("fr-fr")){
                int[] stats = TR.getTranslationFileStats(txtFile);
                if(stats[0] != 0){
                    perMilleCompleted = (int) (stats[1] / ((double) stats[0]) * 1000d);
                }
            }

            if(getFile(".png").exists()) {
                try{
                    image = ImageUtils.buildImage(new FileInputStream(getPath(".png")), 88, 50);
                }catch(FileNotFoundException e){ e.printStackTrace(); }
            }
        }

        public File getFile(String extension){
            return new File(getPath(extension));
        }
        public String getPath(String extension){
            return Main.dataFolder + "translations" + File.separator + shortName + extension;
        }

        public String getShortName() {
            return shortName;
        }
        public String getName() {
            return name;
        }
        public int getPerMilleCompleted() {
            return perMilleCompleted;
        }
        public int getVersion() {
            return version;
        }
        public ImageView getImage() {
            return image;
        }
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
                    languagesComponents.add(new Language(file));
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

        for(Language language : this.languagesComponents){
            HBox box = new HBox();
            box.setStyle("-fx-padding: -5;");

            Label label = new Label(language.getName());
            if(language.getPerMilleCompleted() != -1 && language.getPerMilleCompleted() != 1000){
                label.setText(label.getText() + " (" + TR.tr("Traduit à") + " " + (language.getPerMilleCompleted()/10d) + "%)");
            }

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
            if(Main.settings.language.getValue().equals(language.getShortName())) languages.getSelectionModel().select(box);
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
        newTrans.setOnAction((ActionEvent event) -> Main.hostServices.showDocument("https://pdf4teachers.org/Contribute/"));

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
            for(File file : new File(Main.dataFolder + "translations").listFiles()) file.delete();
        }
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
        Config.set(data, "it-it.name", "Italiano (Incompleto)");

        return data;
    }
    public static void addLanguageToConfig(String name, String displayName, int version){
        Config.set(languages, name + ".version", version);
        Config.set(languages, name + ".name", displayName);
    }

}
