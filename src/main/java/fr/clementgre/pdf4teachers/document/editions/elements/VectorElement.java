package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.datasaving.Config;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class VectorElement extends GraphicElement{

    public VectorElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, Color fill, Color stroke, int strokeWidth, String path) {
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
        /*data.put("fill", fill.toString());
        data.put("stroke", stroke.toString());
        data.put("strokeWidth", strokeWidth);
        data.put("path", path);*/

        return data;
    }
    public static void readYAMLDataAndCreate(HashMap<String, Object> data, int page){
        VectorElement element = readYAMLDataAndGive(data, true, page);
        if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
            MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElement(element, false);
    }
    public static VectorElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage, int page){

        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        Color fill = Color.valueOf(Config.getString(data, "fill"));
        Color stroke = Color.valueOf(Config.getString(data, "stroke"));
        int strokeWidth = (int) Config.getLong(data, "strokeWidth");
        String path = Config.getString(data, "path");

        return new VectorElement(x, y, page, hasPage, width, height, fill, stroke, strokeWidth, path);
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
