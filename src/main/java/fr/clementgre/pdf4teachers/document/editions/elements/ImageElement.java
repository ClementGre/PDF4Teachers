package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.gallery.GalleryManager;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.interfaces.ReturnCallBack;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

public class ImageElement extends GraphicElement{
    
    // imageId
    
    private Image image;
    private ImageView imageView;
    private StringProperty imageId = new SimpleStringProperty();
    
    public ImageElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode, RotateMode rotateMode, String imageId){
        super(x, y, pageNumber, hasPage, width, height, repeatMode, resizeMode, rotateMode);
        this.imageId.set(imageId);
        
        updateImage(true);
    
        if(hasPage && getPage() != null){
            imageView = new ImageView(image);
            imageView.fitWidthProperty().bind(widthProperty());
            imageView.fitHeightProperty().bind(heightProperty());
            
            setupGeneral(imageView);
        }
    }
    
    // SETUP / EVENT CALL BACK
    
    @Override
    protected void setupBindings(){
        imageId.addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
            updateImage(false);
        });
        
        
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
        super.selectPartial();
        
    }
    
    @Override
    public void addedToDocument(boolean silent){
    
    }
    
    @Override
    public void removedFromDocument(boolean silent){
    
    }
    
    // READER AND WRITERS
    
    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("imageId", getImageId());
        
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
        
        RepeatMode repeatMode = RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        ResizeMode resizeMode = ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        RotateMode rotateMode = RotateMode.valueOf(Config.getString(data, "rotateMode"));
        
        return new ImageElement(x, y, page, hasPage, width, height, repeatMode, resizeMode, rotateMode, imageId);
    }
    
    // SPECIFIC METHODS
    
    public void updateImage(boolean checkAutoSize){
        generateImageAsync(() -> {
            imageView.setImage(image);
            
            if(checkAutoSize && getRealWidth() == 0 && getRealHeight() == 0){
                checkLocation(getRealX() * getPage().getWidth() / GRID_WIDTH, getRealY() * getPage().getHeight() / GRID_HEIGHT,
                        image.getWidth() * getPage().getWidth() / GRID_WIDTH, image.getHeight() * getPage().getHeight() / GRID_HEIGHT, false);
            }
        });
    }
    
    private void generateImageAsync(CallBack callBack){
        new Thread(() -> {
    
            File file = new File(getImageId());
            if(file.exists() && GalleryManager.isAcceptableImage(file.getName())){
                try{
                    image = new Image("file:///" + getImageId());
                }catch(Exception e){
                    e.printStackTrace();
                    image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/painttab/not_found.png")));
                }
                if(image.getWidth() == 0)
                    image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/painttab/not_found.png")));
            }else{
                image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/painttab/not_found.png")));
            }
    
            Platform.runLater(callBack::call);
        }, "ImageElement Renderer").start();
    }
    
    @Override
    public Element clone(){
        return null;
    }
    
    // GETTER/SETTER
    
    
    public String getImageId(){
        return imageId.get();
    }
    public StringProperty imageIdProperty(){
        return imageId;
    }
    public void setImageId(String imageId){
        this.imageId.set(imageId);
    }
    
}
