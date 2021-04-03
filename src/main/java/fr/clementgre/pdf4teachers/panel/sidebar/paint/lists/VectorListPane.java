package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridElement;

public class VectorListPane extends ListPane<VectorGridElement>{
    
    
    public VectorListPane(){
        super(MainWindow.paintTab);
    }
    
    
    @Override
    protected void setupGraphics(){
        super.setupGraphics();
        
    }
    
    @Override
    public void updateGraphics(){
        //list.getSortManager().updateGraphics();
    }
    
    @Override
    public ShapesGridView<VectorGridElement> getList(){
        return null;
    }
    
}
