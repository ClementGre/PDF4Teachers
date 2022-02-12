package fr.clementgre.pdf4teachers.document.editions.undoEngine.pages;

import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoAction;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import org.apache.pdfbox.pdmodel.PDPage;

public class PageAddRemoveUndoAction extends UndoAction {
    
    private final int pageIndex;
    private final PDPage page;
    private boolean deleted;
    private final boolean originallyDeleted;
    
    public PageAddRemoveUndoAction(UType undoType, int pageIndex, PDPage page, boolean deleted){
        super(undoType);
        this.pageIndex = pageIndex;
        this.page = page;
        this.deleted = deleted;
        this.originallyDeleted = deleted;
    }
    @Override
    public boolean undoAndInvert(){
        if(MainWindow.mainScreen.hasDocument(false)){
            if(deleted){
                MainWindow.mainScreen.document.pdfPagesRender.editor.addPage(page, pageIndex);
            }else{
                MainWindow.mainScreen.document.pdfPagesRender.editor.deletePage(MainWindow.mainScreen.document.getPage(pageIndex));
            }
    
            // invert
            deleted = !deleted;
    
            return true;
        }
        return false;
    }
    
    @Override
    public String toString(){
        if(originallyDeleted){
            return TR.tr("actions.deletePage", pageIndex+1);
        }else{
            return TR.tr("actions.addPage", pageIndex+1);
        }
        
    }
}
