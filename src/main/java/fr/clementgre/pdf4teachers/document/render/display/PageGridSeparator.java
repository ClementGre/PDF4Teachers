package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class PageGridSeparator extends Pane {
    
    private final Timeline timeline = new Timeline(60);
    
    private final PageRenderer page;
    private final boolean before;
    public PageGridSeparator(PageRenderer page, boolean before){
        this.page = page;
        this.before = before;
    
        if(before){
            translateXProperty().bind(page.translateXProperty().subtract(PageRenderer.PAGE_MARGIN_GRID));
        }else{
            translateXProperty().bind(page.translateXProperty().add(PageRenderer.PAGE_WIDTH));
        }
        translateYProperty().bind(page.translateYProperty());
        setPrefWidth(PageRenderer.PAGE_MARGIN_GRID);
        prefHeightProperty().bind(page.heightProperty());
        
        
        setOnMouseEntered(event -> show(true));
        setOnMouseExited(event -> fadeOut());
        setOpacity(0);
        
        MainWindow.mainScreen.pane.getChildren().add(this);
    }
    
    public void remove(){
        translateXProperty().unbind();
        translateYProperty().unbind();
        prefHeightProperty().unbind();
        MainWindow.mainScreen.pane.getChildren().remove(this);
    }
    public void updateZoom(){
        if(getChildren().size() != 0 && getOpacity() != 0) show(false);
    }
    private void show(boolean fadeIn){
        getChildren().clear();
        
        double factor = (MainWindow.mainScreen.getZoomFactor() - .1) / 3 * 2.5 + .15;
        // .1 .4 -> .15 .4
    
        double circleRadius = PageRenderer.PAGE_MARGIN_GRID/1.8d * .4/factor;
        double circleMargin = circleRadius / 2;
    
        DropShadow shadow = new DropShadow();
        DropShadow shadow2 = new DropShadow();
        shadow.setRadius(10/factor);
        shadow.setSpread(.90);
        shadow.setColor(Color.web("#00da42"));
        shadow2.setColor(Color.web("#00da42"));
        shadow2.setRadius(5/factor);
    
        Line line = new Line(PageRenderer.PAGE_MARGIN_GRID/2f, 0f, PageRenderer.PAGE_MARGIN_GRID/2f, getHeight());
        line.setStroke(Color.web("#00da42"));
        line.setEffect(shadow);
        line.setStrokeWidth(.5 / factor);
    
        Circle circle = new Circle(PageRenderer.PAGE_MARGIN_GRID/2d, getHeight()/2, circleRadius);
        circle.setFill(Color.web("#00c63c"));
        circle.setEffect(shadow2);
        circle.setCursor(Cursor.HAND);
    
        Region plus = SVGPathIcons.generateImage(SVGPathIcons.PLUS, "#e5e5e5", (int) (2*circleRadius-circleMargin), (int) (2*circleRadius-circleMargin));
        plus.setLayoutX(PageRenderer.PAGE_MARGIN_GRID/2d - circleRadius + circleMargin/2);
        plus.setLayoutY(getHeight()/2 - circleRadius + circleMargin/2);
        plus.setMouseTransparent(true);
    
        getChildren().addAll(line, circle, plus);
        requestFocus();
        toFront();
    
        circle.setOnMouseEntered(e -> {
            circle.setFill(Color.web("#00e445"));
            plus.setStyle("-fx-background-color: white;");
        });
        circle.setOnMouseExited(e -> {
            circle.setFill(Color.web("#00c63c"));
            plus.setStyle("-fx-background-color: #e4e4e4;");
        });
    
        circle.setOnMouseClicked(e -> triggerMenu());
        
        if(fadeIn) fadeIn();
    }
    private void fadeIn(){
        timeline.stop();
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(100), new KeyValue(opacityProperty(), 1)));
        timeline.play();
        timeline.setOnFinished(null);
    }
    private void fadeOut(){
        timeline.stop();
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(100), new KeyValue(opacityProperty(), 0)));
        timeline.play();
        timeline.setOnFinished((e) -> getChildren().clear());
    }
    
    public void triggerMenu(){
        System.out.println("Menu triggered");
    }
}
