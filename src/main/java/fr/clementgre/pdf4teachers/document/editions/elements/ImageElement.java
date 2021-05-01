package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.gallery.GalleryManager;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageData;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.io.File;
import java.util.*;

public class ImageElement extends GraphicElement{
    
    // imageId
    
    private boolean notFound = false;
    private Image image;
    private final StringProperty imageId = new SimpleStringProperty();
    private ImageData linkedImageData;
    
    public ImageElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode, String imageId, ImageData linkedImageData){
        super(x, y, pageNumber, hasPage, width, height, repeatMode, resizeMode);
        this.imageId.set(imageId);
        
        updateImage(true, hasPage);
    
        if(linkedImageData != null){
            this.linkedImageData = linkedImageData;
            realWidth.addListener((observable, oldValue, newValue) -> linkedImageData.setWidth(newValue.intValue()));
            realHeight.addListener((observable, oldValue, newValue) -> linkedImageData.setHeight(newValue.intValue()));
            this.repeatMode.addListener((observable, oldValue, newValue) -> linkedImageData.setRepeatMode(newValue));
            this.resizeMode.addListener((observable, oldValue, newValue) -> linkedImageData.setResizeMode(newValue));
            this.imageId.addListener((observable, oldValue, newValue) -> linkedImageData.setImageId(newValue));
        }
        
        if(hasPage && getPage() != null){
            setupGeneral();
        }
    }
    public ImageElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode, String imageId){
        super(x, y, pageNumber, hasPage, width, height, repeatMode, resizeMode);
        this.imageId.set(imageId);
        
        updateImage(true, hasPage);
        if(hasPage && getPage() != null){
            setupGeneral();
        }
    }
    
    public void initializePage(int pageNumber, double x, double y){
        this.pageNumber = pageNumber;
        setupGeneral();
        checkLocation(x, y, false);
        updateImage(false, true);
    }
    
    // SETUP / EVENT CALL BACK
    
    @Override
    protected void setupBindings(){
        super.setupBindings();
        imageId.addListener((observable, oldValue, newValue) -> {
            updateImage(false, true);
        });
        repeatMode.addListener((observable, oldValue, newValue) -> {
            updateBackground();
        });
        
    }
    
    @Override
    protected void onMouseRelease(){
        super.onMouseRelease();
    }
    
    @Override
    protected void setupMenu(){
        super.setupMenu();
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
        
        return new ImageElement(x, y, page, hasPage, width, height, repeatMode, resizeMode, imageId);
    }
    
    // SPECIFIC METHODS
    
    
    @Override
    public void incrementUsesAndLastUse(){
        if(getLinkedImageData() != null){
            getLinkedImageData().incrementUsesAndLastUse();
        }
    }
    
    @Override
    public double getRatio(){
        return image.getWidth() / image.getHeight();
    }
    
    public void updateImage(boolean checkAutoSize, boolean updateBackground){
        generateImageAsync(() -> {
            if(updateBackground) updateBackground();
            
            if(checkAutoSize && getRealWidth() == 0 && getRealHeight() == 0){
                defineSizeAuto();
            }
        });
    }
    
    @Override
    public void defineSizeAuto(){
        double imgWidth = image.getWidth();
        double imgHeight = image.getHeight();
        double width = Math.min(GRID_WIDTH/2, imgWidth);
        double height = imgHeight * width/imgWidth;
    
        checkLocation(getRealX() * getPage().getWidth() / GRID_WIDTH, getRealY() * getPage().getHeight() / GRID_HEIGHT,
                width * getPage().getWidth() / GRID_WIDTH, height * getPage().getHeight() / GRID_HEIGHT, false);
    }
    
    public void updateBackground(){
        BackgroundRepeat repeat = getRepeatMode() == RepeatMode.MULTIPLY ? BackgroundRepeat.REPEAT : BackgroundRepeat.NO_REPEAT;
        BackgroundSize size = new BackgroundSize(1, 1, true, true,
                getRepeatMode() == RepeatMode.MULTIPLY, getRepeatMode() == RepeatMode.CROP);
        BackgroundPosition position = BackgroundPosition.DEFAULT;
        
        if(notFound){
            repeat = BackgroundRepeat.NO_REPEAT;
            size = new BackgroundSize(1, 1, true, true, false, false);
        }
        
        setBackground(new Background(new BackgroundImage(image, repeat, repeat, position, size)));
    }
    
    private void generateImageAsync(CallBack callBack){
        new Thread(() -> {
    
            File file = new File(getImageId());
            if(file.exists() && GalleryManager.isAcceptableImage(file.getName())){
                try{
                    image = new Image("file:///" + getImageId());
                }catch(Exception e){
                    e.printStackTrace(); image = getNotFoundImage();
                }
                if(image.getWidth() == 0) image = getNotFoundImage();
                else notFound = false;
            }else image = getNotFoundImage();
            
            Platform.runLater(callBack::call);
        }, "ImageElement Renderer").start();
    }
    
    private Image getNotFoundImage(){
        notFound = true;
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/painttab/not_found.png")));
    }
    
    @Override
    public Element clone(){
        return new ImageElement(getRealX(), getRealY(), getPageNumber(), true, getRealWidth(), getRealHeight(), getRepeatMode(), getResizeMode(), getImageId());
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
    public ImageData getLinkedImageData(){
        return linkedImageData;
    }
    public void setLinkedImageData(ImageData linkedImageData){
        this.linkedImageData = linkedImageData;
    }
}
