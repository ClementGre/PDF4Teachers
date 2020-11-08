package fr.themsou.document.editions.elements;

import fr.themsou.windows.MainWindow;
import fr.themsou.yaml.Config;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ImageElement extends GraphicElement{

    // imageId

    public ImageElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, String imageId) {
        super(x, y, pageNumber, hasPage, width, height);



    }

    // SETUP / EVENT CALL BACK

    @Override
    protected void setupBindings(){

    }
    @Override
    protected void onMouseRelease(){

    }
    @Override
    protected void setupMenu(){

    }

    // ACTIONS

    @Override
    public void select(){

    }
    @Override
    public void addedToDocument(boolean silent) {

    }
    @Override
    public void removedFromDocument(boolean silent) {

    }

    // READER AND WRITERS

    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("imageId", "Var does not exist yet");

        return data;
    }
    public static void readYAMLDataAndCreate(HashMap<String, Object> data, int page){
        ImageElement element = readYAMLDataAndGive(data, true, page);
        if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
            MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElement(element, false);
    }
    public static ImageElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage, int page){

        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        String imageId = Config.getString(data, "imageId");

        return new ImageElement(x, y, page, hasPage, width, height, imageId);
    }

    // SPECIFIC METHODS

    @Override
    public float getAlwaysHeight() {
        return 0;
    }
    @Override
    public Element clone() {
        return null;
    }

}
