package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class ListsManager {

    public final Button loadListBtn = new Button();
    public final Button saveListBtn = new Button();

    private ContextMenu menu = new ContextMenu();
    private final TextTreeSection section;

    public ListsManager(TextTreeSection section){
        this.section = section;

        loadListBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.LIST, "black", 0, 18, 18, ImageUtils.defaultDarkColorAdjust));
        loadListBtn.setTooltip(PaneUtils.genToolTip(TR.trO("Afficher les listes d'éléments enregistrés")));
        saveListBtn.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.SAVE, "black", 0, 18, 18, ImageUtils.defaultDarkColorAdjust));
        saveListBtn.setTooltip(PaneUtils.genToolTip(TR.trO("Sauvegarder les éléments favoris en tant qu'une nouvelle liste")));

        PaneUtils.setPosition(loadListBtn, 0, 0, 30, 30, true);
        PaneUtils.setPosition(saveListBtn, 0, 0, 30, 30, true);

        updateGraphics();

        setupMenu();
        loadListBtn.setOnMouseClicked(e -> {
            menu.show(loadListBtn, e.getScreenX(), e.getScreenY());
        });

        saveListBtn.setOnAction(event -> {
            TextInputDialog alert = new TextInputDialog(TR.trO("Nouvelle liste"));
            DialogBuilder.setupDialog(alert);

            alert.setTitle(TR.trO("Enregistrer les éléments de cette catégorie"));
            alert.setHeaderText(TR.trO("Vous allez enregistrer les éléments de cette catégorie dans une nouvelle liste."));
            alert.setContentText(TR.trO("Donner un nom à votre liste"));

            Optional<String> result = alert.showAndWait();
            if(result.isPresent()){
                if(!result.get().isEmpty()){
                    if(TextTreeSection.lists.containsKey(result.get())){
                        Alert alert2 = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.trO("Liste déjà existante"));
                        alert2.setHeaderText(TR.trO("Une liste du même nom existe déjà."));
                        alert2.setContentText(TR.trO("Choisissez une action."));

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
                MenuItem menuItem = new MenuItem(list.getKey());
                menu.getItems().add(menuItem);
                menuItem.setOnAction(event -> {

                    Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.trO("Actions de listes"));
                    alert.setHeaderText(TR.trO("Choisissez une action a effectuer avec la liste d'éléments.") + "\n" + TR.trO("Ces actions sont irréversibles."));
                    alert.setContentText(TR.trO("- Vider et charger remplacera la liste des éléments favoris/précédents par celle ci") + "\n" +
                                         TR.trO("- Charger ajoutera cette liste d'éléments à la liste des éléments favoris/précédents") + "\n" +
                                         TR.trO("- Supprimer supprimera la liste de la base de donnée"));

                    ButtonType cancel = new ButtonType(TR.trO("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType load = new ButtonType(TR.trO("Charger"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType loadReplace = new ButtonType(TR.trO("Vider et charger"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType delete = new ButtonType(TR.trO("Supprimer"), ButtonBar.ButtonData.OTHER);
                    alert.getButtonTypes().setAll(cancel, loadReplace, load, delete);

                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.get() == load) loadList(list.getValue(), false);
                    else if(result.get() == loadReplace) loadList(list.getValue(), true);
                    else if(result.get() == delete) deleteList(list.getKey());

                });
            }
        }else{
            menu.getItems().add(new MenuItem(TR.trO("Aucune liste sauvegardée")));
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
            Alert alert = DialogBuilder.getAlert(Alert.AlertType.ERROR, TR.trO("Liste non sauvegardée"));
            alert.setHeaderText(TR.trO("Impossible de sauvegarder la liste"));
            alert.setContentText(TR.trO("Il n'y a aucun élément à enregistrer"));
            alert.show();
            return;
        }
        TextTreeSection.lists.put(listName, list);

        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.trO("Liste sauvegardée"));
        alert.setHeaderText(TR.trO("La liste a bien été sauvegardée !"));
        alert.setContentText(TR.trO("La liste pourra être chargée via le bouton de liste"));
        alert.show();
        ListsManager.setupMenus();
    }

    public void deleteList(String listName){
        TextTreeSection.lists.remove(listName);

        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.trO("Liste supprimée"));
        alert.setHeaderText(TR.trO("La liste") + " \"" + listName + "\" " + TR.trO("a bien été supprimé."));
        alert.show();
        ListsManager.setupMenus();
    }

}
