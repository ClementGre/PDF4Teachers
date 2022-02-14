package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class PageGridNumber extends Label {
    
    private final PageRenderer page;
    public PageGridNumber(PageRenderer page){
        this.page = page;
        
        setTextFill(Color.WHITE);
        
        layoutXProperty().bind(page.widthProperty().subtract(widthProperty()));
        layoutYProperty().bind(page.heightProperty().subtract(heightProperty()));
        
        updateNumber();
        updateZoom();
    
        page.getChildren().add(this);
    }
    
    public void updateNumber(){
        setText(String.valueOf(page.getPage()+1));
    }
    
    public void remove(){
        layoutXProperty().unbind();
        layoutYProperty().unbind();
        page.getChildren().remove(this);
    }
    
    public void updateZoom(){
        double factor = (MainWindow.mainScreen.getZoomFactor() - .1) / 3 * 2.5 + .15;
        // .1 .4 -> .15 .4
        
        setStyle("-fx-background-color: rgba(0, 0, 0, .5); -fx-font-weight: 900;" +
                "-fx-font-size: " + 14/factor + ";" +
                "-fx-background-radius: " + 3/factor + " 0 0 0;" +
                " -fx-padding: " + 2/factor + " " + 5/factor + ";");
    }
}
