package fr.themsou.panel.leftBar.texts.TreeViewSections;

import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.leftBar.texts.TextTreeView;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;

public class TextTreeOnFile extends TextTreeSection {

    public TextTreeOnFile(){
        super(TR.tr("Éléments sur ce document"), TextTreeSection.ONFILE_TYPE);
        setupGraphics();
    }

    @Override
    public void setupSortManager(){
        sortManager.setup(sortCell.pane, TR.tr("Position"),
                TR.tr("Position"), TR.tr("Nom"),
                "\n",
                TR.tr("Police"), TR.tr("Taille"), TR.tr("Couleur"));
    }

    public void updateElementsList(){
        clearElements();

        // GET ALL ELEMENTS In THE DOCUMENT
        if(MainWindow.mainScreen.getStatus() == MainScreen.Status.OPEN){
            for(PageRenderer page : MainWindow.mainScreen.document.pages){
                for(int i = 0; i < page.getElements().size(); i++){
                    if(page.getElements().get(i) instanceof TextElement){
                        TextElement element = (TextElement) page.getElements().get(i);
                        getChildren().add(element.toNoDisplayTextElement(TextTreeSection.ONFILE_TYPE, true));
                    }
                }
            }
        }
        sortManager.simulateCall();
    }
    public void addElement(TextElement element){
        getChildren().add(element.toNoDisplayTextElement(TextTreeSection.ONFILE_TYPE, true));
        sortManager.simulateCall();
    }
    @Override
    public void removeElement(TextElement element){
        super.removeElement(element);
        MainWindow.lbTextTab.treeView.lastsSection.removeElement(element);
        MainWindow.lbTextTab.treeView.favoritesSection.removeElement(element);
    }

}
