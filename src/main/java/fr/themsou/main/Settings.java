package fr.themsou.main;

public class Settings {

    private int defaultZoom;
    private int maxPages;
    private boolean autoSave;

    public Settings(){

        defaultZoom = 150;
        maxPages = 10;
        autoSave = false;

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
        return autoSave;
    }

    public void setDefaultZoom(int zoom){

    }
    public void setMaxPages(int maxPages){

    }
    public void setAutoSaving(boolean autoSave){

    }



}
