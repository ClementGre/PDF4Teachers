package fr.themsou.main;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.io.*;

public class Settings {

    private int defaultZoom;
    private int maxPages;
    private BooleanProperty autoSave = new SimpleBooleanProperty();

    public Settings(){

        defaultZoom = 100;
        maxPages = 30;
        autoSave.set(false);

        loadSettings();

        autoSavingProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                saveSettings();
            }
        });
    }
    public void loadSettings(){

        new Thread(new Runnable() {
            @Override public void run() {

                new File(System.getProperty("user.home") + "/.PDFTeacher/").mkdirs();
                File settings = new File(System.getProperty("user.home") + "/.PDFTeacher/Settings.yml");
                try{
                    if(settings.createNewFile()){ //file was created
                        saveSettings();
                    }else{ // file already exist
                        BufferedReader reader = new BufferedReader(new FileReader(settings));

                        String line;
                        while((line = reader.readLine()) != null) {
                            switch(line.split("=")[0]){
                                case "defaultZoom":
                                    try{
                                        defaultZoom = Integer.parseInt(line.split("=")[1]);
                                    }catch(Exception ignored){}
                                    break;
                                case "maxPages":
                                    try{
                                        maxPages = Integer.parseInt(line.split("=")[1]);
                                    }catch(Exception ignored){}
                                    break;
                                case "autoSave":
                                    try{
                                        autoSave.set(Boolean.parseBoolean(line.split("=")[1]));
                                    }catch(Exception ignored){}
                                    break;
                            }
                        }
                        reader.close();
                    }
                }catch (IOException e){ e.printStackTrace(); }

            }
        }, "SettingsLoader").start();
    }

    public void saveSettings(){

        new Thread(new Runnable() {
            @Override public void run() {

                new File(System.getProperty("user.home") + "/.PDFTeacher/").mkdirs();
                File settings = new File(System.getProperty("user.home") + "/.PDFTeacher/Settings.yml");

                try{
                    settings.createNewFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(settings, false));

                    writer.write("defaultZoom=" + defaultZoom);
                    writer.newLine();
                    writer.write("maxPages=" + maxPages);
                    writer.newLine();
                    writer.write("autoSave=" + autoSave.get());

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
    public int getMaxPages(){
        return maxPages;
    }
    public boolean isAutoSave(){
        return autoSave.get();
    }
    public void setDefaultZoom(int zoom){
        this.defaultZoom = zoom;
        saveSettings();
    }
    public void setMaxPages(int maxPages){
        this.maxPages = maxPages;
        saveSettings();
    }
    public void setAutoSaving(boolean autoSave){
        this.autoSave.set(autoSave);
        saveSettings();
    }
    public BooleanProperty autoSavingProperty(){
        return this.autoSave;
    }
}
