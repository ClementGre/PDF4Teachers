package fr.clementgre.pdf4teachers.interfaces.windows.language;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.UserData;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguagesUpdater {

    private final Alert loadingAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Téléchargement des derniers packs de languages"));
    private final ProgressBar loadingBar = new ProgressBar();
    private final Label currentLanguage = new Label(TR.tr("Actualisation de la base de donnée..."));

    public LanguagesUpdater(){
        loadingAlert.setWidth(600);
        loadingAlert.setHeaderText(TR.tr("Téléchargement des derniers packs de languages..."));
        VBox pane = new VBox();
        loadingBar.setMinHeight(10);
        VBox.setMargin(loadingBar, new Insets(10, 0, 0,0));
        pane.getChildren().addAll(currentLanguage, loadingBar);
        loadingAlert.getDialogPane().setContent(pane);
    }

    public static void backgroundCheck(){
        Platform.runLater(() -> {
            new LanguagesUpdater().update((hasDownloadedLanguages) -> {
                if(hasDownloadedLanguages){
                    Main.window.restart();
                }
            }, true, true);
        });
    }
    public static void backgroundStats(){
        Platform.runLater(() -> {
            new LanguagesUpdater().updateStats(null);
        });
    }
    public static void backgroundStats(CallBack callBack){
        Platform.runLater(() -> {
            new LanguagesUpdater().updateStats(callBack);
        });
    }

    private class Language{
        private HashMap<String, String> urls = new HashMap<>();
        private String release;
        private int version;
        private String name;
        private String displayName;
        public HashMap<String, String> getUrls() {
            return urls;
        }
        public void pushUrl(Map.Entry<String, String> urls) {
            this.urls.put(urls.getKey(), urls.getValue());
        }
        public String getRelease() {
            return release;
        }
        public void setRelease(String release) {
            this.release = release;
        }
        public int getVersion() {
            return version;
        }
        public void setVersion(int version) {
            this.version = version;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getDisplayName() {
            return displayName;
        }
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        @Override
        public String toString() {
            return "Language{" +
                    "urls=" + urls.toString() +
                    ", release=" + release +
                    ", version=" + version +
                    ", name='" + name + '\'' +
                    ", displayName='" + displayName + '\'' +
                    '}';
        }
    }
    public void updateStats(CallBack callBack){
        new Thread(() -> {
            String uuid = Main.DEBUG ? "DEBUG" : MainWindow.userData.uuid;
            try {
                URL url = new URL("https://api.pdf4teachers.org/startupdate/?time=" + MainWindow.userData.foregroundTime +
                        "&starts=" + MainWindow.userData.startsCount +
                        "&lang=" + Main.settings.language.getValue() +
                        "&version=" + Main.VERSION +
                        "&id=" + uuid);
                if(!Main.settings.sendStats.getValue()){
                    url = new URL("https://api.pdf4teachers.org/startupdate/?time=0&starts=0&version=" + Main.VERSION + "&id=" + uuid);
                }
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(false);
                if(Main.DEBUG){
                    int responseCode = con.getResponseCode();
                    System.out.println("updating stats with response code " + responseCode);
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            if(callBack != null) callBack.call();
        }).start();

    }

    public void update(CallBackArg<Boolean> callBack, boolean hideFirstDialogState, boolean provideData){

        if(!hideFirstDialogState) loadingAlert.show();

        new Thread(() -> {
            try{
                URL url = new URL("https://api.pdf4teachers.org/startupdate/");
                if(provideData){
                    String uuid = Main.DEBUG ? "DEBUG" : MainWindow.userData.uuid;
                    if(Main.settings.sendStats.getValue()){
                        url = new URL("https://api.pdf4teachers.org/startupdate/?time=" + MainWindow.userData.foregroundTime +
                                "&starts=" + MainWindow.userData.startsCount +
                                "&lang=" + Main.settings.language.getValue() +
                                "&version=" + Main.VERSION +
                                "&id=" + uuid);
                    }else{
                        url = new URL("https://api.pdf4teachers.org/startupdate/?time=0&starts=0&version=" + Main.VERSION + "&id=" + uuid);
                    }
                }
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                if(Main.DEBUG){
                    int responseCode = con.getResponseCode();
                    System.out.println("updating language with response code " + responseCode);
                    if(responseCode != 200) System.out.println(con.getResponseMessage());
                }


                JsonFactory jfactory = new JsonFactory();
                JsonParser jParser = jfactory.createParser(con.getInputStream());

                List<Language> languages = new ArrayList<>();
                Language currentLanguage = null;

                int indentLevel = 0;
                JsonToken token; // Current Token (START_OBJECT, END_OBJECT, VALUE_STRING, FIELD_NAME)
                while((token = jParser.nextToken()) != null){
                    String jsonField = jParser.getCurrentName(); // Current json Object or Field
                    //jParser.getText(); jParser.getIntValue()  // Current value of Field or Object ({, }, [, ])

                    if(indentLevel == 1 && token == JsonToken.FIELD_NAME){
                        currentLanguage = new Language();
                        currentLanguage.setName(jsonField);

                    }else if(indentLevel == 2 && (token == JsonToken.VALUE_STRING || token == JsonToken.VALUE_NUMBER_INT)){
                        assert currentLanguage != null;
                        switch(jsonField){
                            case "release":
                                currentLanguage.setRelease(jParser.getText());
                                break;
                            case "version":
                                currentLanguage.setVersion(jParser.getIntValue());
                                break;
                            case "name":
                                currentLanguage.setDisplayName(jParser.getText());
                                break;
                        }
                    }else if(indentLevel == 3 && token == JsonToken.VALUE_STRING){
                        assert currentLanguage != null;
                        currentLanguage.pushUrl(Map.entry(jsonField, jParser.getText()));
                    }
                    if(indentLevel == 2 && token == JsonToken.END_OBJECT){
                        languages.add(currentLanguage);
                        currentLanguage = null;
                    }

                    if(token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) indentLevel++;
                    if(token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY) indentLevel--;
                }
                jParser.close();



                List<Language> toDownloadLanguages = new ArrayList<>();
                if(Main.DEBUG) System.out.println("Listing languages :");
                for(Language language : languages){
                    if(Main.DEBUG) System.out.print(language.toString());
                    if(isLanguageAlreadyExisting(language)){
                        if(Main.DEBUG) System.out.println(" (Already existing)");
                    }else{
                        if(Main.DEBUG) System.out.println(" (Will be downloaded)");
                        toDownloadLanguages.add(language);
                    }
                }


                Boolean haveToDownloadLanguages = toDownloadLanguages.size() != 0;
                if(haveToDownloadLanguages){
                    Platform.runLater(loadingAlert::show);
                }

                int i = 0;
                for(Language language : toDownloadLanguages){
                    if(!downloadLanguage(language, i, toDownloadLanguages.size())){
                        complete(callBack, false);
                        return;
                    }
                    i++;
                }

                complete(callBack, haveToDownloadLanguages);

            }catch(IOException e){
                e.printStackTrace();
                complete(callBack, false);
            }

        }, "Languages Updater").start();


    }
    private void complete(CallBackArg<Boolean> callBack, Boolean hasDownloadedLanguages){
        Platform.runLater(() -> {
            loadingAlert.close();
            callBack.call(hasDownloadedLanguages);
        });
    }

    private boolean isLanguageAlreadyExisting(Language language){

        if(!Main.VERSION.equals(language.getRelease())) return true;

        if(LanguageWindow.getLanguagesConfig().containsKey(language.getName())){
            Object existing = LanguageWindow.getLanguagesConfig().get(language.getName());
            if(existing instanceof HashMap){
                HashMap<String, Object> existingLanguage = (HashMap<String, Object>) existing;
                if(((int) existingLanguage.get("version")) >= language.getVersion()){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean downloadLanguage(Language language, int index, int size){

        int subIndex = 0;
        int subSize = language.getUrls().size();
        for(Map.Entry<String, String> urls : language.getUrls().entrySet()){
            subIndex++;

            int finalSubIndex = subIndex;
            Platform.runLater(() -> {
                this.currentLanguage.setText(language.getDisplayName() + " v" + language.getVersion() + " - " + urls.getKey() + " (" + (finalSubIndex +1) + "/" + subSize + ")");
                loadingBar.setProgress(finalSubIndex /((float)subSize-1));
            });
            try{
                BufferedInputStream in = new BufferedInputStream(new URL(urls.getValue()).openStream());
                File target = new File(Main.dataFolder + "translations" + File.separator + urls.getKey());

                File closed = null;
                if(MainWindow.mainScreen != null){
                    if(MainWindow.mainScreen.hasDocument(false)){
                        if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(target.getAbsolutePath())){
                            closed = MainWindow.mainScreen.document.getFile();
                            Platform.runLater(() -> MainWindow.mainScreen.closeFile(false));
                            Thread.sleep(500);
                        }
                    }
                }
                Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LanguageWindow.addLanguageToConfig(language.getName(), language.getDisplayName(), language.getVersion());

                if(closed != null) {
                    File finalClosed = closed;
                    Platform.runLater(() -> MainWindow.mainScreen.openFile(finalClosed));
                }

            }catch(IOException | InterruptedException e){
                e.printStackTrace();
                return false;
            }
        }
        return true;

    }

}
