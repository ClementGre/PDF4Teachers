package fr.themsou.panel.leftBar.texts;

import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.scene.control.*;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class ListsManager {

    public Button loadListBtn = new Button();
    public Button saveListBtn = new Button();

    ContextMenu menu = new ContextMenu();

    LBTextTab textTab;

    public ListsManager(LBTextTab textTab){
        this.textTab = textTab;

        loadListBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/list.png") +"", 18, 18));
        loadListBtn.setTooltip(Builders.genToolTip(TR.tr("Afficher les listes d'éléments enregistrés")));
        saveListBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/save.png") +"", 22, 22));
        saveListBtn.setTooltip(Builders.genToolTip(TR.tr("Sauvegarder les éléments favoris en tant qu'une nouvelle liste")));

        Builders.setPosition(loadListBtn, 0, 0, 30, 30, true);
        Builders.setPosition(saveListBtn, 0, 0, 30, 30, true);

        setupMenu();
        loadListBtn.setOnMouseClicked(e -> {
            menu.show(loadListBtn, e.getScreenX(), e.getScreenY());
        });

        saveListBtn.setOnAction(event -> {

            TextInputDialog alert = new TextInputDialog(TR.tr("Nouvelle liste"));
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            Builders.secureAlert(alert);

            alert.setTitle(TR.tr("Enregistrer les éléments favoris"));
            alert.setHeaderText(TR.tr("Vous allez enregistrer les éléments favoris dans une nouvelle liste."));
            alert.setContentText(TR.tr("Donner un nom à votre liste"));

            Optional<String> result = alert.showAndWait();
            if(result.isPresent()){
                if(!result.get().isEmpty()){
                    if(textTab.favoriteLists.containsKey(result.get())){
                        Alert alert2 = new Alert(Alert.AlertType.WARNING);
                        new JMetro(alert2.getDialogPane(), Style.LIGHT);
                        Builders.secureAlert(alert2);
                        alert2.setTitle(TR.tr("Liste déjà existante"));
                        alert2.setHeaderText(TR.tr("Une liste du même nom existe déjà."));
                        alert2.setContentText(TR.tr("Choisissez une action."));

                        ButtonType rename = new ButtonType(TR.tr("Renomer"), ButtonBar.ButtonData.NO);
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

    public void setupMenu(){
        menu.getItems().clear();
        menu.setMinWidth(400);
        menu.setPrefWidth(400);

        if(textTab.favoriteLists.size() >= 1){
            for(Map.Entry<String, ArrayList<TextListItem>> list : textTab.favoriteLists.entrySet()){
                MenuItem menuItem = new MenuItem(list.getKey());
                menu.getItems().add(menuItem);
                menuItem.setOnAction(event -> {

                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    new JMetro(alert.getDialogPane(), Style.LIGHT);
                    Builders.secureAlert(alert);
                    alert.setTitle(TR.tr("Actions de listes"));
                    alert.setHeaderText(TR.tr("Choisissez une action a effectuer avec la liste d'éléments.") + "\n" + TR.tr("Ces actions sont irréversibles."));
                    alert.setContentText(TR.tr("- Charger remplacera la liste des éléments favoris par celle ci") + "\n" + TR.tr("- Supprimer supprimera la liste de la base de donnée"));

                    ButtonType cancel = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType load = new ButtonType(TR.tr("Charger"), ButtonBar.ButtonData.OK_DONE);
                    ButtonType delete = new ButtonType(TR.tr("Supprimer"), ButtonBar.ButtonData.OTHER);
                    alert.getButtonTypes().setAll(cancel, load, delete);

                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.get() == load) loadList(list.getValue());
                    else if(result.get() == delete) deleteList(list.getKey());

                });
            }
        }else{
            menu.getItems().add(new MenuItem(TR.tr("Aucune liste sauvegardée")));
        }
    }

    public void loadList(ArrayList<TextListItem> items){

        textTab.clearSavedFavoritesElements();

        for(TextListItem item : items){
            textTab.favoritesText.getChildren().add(item.toTextTreeItem(TextTreeItem.FAVORITE_TYPE));
        }
        textTab.favoritesTextSortManager.simulateCall();

    }

    public void saveList(String listName){

        textTab.favoriteLists.remove(listName);
        for(TreeItem<String> item : textTab.favoritesText.getChildren()){
            if(item instanceof TextTreeItem){
                ArrayList<TextListItem> list = MainWindow.lbTextTab.favoriteLists.containsKey(listName) ? MainWindow.lbTextTab.favoriteLists.get(listName) : new ArrayList<>();
                list.add(((TextTreeItem) item).toTextItem());
                MainWindow.lbTextTab.favoriteLists.put(listName, list);
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        new JMetro(alert.getDialogPane(), Style.LIGHT);
        Builders.secureAlert(alert);
        alert.setTitle(TR.tr("Liste sauvegardée"));

        alert.setHeaderText(TR.tr("La liste a bien été sauvegardée !"));
        alert.setContentText(TR.tr("La liste pourra être chargée via le bouton importer"));
        alert.show();

        setupMenu();
    }

    public void deleteList(String listName){

        textTab.favoriteLists.remove(listName);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        new JMetro(alert.getDialogPane(), Style.LIGHT);
        Builders.secureAlert(alert);
        alert.setTitle(TR.tr("Liste supprimée"));

        alert.setHeaderText(TR.tr("La liste") + " \"" + listName + "\" " + TR.tr("a bien été supprimé."));
        alert.show();

        setupMenu();

    }

}
