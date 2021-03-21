package fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.ListsManager;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.utils.sort.Sorter;

import java.util.ArrayList;
import java.util.List;

public class TextTreeLasts extends TextTreeSection{

    public ListsManager listsManager;

    public TextTreeLasts(){
        super(TR.tr("textTab.previousList.name"), LAST_TYPE);

        listsManager = new ListsManager(this);
        setupGraphics();
    }

    @Override
    public void setupSortManager(){
        sortManager.setup(sortCell.pane, TR.tr("sorting.sortType.addDate.short"),
                TR.tr("sorting.sortType.addDate.short"), TR.tr("sorting.sortType.name"), TR.tr("sorting.sortType.use"),
                "\n",
                TR.tr("sorting.sortType.fontFamily"), TR.tr("sorting.sortType.fontSize"), TR.tr("sorting.sortType.color"));
    }

    @Override
    public void setupGraphics(){
        super.setupGraphics();
        pane.getChildren().add(pane.getChildren().size() - 1, listsManager.saveListBtn);
        pane.getChildren().add(pane.getChildren().size() - 1, listsManager.loadListBtn);

    }

    @Override
    public void updateGraphics(){
        super.updateGraphics();
        listsManager.updateGraphics();
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
