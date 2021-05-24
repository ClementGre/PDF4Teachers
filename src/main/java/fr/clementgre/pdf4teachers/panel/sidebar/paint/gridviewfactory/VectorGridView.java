package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import javafx.scene.control.Slider;
import java.util.List;

public class VectorGridView extends ShapesGridView<VectorGridElement>{
    
    public VectorGridView(Slider zoomSlider){
        super(true, zoomSlider);
    }
    
    @Override
    protected void setup(){
        setCellFactory(param -> new VectorGridCell(this));
        super.setup();
    }
    
    @Override
    protected void sort(String sortType, boolean order){
        int multiple = (order ? 1 : -1);
        if(SORT_USE.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareUseWith(o2) * multiple);
        }else if(SORT_LAST_USE.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareLastUseTimeWith(o2) * multiple);
        }
    }
    
    @Override
    protected List<VectorGridElement> filter(List<VectorGridElement> items){
        return items;
    }
}
