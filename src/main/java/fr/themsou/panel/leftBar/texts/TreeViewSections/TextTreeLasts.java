package fr.themsou.panel.leftBar.texts.TreeViewSections;

import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.utils.TR;
import fr.themsou.utils.sort.Sorter;
import java.util.ArrayList;
import java.util.List;

public class TextTreeLasts extends TextTreeSection {

    public TextTreeLasts(){
        super(TR.tr("Éléments Précédents"), TextTreeSection.LAST_TYPE);
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
    public void addElement(TextTreeItem element){
        if(!getChildren().contains(element)){
            getChildren().add(element);
            if(getChildren().size() > 50){

                // SORT BY DATE
                List<TextTreeItem> toSort = new ArrayList<>();
                for(int i = 0; i < getChildren().size(); i++){
                    if(getChildren().get(i) instanceof TextTreeItem){
                        toSort.add((TextTreeItem) getChildren().get(i));
                    }
                }
                List<TextTreeItem> sorted = Sorter.sortElementsByDate(toSort, false);

                // GET THE LESS USE IN THE 20 OLDER
                toSort = new ArrayList<>();
                for(int i = 0; i < 20; i++){
                    toSort.add(sorted.get(i));
                }
                sorted = Sorter.sortElementsByUtils(toSort, false);
                removeElement(sorted.get(0));
            }
            sortManager.simulateCall();
        }
    }
}
