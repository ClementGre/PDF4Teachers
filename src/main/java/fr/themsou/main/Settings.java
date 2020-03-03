package fr.themsou.main;
import fr.themsou.utils.StringUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Settings {

    private int defaultZoom;
    private BooleanProperty autoSave = new SimpleBooleanProperty();
    private int regularSaving;
    private BooleanProperty restoreLastSession = new SimpleBooleanProperty();
    private BooleanProperty removeElementInPreviousListWhenAddingToFavorites = new SimpleBooleanProperty();
    private File[] openedFiles;
    private File openedFile;

    public Settings(){

        defaultZoom = 100;
        autoSave.set(false);
        regularSaving = -1;
        restoreLastSession.set(true);
        openedFiles = new File[]{};
        openedFile = null;
        removeElementInPreviousListWhenAddingToFavorites.set(false);

        loadSettings();

        autoSavingProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                saveSettings();
            }
        });
        restoreLastSessionProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                saveSettings();
            }
        });
        removeElementInPreviousListWhenAddingToFavorites.addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                saveSettings();
            }
        });
    }
    public void loadSettings(){

        new Thread(new Runnable() {
            @Override public void run() {

                new File(Main.dataFolder).mkdirs();
                File settings = new File(Main.dataFolder + "Settings.yml");
                try{

                    List<File> lastFiles = new ArrayList<>();
                    File lastFile = null;

                    if(settings.createNewFile()){ //file was created
                        saveSettings();
                    }else{ // file already exist
                        BufferedReader reader = new BufferedReader(new FileReader(settings));

                        String line;
                        while((line = reader.readLine()) != null) {

                            String value = StringUtils.removeBefore(line, '=');

                            switch(line.split("=")[0]){
                                case "defaultZoom":
                                    try{
                                        defaultZoom = Integer.parseInt(value);
                                    }catch(Exception ignored){}
                                break;
                                case "autoSave":
                                    try{
                                        autoSave.set(Boolean.parseBoolean(value));
                                    }catch(Exception ignored){}
                                break;
                                case "regularSaving":
                                    try{
                                        regularSaving = Integer.parseInt(value);
                                    }catch(Exception ignored){}
                                break;
                                case "restoreLastSession":
                                    try{
                                        restoreLastSession.set(Boolean.parseBoolean(value));
                                    }catch(Exception ignored){}
                                break;
                                case "openedFiles":
                                    try{
                                        for(String filePath : value.split(";/;")){
                                            if(new File(filePath).exists()){
                                                lastFiles.add(new File(filePath));
                                            }
                                        }

                                    }catch(Exception ignored){}
                                break;
                                case "openedFile":
                                    try{
                                        if(new File(value).exists()) lastFile = new File(value);
                                    }catch(Exception ignored){}
                                break;
                                case "removeElementInPreviousListWhenAddingToFavorites":
                                    try{
                                        removeElementInPreviousListWhenAddingToFavorites.set(Boolean.parseBoolean(value));
                                    }catch(Exception ignored){}
                                break;
                            }
                        }
                        reader.close();

                        if(restoreLastSession.get()){
                            openedFiles = new File[lastFiles.size()];
                            openedFiles = lastFiles.toArray(openedFiles);
                            openedFile = lastFile;
                        }


                    }
                }catch (IOException e){ e.printStackTrace(); }

            }
        }, "SettingsLoader").start();
    }

    public void saveSettings(){

        new Thread(new Runnable() {
            @Override public void run() {

                new File(Main.dataFolder).mkdirs();
                File settings = new File(Main.dataFolder + "Settings.yml");

                try{
                    settings.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(settings, false));

                    writer.write("defaultZoom=" + defaultZoom);

                    writer.newLine();
                    writer.write("autoSave=" + autoSave.get());

                    writer.newLine();
                    writer.write("regularSaving=" + regularSaving);

                    writer.newLine();
                    writer.write("restoreLastSession=" + restoreLastSession.get());

                    writer.newLine();
                    writer.write("openedFiles=");
                    for(File file : openedFiles){
                        writer.write(file.getAbsolutePath() + ";/;");
                    }

                    writer.newLine();
                    if(openedFile != null){
                        writer.write("openedFile=" + openedFile.getAbsolutePath());
                    }else{
                        writer.write("openedFile=");
                    }

                    writer.newLine();
                    writer.write("removeElementInPreviousListWhenAddingToFavorites=" + removeElementInPreviousListWhenAddingToFavorites.get());

                    writer.flush();
                    writer.close();

                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }, "SettingsSaver").start();

    }

    public int getDefaultZoom(){
        return defaultZoom;
    }
    public boolean isAutoSave(){
        return autoSave.get();
    }
    public void setDefaultZoom(int zoom){
        this.defaultZoom = zoom;
        saveSettings();
    }
    public void setAutoSaving(boolean autoSave){
        this.autoSave.set(autoSave);
        saveSettings();
    }
    public BooleanProperty autoSavingProperty(){
        return this.autoSave;
    }

    public int getRegularSaving() {
        return regularSaving;
    }
    public void setRegularSaving(int regularSaving) {
        this.regularSaving = regularSaving;
        saveSettings();
    }

    public boolean isRestoreLastSession() {
        return restoreLastSession.get();
    }
    public BooleanProperty restoreLastSessionProperty() {
        return restoreLastSession;
    }
    public void setRestoreLastSession(boolean restoreLastSession) {
        this.restoreLastSession.set(restoreLastSession);
        saveSettings();
    }

    public File[] getOpenedFiles() {
        return openedFiles;
    }
    public void setOpenedFiles(File[] openedFiles) {
        this.openedFiles = openedFiles;
        saveSettings();
    }
    public File getOpenedFile() {
        return openedFile;
    }
    public void setOpenedFile(File openedFile) {
        this.openedFile = openedFile;
        saveSettings();
    }

    public boolean isRemoveElementInPreviousListWhenAddingToFavorites() {
        return removeElementInPreviousListWhenAddingToFavorites.get();
    }
    public BooleanProperty removeElementInPreviousListWhenAddingToFavoritesProperty() {
        return removeElementInPreviousListWhenAddingToFavorites;
    }
    public void setRemoveElementInPreviousListWhenAddingToFavorites(boolean removeElementInPreviousListWhenAddingToFavorites) {
        this.removeElementInPreviousListWhenAddingToFavorites.set(removeElementInPreviousListWhenAddingToFavorites);
        saveSettings();
    }
}
