package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import java.util.ArrayList;
import java.util.List;

public class ImageGridView extends ShapesGridView<ImageGridElement>{
    
    private final int imageRenderSize;
    
    public ImageGridView(boolean defineCellSizeAsRowNumber, int cellSize, int imageRenderSize){
        super(defineCellSizeAsRowNumber, cellSize);
        this.imageRenderSize = imageRenderSize;
        
    }
    
    @Override
    protected void setup(){
        setCellFactory(param -> new ImageGridCell(this));
        super.setup();
    }
    
    @Override
    protected void sort(String sortType, boolean order){
        int multiple = (order ? 1 : -1);
        if(SORT_FILE_EDIT_TIME.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareTimeWith(o2) * multiple);
        }else if(SORT_USE.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareUseWith(o2) * multiple);
        }else if(SORT_NAME.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareNameWith(o2) * multiple);
        }else if(SORT_SIZE.equals(sortType)){
            getItems().sort((o1, o2) -> o1.compareSizeWith(o2) * multiple);
        }else{ // SORT_FOLDER (Default)
            getItems().sort((o1, o2) -> o1.compareDirectoryWith(o2) * multiple);
        }
    }
    
    public void editImages(List<ImageGridElement> newImagesList){
        List<ImageGridElement> actualImages = getItems();
    
        // Remove images that are not anymore into the list
        List<ImageGridElement> toRemove = new ArrayList<>();
        
        for(ImageGridElement image : actualImages){
            if(!newImagesList.contains(image)) toRemove.add(image);
        }
        getItems().removeAll(toRemove);
        
        
        // Add images that was added to the list
        List<ImageGridElement> toAdd = new ArrayList<>();
        
        for(ImageGridElement image : newImagesList){
            if(!actualImages.contains(image)) toAdd.add(image);
        }
        getItems().addAll(toAdd);
        
        sort();
    }
    
    public int getImageRenderSize(){
        return imageRenderSize;
    }
}
