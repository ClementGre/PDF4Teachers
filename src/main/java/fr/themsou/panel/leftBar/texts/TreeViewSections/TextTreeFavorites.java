package fr.themsou.panel.leftBar.texts.TreeViewSections;

import fr.themsou.panel.leftBar.texts.ListsManager;
import fr.themsou.panel.leftBar.texts.TextListItem;
import fr.themsou.utils.TR;
import java.util.ArrayList;
import java.util.HashMap;

public class TextTreeFavorites extends TextTreeSection {

    public HashMap<String, ArrayList<TextListItem>> favoriteLists = new HashMap<>();
    public ListsManager listsManager;

    public TextTreeFavorites(){
        super(TR.tr("Éléments Favoris"), TextTreeSection.FAVORITE_TYPE);

        listsManager = new ListsManager(this);
        setupGraphics();
    }

    @Override
    public void setupSortManager(){
        sortManager.setup(sortCell.pane, TR.tr("Ajout"),
                TR.tr("Ajout"), TR.tr("Nom"), TR.tr("Utilisation"),
                "\n",
                TR.tr("Police"), TR.tr("Taille"), TR.tr("Couleur"));
    }

    @Override
    public void setupGraphics(){
        super.setupGraphics();

        pane.getChildren().add(pane.getChildren().size()-1, listsManager.saveListBtn);
        pane.getChildren().add(pane.getChildren().size()-1, listsManager.loadListBtn);
    }

    @Override
    public void updateGraphics(){
        super.updateGraphics();
        listsManager.updateGraphics();
    }
}
