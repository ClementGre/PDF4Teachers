package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ListPane;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GalleryWindow extends Stage{
    
    private final VBox root = new VBox();
    
    private final HBox settings = new HBox();
    private final GridPane sortPanel = new GridPane();
    private final ComboBox<String> filter = new ComboBox<>();
    private final Button editSources = new Button();
    private final EditSourcesPopOver editSourcesPopOver = new EditSourcesPopOver(this);
    
    private final GridView<ImageGridElement> list = new GridView<>();
    
    private SortManager sortManager;
    
    
    public GalleryWindow(){
        
        Scene scene = new Scene(root, 545, Main.SCREEN_BOUNDS.getHeight() - 100 >= 675 ? 675 : Main.SCREEN_BOUNDS.getHeight() - 100);
    
        initOwner(Main.window);
        initModality(Modality.APPLICATION_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        setWidth(1000);
        setHeight(600);
        setTitle(TR.tr("galleryWindow.title"));
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);
    
        setOnCloseRequest(e -> editSourcesPopOver.hide(Duration.ZERO));
        
        setup();
        show();
    }
    
    private void setup(){
        setupSettings();
        
        root.getChildren().addAll(settings, list);
        loadImages();
    }
    
    
    public int cellSize = 200;
    private static final int IMAGE_W = 500;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    class ImageGridCell extends GridCell<ImageGridElement>{
        
        private final ImageView imageView;
    
        private final DropShadow shadow = new DropShadow();
        public static final int PADDING = 2;
        
        public ImageGridCell(){
            this.imageView = new ImageView();
    
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
    
            imageView.fitWidthProperty().bind(widthProperty().subtract(2*PADDING));
            imageView.fitHeightProperty().bind(heightProperty().subtract(2*PADDING));
    
            shadow.setColor(Color.web("#0078d7"));
            shadow.setSpread(.90);
            shadow.setOffsetY(0);
            shadow.setOffsetX(0);
            shadow.setRadius(0);
            setEffect(shadow);
    
            setOnMouseEntered((e) -> {
                shadow.setRadius(2);
            });
            setOnMouseExited((e) -> {
                shadow.setRadius(0);
            });
            
        }
    
        @Override
        protected void updateItem(ImageGridElement item, boolean empty) {
            super.updateItem(item, empty);
        
            if(empty){
                setGraphic(null);
                setOnMouseClicked(null);
            }else{
               
                if(item.getImage() == null){
                    if(!item.isRendering()){
                        item.setRendering(true);
                        executor.submit(() -> loadImage(item, () -> item.setRendering(false)));
                    }
                }
                imageView.imageProperty().bind(item.imageProperty());
    
                setGraphic(imageView);
                setOnMouseClicked((e) -> {
                    System.out.println("clicked");
                });
            }
        }
    }
    
    public void loadImages(){
        List<ImageGridElement> images = getImages();
        
        for(ImageGridElement image : images){
            boolean match = false;
            for(ImageGridElement image2 : list.getItems()){
                if(image2.getImageId().equals(image.getImageId())){
                    match = true;  break;
                }
            }
            if(!match) list.getItems().add(image);
        }
    }
    
    private void setupSettings(){
    
        sortManager = new SortManager((sortType, order) -> {
        
        }, null);
        sortManager.setup(sortPanel, ListPane.SORT_USE, ListPane.SORT_USE, ListPane.SORT_TIME);
        
        editSources.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.PENCIL_EDIT, "black", 0, 20, 20, ImageUtils.defaultDarkColorAdjust));
        editSources.setOnAction((e) -> editSourcesPopOver.show(editSources));
    
        PaneUtils.setHBoxPosition(sortPanel, 0, 30, 0);
        PaneUtils.setHBoxPosition(filter, 0, 30, 0);
        PaneUtils.setHBoxPosition(editSources, 30, 30, 0);
        
        settings.setSpacing(10);
        settings.setPadding(new Insets(0, 20, 0, 20));
        settings.getChildren().addAll(sortPanel, new HBoxSpacer(), filter, editSources);
    }
    
    private List<ImageGridElement> getImages(){
        return GalleryManager.getImages().stream().map((img) -> new ImageGridElement(img.getImageId())).collect(Collectors.toList());
    }
    
    private void loadImage(ImageGridElement image, CallBack callBack){
        if(image == null) return;

        try{
            image.setImage(getImageCropped(image.getImageId()));
        }catch(IOException e){ e.printStackTrace(); }

        Platform.runLater(callBack::call);
    }
    
    private Image getImageCropped(String imageId) throws IOException{
        
        BufferedImage cropped = new BufferedImage(IMAGE_W, IMAGE_W, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = cropped.createGraphics();
        graphics2D.drawImage(
                getImageSquare(ImageIO.read(new FileInputStream(imageId))),
                0, 0, IMAGE_W, IMAGE_W, null);
        
        graphics2D.dispose();
        
        return SwingFXUtils.toFXImage(cropped, null);
    }
    private BufferedImage getImageSquare(BufferedImage image){
        int w = image.getWidth();
        int h = image.getHeight();
        if(w > h){
            int sideMargin = (w - h) / 2;
            return image.getSubimage(sideMargin, 0, h, h);
        }else{
            int sideMargin = (h - w) / 2;
            return image.getSubimage(0, sideMargin, w, w);
        }
    }
    
}
