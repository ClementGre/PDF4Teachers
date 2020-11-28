package fr.clementgre.pdf4teachers.panel.leftBar.texts;

import fr.clementgre.pdf4teachers.panel.leftBar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
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

        loadListBtn.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/TextTab/list.png") +"", 18, 18));
        loadListBtn.setTooltip(PaneUtils.genToolTip(TR.tr("Afficher les listes d'éléments enregistrés")));
        saveListBtn.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/TextTab/save.png") +"", 22, 22));
        saveListBtn.setTooltip(PaneUtils.genToolTip(TR.tr("Sauvegarder les éléments favoris en tant qu'une nouvelle liste")));

        PaneUtils.setPosition(loadListBtn, 0, 0, 30, 30, true);
        PaneUtils.setPosition(saveListBtn, 0, 0, 30, 30, true);

        updateGraphics();

        setupMenu();
        loadListBtn.setOnMouseClicked(e -> {
            menu.show(loadListBtn, e.getScreenX(), e.getScreenY());
        });

        saveListBtn.setOnAction(event -> {
            TextInputDialog alert = new TextInputDialog(TR.tr("Nouvelle liste"));
            DialogBuilder.setupDialog(alert);

            alert.setTitle(TR.tr("Enregistrer les éléments de cette catégorie"));
            alert.setHeaderText(TR.tr("Vous allez enregistrer les éléments de cette catégorie dans une nouvelle liste."));
            alert.setContentText(TR.tr("Donner un nom à votre liste"));

            Optional<String> result = alert.showAndWait();
            if(result.isPresent()){
                if(!result.get().isEmpty()){
                    if(TextTreeSection.lists.containsKey(result.get())){
                        Alert alert2 = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("Liste déjà existante"));
                        alert2.setHeaderText(TR.tr("Une liste du même nom existe déjà."));
                        alert2.setContentText(TR.tr("Choisissez une action."));

                        ButtonType rename = new ButtonType(TR.tr("Renommer"), ButtonBar.ButtonData.NO);
                        ButtonType erase = new ButtonType(TR.tr("Écraser"), ButtonBar.ButtonData.APPLY);
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

                    Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Actions de listes"));
                    alert.setHeaderText(TR.tr("Choisissez une action a effectuer avec la liste d'éléments.") + "\n" + TR.tr("Ces actions sont irréversibles."));
                    alert.setContentText(TR.tr("- Vider et charger remplacera la liste des éléments favoris/précédents par celle ci") + "\n" +
                                         TR.tr("- Charger ajoutera cette liste d'éléments à la liste des éléments favoris/précédents") + "\n" +
                                         TR.tr("- Supprimer supprimera la liste de la base de donnée"));

                    ButtonType cancel = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType load = new ButtonType(TR.tr("Charger"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType loadReplace = new ButtonType(TR.tr("Vider et charger"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType delete = new ButtonType(TR.tr("Supprimer"), ButtonBar.ButtonData.OTHER);
                    alert.getButtonTypes().setAll(cancel, loadReplace, load, delete);

                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.get() == load) loadList(list.getValue(), false);
                    else if(result.get() == loadReplace) loadList(list.getValue(), true);
                    else if(result.get() == delete) deleteList(list.getKey());

                });
            }
        }else{
            menu.getItems().add(new MenuItem(TR.tr("Aucune liste sauvegardée")));
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
            Alert alert = DialogBuilder.getAlert(Alert.AlertType.ERROR, TR.tr("Liste non sauvegardée"));
            alert.setHeaderText(TR.tr("Impossible de sauvegarder la liste"));
            alert.setContentText(TR.tr("Il n'y a aucun élément à enregistrer"));
            alert.show();
            return;
        }
        TextTreeSection.lists.put(listName, list);

        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Liste sauvegardée"));
        alert.setHeaderText(TR.tr("La liste a bien été sauvegardée !"));
        alert.setContentText(TR.tr("La liste pourra être chargée via le bouton de liste"));
        alert.show();
        ListsManager.setupMenus();
    }

    public void deleteList(String listName){
        TextTreeSection.lists.remove(listName);

        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Liste supprimée"));
        alert.setHeaderText(TR.tr("La liste") + " \"" + listName + "\" " + TR.tr("a bien été supprimé."));
        alert.show();
        ListsManager.setupMenus();
    }

}
