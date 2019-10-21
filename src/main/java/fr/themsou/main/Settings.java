package fr.themsou.main;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Settings {

    private int defaultZoom;
    private int maxPages;
    private BooleanProperty autoSave = new SimpleBooleanProperty();

    public Settings(){

        defaultZoom = 100;
        maxPages = 30;
        autoSave.set(false);

        autoSavingProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                saveSettings();
            }
        });
    }
    public void loadSettings(){

    }

    public void saveSettings(){

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
