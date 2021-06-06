package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import javafx.scene.control.Slider;
import java.util.List;

public class VectorGridView extends ShapesGridView<VectorGridElement>{
    
    private final boolean favorite;
    private final boolean hasContextMenu;
    public VectorGridView(Slider zoomSlider, boolean favorite, boolean hasContextMenu){
        super(true, zoomSlider);
        this.favorite = favorite;
        this.hasContextMenu = hasContextMenu;
    }
    
    @Override
    protected void setup(){
        setCellFactory(param -> new VectorGridCell(favorite, hasContextMenu));
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
    public void resetUseData(){
        for(VectorGridElement element : getAllItems()){
            element.resetUseData();
        }
        getSortManager().simulateCall();
    }
    
    @Override
    protected List<VectorGridElement> filter(List<VectorGridElement> items){
        return items;
    }
    
    public void onThemeChanged(){
        getItems().forEach(VectorGridElement::updateSVGSpecs);
    }
}
