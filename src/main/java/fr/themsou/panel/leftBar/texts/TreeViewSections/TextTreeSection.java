package fr.themsou.panel.leftBar.texts.TreeViewSections;

import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.panel.leftBar.texts.LBTextTab;
import fr.themsou.panel.leftBar.texts.SortPanelTreeItem;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.panel.leftBar.texts.TextTreeView;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.utils.sort.SortManager;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.windows.MainWindow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class TextTreeSection extends TreeItem {

    public static final int FAVORITE_TYPE = 1;
    public static final int LAST_TYPE = 2;
    public static final int ONFILE_TYPE = 3;

    // SORT

    public SortManager sortManager;
    public SortPanelTreeItem sortCell = new SortPanelTreeItem();
    public ToggleButton sortToggleBtn = new ToggleButton("");

    public String sectionName;
    public int sectionType;

    HBox pane = new HBox();
    ContextMenu menu;

    public TextTreeSection(String sectionName, int sectionType){
        this.sectionName = sectionName;
        this.sectionType = sectionType;

        setup();
    }

    public void setup(){

        sortToggleBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                getChildren().add(0, sortCell);
                setExpanded(true);
            }else{
                getChildren().remove(0);
            }
        });

        sortManager = new SortManager((sortType, order) -> {

            List<TextTreeItem> toSort = new ArrayList<>();
            for(int i = 0; i < getChildren().size(); i++){
                if(getChildren().get(i) instanceof TextTreeItem){
                    toSort.add((TextTreeItem) getChildren().get(i));
                }
            }
            clearElements();
            for(TextTreeItem item : TextTreeView.autoSortList(toSort, sortType, order)) getChildren().add(item);
        }, null);

        setupSortManager();
        setExpanded(true);
    }
    public abstract void setupSortManager();

    public void setupGraphics(){

        Builders.setPosition(sortToggleBtn, 0, 0, 30, 30, true);
        sortToggleBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/sort.png") +"", 0, 0));
        sortToggleBtn.setTooltip(Builders.genToolTip(TR.tr("Trier")));

        if(sortToggleBtn.isSelected()) sortToggleBtn.setStyle("");
        else sortToggleBtn.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
        sortToggleBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) sortToggleBtn.setStyle("");
            else sortToggleBtn.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
        });

        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(18);
        pane.setStyle("-fx-padding: -6 -6 -6 0;");

        Text name = new Text(sectionName);
        pane.getChildren().add(name);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        pane.getChildren().add(spacer);

        pane.getChildren().add(sortToggleBtn);

        menu = TextTreeView.getCategoryMenu(this);
    }
    public void updateGraphics(){
        if(sortToggleBtn.isSelected()) sortToggleBtn.setStyle("");
        else sortToggleBtn.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");

        sortManager.updateGraphics();

        MainWindow.lbTextTab.treeView.refresh();
    }
    public void updateCell(TreeCell cell){
        cell.setOnMouseClicked(null);

        cell.setMaxHeight(30);
        cell.setStyle("-fx-padding: 6 6 6 2; -fx-background-color: " + StyleManager.getHexAccentColor() + ";");
        cell.setContextMenu(menu);

        cell.setGraphic(pane);
    }

    public void updateChildrenGraphics(){
        for(Object item : getChildren()){
            if(item instanceof TextTreeItem) ((TextTreeItem) item).updateGraphic();
        }
    }

    public void clearElements(){
        List<TreeItem<String>> items = getChildren();
        for(int i = items.size()-1; i >= 0; i--){
            if(items.get(i) instanceof TextTreeItem){
                items.remove(i);
            }
        }
    }
    public void addElement(TextTreeItem element){
        if(!getChildren().contains(element)){
            getChildren().add(element);
            sortManager.simulateCall();
        }
    }
    public void removeElement(TextTreeItem element){
        getChildren().remove(element);
        sortManager.simulateCall();
    }
    public void removeElement(TextElement element){
        List<TreeItem<String>> items = getChildren();
        for(TreeItem<String> item : items){
            if(item instanceof TextTreeItem){
                if(((TextTreeItem) item).getCore() != null){
                    if(((TextTreeItem) item).getCore().equals(element)){
                        items.remove(item);
                        break;
                    }
                }
            }
        }
    }

}
