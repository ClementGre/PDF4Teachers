package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguagesUpdater;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageData;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageLambdaData;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ListPane;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.cell.ImageGridCell;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class GalleryWindow extends Stage{
    
    private final VBox root = new VBox();
    
    private final HBox settings = new HBox();
    private final GridPane sortPanel = new GridPane();
    private final ComboBox<String> filter = new ComboBox<>();
    private final Button editSources = new Button();
    private final EditSourcesPopOver editSourcesPopOver = new EditSourcesPopOver(this);
    
    private final GridView<ImageListElement> list = new GridView<>();
    
    private SortManager sortManager;
    
    
    private static final int CELL_SIZE = 200;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
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
    
        list.setCache(true);
        list.setCacheHint(CacheHint.SPEED);
        list.setCellWidth(CELL_SIZE);
        list.setCellHeight(CELL_SIZE);
        list.setHorizontalCellSpacing(10);
        list.setVerticalCellSpacing(10);
        
        list.setCellFactory(param -> new ImageGridCell());
        
        root.getChildren().addAll(settings, list);
        loadImages();
    }
    
    static class ImageGridCell extends GridCell<ImageListElement>{
        
        private final ImageView imageView;
        public ImageGridCell(){
            this.imageView = new ImageView();
        }
    
        @Override protected void updateItem(ImageListElement item, boolean empty) {
            super.updateItem(item, empty);
        
            if (empty) {
                setGraphic(null);
            } else {
                imageView.setPreserveRatio(true);
                imageView.setSmooth(false);
                imageView.setFitWidth(CELL_SIZE);
                imageView.setFitHeight(CELL_SIZE);
                setMaxWidth(CELL_SIZE);
                setMaxHeight(CELL_SIZE);
                
                imageView.setImage(item.getImage());
                setGraphic(imageView);
            }
        }
    }
    
    public void loadImages(){
        executor.submit(this::addImagesToGrid);
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
    
    private List<ImageListElement> getImages(){
        return GalleryManager.getImages().stream().map((img) -> new ImageListElement(img.getImageId())).collect(Collectors.toList());
    }
    
    
    private static final ImageListElement POISON_PILL = createFakeImage(1, 1);
    
    private void addImagesToGrid() {
        List<ImageListElement> images = getImages();
        final Queue<ImageListElement> imageQueue = new ConcurrentLinkedQueue<>();
        
        executor.submit(() -> deliverImagesToGrid(imageQueue));
        for(ImageListElement image : images) {
            // (In the real application, get a list of image filenames, read each image's thumbnail, generating it if needed.
            // (In this minimal reproducible code, we'll just create a new dummy image for each ImageView)
            imageQueue.add(image);
        }
        // Add poison image to signal the end of the queue.
        imageQueue.add(POISON_PILL);
    }
    
    private void deliverImagesToGrid(Queue<ImageListElement> imageQueue) {
        try {
            Semaphore semaphore = new Semaphore(1);
            semaphore.acquire(); // Get the one and only permit
            boolean done = false;
            while(!done){
                
                List<ImageListElement> imagesToAdd = new ArrayList<>();
                for (int i = 0; i < 1000; i++) {
                    final ImageListElement image = imageQueue.poll();
                    if(image == null) break; // Queue is now empty, so quit adding any to the list
                    else if (image.getImage() == POISON_PILL.getImage()) done = true;
                    else{
                        try{
                            image.setImage(new Image(new FileInputStream(image.getImageId())));
                        }catch(FileNotFoundException e){ e.printStackTrace(); }
                        imagesToAdd.add(image);
                    }
                }
                
                if(imagesToAdd.size() > 0){
                    Platform.runLater(() -> {
                        try{
                            list.getItems().addAll(imagesToAdd);
                        }finally{
                            semaphore.release();
                        }
                    });
                    // Block until the items queued up via Platform.runLater() have been processed by the UI thread and release() has been called.
                    semaphore.acquire();
                }
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // Create an image with a bunch of rectangles in it just to have something to display.
    private static ImageListElement createFakeImage(int imageIndex, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        for (int i = 1; i < size; i ++) {
            g.setColor(new Color(i * imageIndex % 256, i * 2 * (imageIndex + 40) % 256, i * 3 * (imageIndex + 60) % 256));
            g.drawRect(i, i, size - i * 2, size - i * 2);
        }
        return new ImageListElement("", SwingFXUtils.toFXImage(image, null));
    }
    
}
