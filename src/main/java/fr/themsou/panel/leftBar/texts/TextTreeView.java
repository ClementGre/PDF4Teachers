package fr.themsou.panel.leftBar.texts;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeFavorites;
import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeLasts;
import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeOnFile;
import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeSection;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import fr.themsou.utils.sort.Sorter;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class TextTreeView extends TreeView<String>{

    public static TreeItem<String> treeViewRoot = new TreeItem<>();

    public static TextTreeFavorites favoritesSection = new TextTreeFavorites();
    public static TextTreeLasts lastsSection = new TextTreeLasts();
    public static TextTreeOnFile onFileSection = new TextTreeOnFile();

    public TextTreeView(Pane pane){

        setMaxWidth(400);
        setShowRoot(false);
        setEditable(true);
        setRoot(treeViewRoot);
        treeViewRoot.getChildren().addAll(favoritesSection, lastsSection, onFileSection);

        disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        setBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY)));

        prefHeightProperty().bind(pane.heightProperty().subtract(layoutYProperty()));
        prefWidthProperty().bind(pane.widthProperty());
        widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            // Update element's graphic only if it is the last width value
            new Thread(() -> {
                try{ Thread.sleep(200); }catch(InterruptedException e){ e.printStackTrace(); }
                Platform.runLater(() -> {
                    if(getWidth() == newValue.longValue()) updateListsGraphic();
                });
            }).start();
        });

        setCellFactory((TreeView<String> param) -> new TreeCell<>() {
            @Override protected void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);

                // Null
                if(empty){
                    setGraphic(null);
                    setStyle(null);
                    setContextMenu(null);
                    setOnMouseClicked(null);
                    return;
                }

                // TextElement
                if(getTreeItem() instanceof TextTreeItem){
                    ((TextTreeItem) getTreeItem()).updateCell(this);
                    return;
                }
                // TreeSection
                if(getTreeItem() instanceof TextTreeSection){
                    ((TextTreeSection) getTreeItem()).updateCell(this);
                    return;
                }
                // SortPanel
                if(getTreeItem() instanceof SortPanelTreeItem){
                    ((SortPanelTreeItem) getTreeItem()).updateCell(this);
                    return;
                }

                // Other
                setStyle(null);
                setGraphic(null);
                setContextMenu(null);
                setOnMouseClicked(null);
            }
        });
    }

    public static ContextMenu getCategoryMenu(TextTreeSection section){

        ContextMenu menu = new ContextMenu();


        if(section.sectionType == TextTreeSection.ONFILE_TYPE){
            NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.tr("Supprimer tous les éléments textuels"), -1, false);
            item3.setToolTip(TR.tr("Supprime tous les éléments textuels ajoutés au document, cela va donc supprimer une partie de l'édition."));
            menu.getItems().addAll(item3);

            item3.setOnAction(e -> {
                for(PageRenderer page : MainWindow.mainScreen.document.pages){
                    page.clearTextElements();
                }
                TextTreeView.onFileSection.updateElementsList();
                Edition.setUnsave();
            });
        }else{
            NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Vider la liste"), -1, false);
            item1.setToolTip(TR.tr("Supprime tous les éléments de la liste. Ne supprime en aucun cas les éléments sur le document."));
            NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Supprimer les donnés d'utilisation"), -1, false);
            item2.setToolTip(TR.tr("Réinitialise les donnés des éléments de la liste indiquant le nombre d'utilisation de l'élément. Cela va réinitialiser l'ordre du tri par Utilisation."));
            menu.getItems().addAll(item1, item2);

            item1.setOnAction(e -> {
                section.clearElements();
            });
            item2.setOnAction(e -> {
                for(Object element : section.getChildren()){
                    if(element instanceof TextTreeItem){
                        ((TextTreeItem) element).setUses(0);
                    }
                }
                if(section.sortManager.getSelectedButton().getText().equals(TR.tr("Utilisation"))){
                    section.sortManager.simulateCall();
                }
            });
        }
        Builders.setMenuSize(menu);
        return menu;
    }

    public static ContextMenu getNewMenu(TextTreeItem element){

        ContextMenu menu = new ContextMenu();
        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Ajouter et lier"), -1, false);
        item1.setToolTip(TR.tr("Ajoute cet élément à l'édition et lie l'élément de l'édition avec celui de la liste. Toute modification apportée à l'élément de l'édition entrainera la modification de l'élément dans la liste."));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Retirer"), -1, false);
        item2.setToolTip(TR.tr("Retire cet élément de la liste. Dans la liste Éléments sur ce document, ceci supprime aussi l'élément sur le document édité"));
        NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.tr("Ajouter aux favoris"), -1, false);
        item3.setToolTip(TR.tr("Ajoute cet élément à la liste des éléments précédents."));
        NodeMenuItem item4 = new NodeMenuItem(new HBox(), TR.tr("Ajouter aux éléments précédents"), -1, false);
        item4.setToolTip(TR.tr("Ajoute cet élément à la liste des éléments favoris."));
        NodeMenuItem item5 = new NodeMenuItem(new HBox(), TR.tr("Dé-lier l'élément"), -1, false);
        item5.setToolTip(TR.tr("L'élément de la liste ne sera plus synchronisé avec l'élément du document"));


        // Ajouter les items en fonction du type
        if(element.getType() != TextTreeSection.ONFILE_TYPE) menu.getItems().add(item1);
        menu.getItems().add(item2);
        if(element.getType() != TextTreeSection.FAVORITE_TYPE) menu.getItems().add(item3); // onFile & lasts
        if(element.getType() == TextTreeSection.ONFILE_TYPE) menu.getItems().add(item4); // onFile
        if(element.getType() != TextTreeSection.ONFILE_TYPE && element.getCore() != null) menu.getItems().add(item5); // élément précédent qui est lié

        Builders.setMenuSize(menu);

        // Définis les actions des boutons
        item1.setOnAction((e) -> {
            element.addToDocument(true);
            if(element.getType() == TextTreeSection.FAVORITE_TYPE){
                if(TextTreeView.favoritesSection.sortManager.getSelectedButton().getText().equals(TR.tr("Utilisation"))){
                    TextTreeView.favoritesSection.sortManager.simulateCall();
                }
            }else if(element.getType() == TextTreeSection.LAST_TYPE){
                if(TextTreeView.lastsSection.sortManager.getSelectedButton().getText().equals(TR.tr("Utilisation"))){
                    TextTreeView.lastsSection.sortManager.simulateCall();
                }
            }
        });
        item2.setOnAction((e) -> {
            if(element.getType() == TextTreeSection.ONFILE_TYPE){
                element.getCore().delete();
            }else{
                removeSavedElement(element);
            }
        });
        item3.setOnAction((e) -> {
            addSavedElement(new TextTreeItem(element.getFont(), element.getText(), element.getColor(), TextTreeSection.FAVORITE_TYPE, 0, System.currentTimeMillis()/1000));
            if(element.getType() == TextTreeSection.LAST_TYPE){
                if(Main.settings.isRemoveElementInPreviousListWhenAddingToFavorites()){
                    removeSavedElement(element);
                }
            }
        });
        item4.setOnAction((e) -> {
            addSavedElement(new TextTreeItem(element.getFont(), element.getText(), element.getColor(), TextTreeSection.LAST_TYPE, 0, System.currentTimeMillis()/1000));
        });
        item5.setOnAction((e) -> {
            element.unLink();
        });
        return menu;

    }

    public static void updateListsGraphic(){
        favoritesSection.updateChildrenGraphics();
        lastsSection.updateChildrenGraphics();
        onFileSection.updateChildrenGraphics();
    }

    public static void addSavedElement(TextTreeItem element){
        if(element.getType() == TextTreeSection.FAVORITE_TYPE){
            favoritesSection.addElement(element);
        }else if(element.getType() == TextTreeSection.LAST_TYPE){
            lastsSection.addElement(element);
        }
    }
    public static void removeSavedElement(TextTreeItem element){
        if(element.getType() == TextTreeSection.FAVORITE_TYPE){
            favoritesSection.removeElement(element);
        }else if(element.getType() == TextTreeSection.LAST_TYPE){
            lastsSection.removeElement(element);
        }
    }

    public static List<TextTreeItem> autoSortList(List<TextTreeItem> toSort, String sortType, boolean order){

        if(sortType.equals(TR.tr("Ajout"))){
            return Sorter.sortElementsByDate(toSort, order);
        }else if(sortType.equals(TR.tr("Nom"))){
            return Sorter.sortElementsByName(toSort, order);
        }else if(sortType.equals(TR.tr("Utilisation"))){
            return Sorter.sortElementsByUtils(toSort, order);
        }else if(sortType.equals(TR.tr("Police"))){
            return Sorter.sortElementsByPolice(toSort, order);
        }else if(sortType.equals(TR.tr("Taille"))){
            return Sorter.sortElementsBySize(toSort, order);
        }else if(sortType.equals(TR.tr("Couleur"))){
            return Sorter.sortElementsByColor(toSort, order);
        }else if(sortType.equals(TR.tr("Position"))){
            return Sorter.sortElementsByCorePosition(toSort, order);
        }
        return toSort;
    }

    public static List<TextTreeItem> getMostUseElements(){

        List<TextTreeItem> toSort = new ArrayList<>();
        for(int i = 0; i < favoritesSection.getChildren().size(); i++){
            if(favoritesSection.getChildren().get(i) instanceof TextTreeItem){
                toSort.add((TextTreeItem) favoritesSection.getChildren().get(i));
            }
        }
        for(int i = 0; i < lastsSection.getChildren().size(); i++){
            if(lastsSection.getChildren().get(i) instanceof TextTreeItem){
                toSort.add((TextTreeItem) lastsSection.getChildren().get(i));
            }
        }
        return autoSortList(toSort, TR.tr("Utilisation"), true);

    }
}


