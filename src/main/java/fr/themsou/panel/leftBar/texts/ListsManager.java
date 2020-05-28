package fr.themsou.panel.leftBar.texts;

import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeFavorites;
import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeSection;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.utils.style.StyleManager;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class ListsManager {

    public Button loadListBtn = new Button();
    public Button saveListBtn = new Button();

    ContextMenu menu = new ContextMenu();

    TextTreeFavorites favoritesSection;

    public ListsManager(TextTreeFavorites favoritesSection){
        this.favoritesSection = favoritesSection;

        loadListBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/list.png") +"", 18, 18));
        loadListBtn.setTooltip(Builders.genToolTip(TR.tr("Afficher les listes d'éléments enregistrés")));
        saveListBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/save.png") +"", 22, 22));
        saveListBtn.setTooltip(Builders.genToolTip(TR.tr("Sauvegarder les éléments favoris en tant qu'une nouvelle liste")));

        Builders.setPosition(loadListBtn, 0, 0, 30, 30, true);
        Builders.setPosition(saveListBtn, 0, 0, 30, 30, true);

        updateGraphics();

        setupMenu();
        loadListBtn.setOnMouseClicked(e -> {
            menu.show(loadListBtn, e.getScreenX(), e.getScreenY());
        });

        saveListBtn.setOnAction(event -> {

            TextInputDialog alert = new TextInputDialog(TR.tr("Nouvelle liste"));
            Builders.setupDialog(alert);

            alert.setTitle(TR.tr("Enregistrer les éléments favoris"));
            alert.setHeaderText(TR.tr("Vous allez enregistrer les éléments favoris dans une nouvelle liste."));
            alert.setContentText(TR.tr("Donner un nom à votre liste"));

            Optional<String> result = alert.showAndWait();
            if(result.isPresent()){
                if(!result.get().isEmpty()){
                    if(favoritesSection.favoriteLists.containsKey(result.get())){
                        Alert alert2 = Builders.getAlert(Alert.AlertType.WARNING, TR.tr("Liste déjà existante"));
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

    public void setupMenu(){
        menu.getItems().clear();
        menu.setMinWidth(400);
        menu.setPrefWidth(400);

        if(favoritesSection.favoriteLists.size() >= 1){
            for(Map.Entry<String, ArrayList<TextListItem>> list : favoritesSection.favoriteLists.entrySet()){
                MenuItem menuItem = new MenuItem(list.getKey());
                menu.getItems().add(menuItem);
                menuItem.setOnAction(event -> {

                    Alert alert = Builders.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Actions de listes"));
                    alert.setHeaderText(TR.tr("Choisissez une action a effectuer avec la liste d'éléments.") + "\n" + TR.tr("Ces actions sont irréversibles."));
                    alert.setContentText(TR.tr("- Vider et charger remplacera la liste des éléments favoris par celle ci") + "\n" +
                                         TR.tr("- Charger ajoutera cette liste d'éléments à la liste des éléments favoris") + "\n" +
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

    public void loadList(ArrayList<TextListItem> items, boolean replace){

        if(replace) favoritesSection.clearElements();

        for(TextListItem item : items){
            favoritesSection.getChildren().add(item.toTextTreeItem(TextTreeSection.FAVORITE_TYPE));
        }
        favoritesSection.sortManager.simulateCall();

    }

    public void saveList(String listName){

        favoritesSection.favoriteLists.remove(listName);
        for(Object item : favoritesSection.getChildren()){
            if(item instanceof TextTreeItem){
                ArrayList<TextListItem> list = favoritesSection.favoriteLists.containsKey(listName) ? favoritesSection.favoriteLists.get(listName) : new ArrayList<>();
                list.add(((TextTreeItem) item).toTextItem());
                favoritesSection.favoriteLists.put(listName, list);
            }
        }
        Alert alert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Liste sauvegardée"));
        alert.setHeaderText(TR.tr("La liste a bien été sauvegardée !"));
        alert.setContentText(TR.tr("La liste pourra être chargée via le bouton de liste"));
        alert.show();

        setupMenu();
    }

    public void deleteList(String listName){

        favoritesSection.favoriteLists.remove(listName);

        Alert alert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Liste supprimée"));

        alert.setHeaderText(TR.tr("La liste") + " \"" + listName + "\" " + TR.tr("a bien été supprimé."));
        alert.show();

        setupMenu();

    }

}
