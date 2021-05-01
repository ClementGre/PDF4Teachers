package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class ListsManager{
    
    public final Button loadListBtn = new Button();
    public final Button saveListBtn = new Button();
    
    private ContextMenu menu = new ContextMenu();
    private final TextTreeSection section;
    
    public ListsManager(TextTreeSection section){
        this.section = section;
        
        loadListBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.LIST, "black", 0, 18, 18, ImageUtils.defaultDarkColorAdjust));
        loadListBtn.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("textTab.lists.show.tooltip")));
        saveListBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.SAVE, "black", 0, 18, 18, ImageUtils.defaultDarkColorAdjust));
        saveListBtn.setTooltip(PaneUtils.genWrappedToolTip(TR.tr("textTab.lists.save.tooltip")));
        
        PaneUtils.setPosition(loadListBtn, 0, 0, 30, 30, true);
        PaneUtils.setPosition(saveListBtn, 0, 0, 30, 30, true);
        
        updateGraphics();
        
        setupMenu();
        loadListBtn.setOnMouseClicked(e -> {
            menu.show(loadListBtn, e.getScreenX(), e.getScreenY());
        });
        
        saveListBtn.setOnAction(event -> {
            TextInputDialog alert = new TextInputDialog(TR.tr("textTab.lists.defaultName"));
            DialogBuilder.setupDialog(alert);
            
            alert.setTitle(TR.tr("textTab.lists.save.dialog.title"));
            alert.setHeaderText(TR.tr("textTab.lists.save.dialog.header"));
            alert.setContentText(TR.tr("textTab.lists.save.dialog.details"));
            
            Optional<String> result = alert.showAndWait();
            if(result.isPresent()){
                if(!result.get().isEmpty()){
                    if(TextTreeSection.lists.containsKey(result.get())){
                        Alert alert2 = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("textTab.lists.save.alreadyExistDialog.title"));
                        alert2.setHeaderText(TR.tr("textTab.lists.save.alreadyExistDialog.header"));
                        
                        ButtonType rename = new ButtonType(TR.tr("dialog.actionError.rename"), ButtonBar.ButtonData.NO);
                        ButtonType erase = new ButtonType(TR.tr("dialog.actionError.overwrite"), ButtonBar.ButtonData.APPLY);
                        alert2.getButtonTypes().setAll(rename, erase);
                        
                        Optional<ButtonType> result2 = alert2.showAndWait();
                        if(result2.get() == erase){
                            saveList(result.get());
                        }else{
                            saveListBtn.fire();
                        }
                    }else saveList(result.get());
                }
            }
        });
    }
    
    public void updateGraphics(){
        loadListBtn.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
        saveListBtn.setStyle("-fx-background-color: " + StyleManager.getHexAccentColor() + ";");
    }
    
    public static void setupMenus(){
        MainWindow.textTab.treeView.favoritesSection.listsManager.setupMenu();
        MainWindow.textTab.treeView.lastsSection.listsManager.setupMenu();
    }
    
    public void setupMenu(){
        menu.getItems().clear();
        menu.setMinWidth(400);
        menu.setPrefWidth(400);
        
        if(TextTreeSection.lists.size() >= 1){
            for(Map.Entry<String, ArrayList<TextListItem>> list : TextTreeSection.lists.entrySet()){
                NodeMenuItem menuItem = new NodeMenuItem(list.getKey());
                menu.getItems().add(menuItem);
                menuItem.setOnAction(event -> {
                    
                    Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("textTab.lists.actionDialog.title"));
                    alert.setHeaderText(TR.tr("textTab.lists.actionDialog.header"));
                    alert.setContentText(TR.tr("textTab.lists.actionDialog.details"));
                    
                    ButtonType cancel = new ButtonType(TR.tr("actions.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType load = new ButtonType(TR.tr("actions.load"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType loadReplace = new ButtonType(TR.tr("Vider et charger"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType delete = new ButtonType(TR.tr("actions.delete"), ButtonBar.ButtonData.OTHER);
                    alert.getButtonTypes().setAll(cancel, loadReplace, load, delete);
                    
                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.get() == load) loadList(list.getValue(), false);
                    else if(result.get() == loadReplace) loadList(list.getValue(), true);
                    else if(result.get() == delete) deleteList(list.getKey());
                    
                });
            }
        }else{
            menu.getItems().add(new NodeMenuItem(TR.tr("textTab.lists.show.none")));
        }
    }
    
    public void loadList(ArrayList<TextListItem> items, boolean flush){
        if(flush) section.clearElements();
        for(TextListItem item : items) section.getChildren().add(item.toTextTreeItem(section.sectionType));
        section.sortManager.simulateCall();
    }
    
    public void saveList(String listName){
        TextTreeSection.lists.remove(listName);
        ArrayList<TextListItem> list = new ArrayList<>();
        for(Object item : section.getChildren()){
            if(item instanceof TextTreeItem){
                list.add(((TextTreeItem) item).toTextItem());
            }
        }
        if(list.size() == 0){
            Alert alert = DialogBuilder.getAlert(Alert.AlertType.ERROR, TR.tr("textTab.lists.save.voidListDialog.title"));
            alert.setHeaderText(TR.tr("textTab.lists.save.voidListDialog.header"));
            alert.setContentText(TR.tr("textTab.lists.save.voidListDialog.details"));
            alert.show();
            return;
        }
        TextTreeSection.lists.put(listName, list);
        
        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("textTab.lists.save.completedDialog.title"));
        alert.setHeaderText(TR.tr("textTab.lists.save.completedDialog.header"));
        alert.setContentText(TR.tr("textTab.lists.save.completedDialog.details"));
        alert.show();
        ListsManager.setupMenus();
    }
    
    public void deleteList(String listName){
        TextTreeSection.lists.remove(listName);
        
        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("textTab.lists.deleteCompletedDialog.title"));
        alert.setHeaderText(TR.tr("textTab.lists.deleteCompletedDialog.header", listName));
        alert.show();
        ListsManager.setupMenus();
    }
    
}
