package fr.clementgre.pdf4teachers.datasaving.settings;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.datasaving.UserDataObject;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.leftBar.texts.TextTreeView;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Settings {

    private String settingsVersion = "";

    @SettingObject
    public StringSetting language = new StringSetting("", "language", "language",
            TR.ct("Langage (Français France)"), TR.ct("Définit la langue de l'interface"));
    @SettingObject
    public BooleanSetting restoreLastSession = new BooleanSetting(true, "recharger", "restoreLastSession",
            TR.ct("Toujours restaurer la session précédente"), TR.ct("Réouvre les derniers fichiers ouverts lors de l'ouverture de l'application."));
    @SettingObject
    public BooleanSetting checkUpdates = new BooleanSetting(true, "wifi", "checkUpdates",
            TR.ct("Alerter quand une mise à jour est disponible"), TR.ct("Fait apparaître une fenêtre à chaque démarrage si une nouvelle version est disponible. Même si cette option est désactivée, l'application vérifiera si une nouvelle version est disponible et affichera le menu À propos en couleur"));
    @SettingObject
    public BooleanSetting sendStats = new BooleanSetting(true, "wifi", "sendStatistics",
            TR.ct("Envoyer des statistiques d'utilisation anonymes"), TR.ct("Partage le temps total passé sur l'application et le nombre de lancements de l'application. Même avec cette option désactivée, l'application enverra des requêtes de statistiques (sans passer de valeurs)."));
    @SettingObject
    public IntSetting defaultZoom = new IntSetting(130, "zoom", "defaultZoom",
            TR.ct("Zoom lors de l'ouverture d'un document"), TR.ct("Définit le zoom par défaut lors de l'ouverture d'un document. Le zoom est aussi contrôlé avec Ctrl+Molette ou pincement sur trackpad"), true);
    @SettingObject
    public BooleanSetting zoomAnimations = new BooleanSetting(!Main.isOSX(), "cloud", "zoomAnimations",
            TR.ct("Animations de zoom ou défilement"), TR.ct("Permet des transitions fluides lors d'un zoom ou d'un défilement de la page. Il est possible de désactiver cette option si l'ordinateur est lent lors du zoom. Cette option est déconseillée aux utilisateurs de TrackPad"));
    @SettingObject
    public BooleanSetting darkTheme = new BooleanSetting(true, "settings", "darkTheme",
            TR.ct("Thème sombre"), TR.ct("Change les couleurs de l'interface vers un thème plus sombre."));

    @SettingObject
    public BooleanSetting autoSave = new BooleanSetting(true, "sauvegarder", "autoSave",
            TR.ct("Sauvegarder automatiquement"), TR.ct("Sauvegarde l'édition du document automatiquement lors de la fermeture du document ou de l'application."));
    @SettingObject
    public IntSetting regularSave = new IntSetting(-1, "sauvegarder-recharger", "regularSave",
            TR.ct("Sauvegarder régulièrement"), TR.ct("Sauvegarde l'édition du document automatiquement toutes les x minutes."));

    @SettingObject
    public BooleanSetting textAutoRemove = new BooleanSetting(true, "favoris", "textAutoRemove",
            TR.ct("Supprimer l'élément des éléments précédents\nlorsqu'il est ajouté aux favoris"), TR.ct("Dans la liste des derniers éléments textuels utilisés, retire automatiquement l'élément lorsqu'il est ajouté aux favoris."));
    @SettingObject
    public BooleanSetting textOnlyStart = new BooleanSetting(true, "lines", "textOnlyStart",
            TR.ct("N'afficher que le début des éléments textuels"), TR.ct("Dans les liste des éléments textuels, n'affiche que les deux premières lignes de l'élément."));
    @SettingObject
    public BooleanSetting textSmall = new BooleanSetting(false, "cursor", "textSmall",
            TR.ct("Réduire la taille des éléments dans les listes"), TR.ct("Dans les liste des éléments textuels, affiche les éléments en plus petit."));

    @SettingObject
    public StringSetting mainScreenSize = new StringSetting("1200;675;-1;-1;false", null, "mainScreenSize",
            "", "");

    @SettingObject
    public BooleanSetting allowAutoTips = new BooleanSetting(true, "info", "allowAutoTips",
            TR.ct("Conseils automatiques"), TR.ct("Fait apparaître des messages d'aide/conseil de temps en temps ou selon les actions qui sont faites. Cliquer sur OK empêchera un conseil de réapparaitre. Désactiver puis réactiver l'option réinitialise la liste des conseils lus."));

    public Settings(){
        loadSettings();

        textOnlyStart.valueProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) -> {
            if(MainWindow.textTab != null) TextTreeView.updateListsGraphic();
        });
        textSmall.valueProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) -> {
            if(MainWindow.textTab != null) TextTreeView.updateListsGraphic();
            if(t1) MainWindow.textTab.txtArea.setStyle("-fx-font-size: 12");
            else MainWindow.textTab.txtArea.setStyle("-fx-font-size: 13");
        });
    }
    public void loadSettings(){

        new File(Main.dataFolder).mkdirs();
        File settings = new File(Main.dataFolder + "settings.yml");
        try{

            if(settings.createNewFile()){ //file was created
                saveSettings();
            }else{ // file already exist

                Config config = new Config(settings);
                config.load();

                settingsVersion = config.getString("version");

                for(Field field : getClass().getDeclaredFields()) {
                    if(field.isAnnotationPresent(SettingObject.class)){
                        try {
                            if(field.getType() == StringSetting.class){
                                StringSetting var = (StringSetting) field.get(this);
                                String value = config.getString(var.getPath());
                                if(!value.isEmpty()) var.setValue(value);

                            }else if(field.getType() == BooleanSetting.class){
                                BooleanSetting var = (BooleanSetting) field.get(this);
                                Boolean value = config.getBooleanNull(var.getPath());
                                if(value != null) var.setValue(value);

                            }else if(field.getType() == IntSetting.class){
                                IntSetting var = (IntSetting) field.get(this);
                                Long value = config.getLongNull(var.getPath());
                                if(value != null) var.setValue(Math.toIntExact(value));
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                if(!settingsVersion.equals(Main.VERSION)) saveSettings();
            }
        }catch (IOException e){ e.printStackTrace(); }
    }

    public void saveSettings(){
        new Thread(() -> {
            try{
                new File(Main.dataFolder).mkdirs();
                File settings = new File(Main.dataFolder + "settings.yml");
                settings.createNewFile();
                Config config = new Config(settings);

                config.set("version", Main.VERSION);

                for(Field field : getClass().getDeclaredFields()) {
                    if(field.isAnnotationPresent(SettingObject.class)){
                        try{
                            Setting setting = (Setting) field.get(this);
                            config.set(setting.getPath(), setting.getValue());
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                config.save();

            }catch(IOException e){
                e.printStackTrace();
            }
        }, "settingsSaver").start();

    }

    public String getSettingsVersion() {
        return settingsVersion;
    }
}
