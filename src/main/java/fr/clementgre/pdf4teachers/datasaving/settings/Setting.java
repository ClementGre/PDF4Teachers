package fr.clementgre.pdf4teachers.datasaving.settings;

import javafx.scene.control.MenuItem;

public abstract class Setting<T> {

    private String path;
    String icon;
    String title;
    String description;
    MenuItem menuItem;

    public Setting(String icon, String path, String title, String description) {
        this.icon = icon;
        this.path = path;
        this.title = title;
        this.description = description;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public abstract void setupMenuItem();
    public abstract T getValue();
    public abstract void setValue(T value);

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
