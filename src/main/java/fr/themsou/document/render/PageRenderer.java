package fr.themsou.document.render;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.main.Main;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class PageRenderer extends Pane {

    private Image render;
    private ImageView renderView;
    private int page;
    private ArrayList<Element> elements = new ArrayList<>();
    public double mouseX = 0;
    public double mouseY = 0;

    public PageRenderer(java.awt.Image render, int page) {
        this.render = SwingFXUtils.toFXImage((BufferedImage) render, null);
        this.page = page;
        renderView = new ImageView(this.render);
        final double ratio = this.render.getHeight() / this.render.getWidth();

        setWidth(Main.mainScreen.pageWidthProperty().get());
        setHeight(Main.mainScreen.pageWidthProperty().get() * ratio);

        prefWidthProperty().bind(Main.mainScreen.pageWidthProperty());
        prefHeightProperty().bind(widthProperty().multiply(ratio));

        renderView.fitHeightProperty().bind(heightProperty());
        renderView.fitWidthProperty().bind(widthProperty());

        getChildren().add(renderView);

        // BORDER

        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {

                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                Main.mainScreen.document.setCurrentPage(page);
            }
        });

        // Disable pane when is not into the visible part of the ScrollPane to prevent lags
        visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            try{
                Bounds paneBounds = Main.mainScreen.localToScene(Main.mainScreen.getBoundsInParent());
                Bounds nodeBounds = localToScene(getBoundsInLocal());
                return paneBounds.intersects(nodeBounds);
            }catch(IndexOutOfBoundsException ex){
                return false;
            }
        }, Main.mainScreen.vvalueProperty()));

    }

    public void clearElements(){
        getChildren().clear();
        getChildren().add(renderView);
        elements = new ArrayList<>();
        Main.lbTextTab.updateOnFileElementsList();
    }

    public void addElement(Element element){

        if(element != null){

            elements.add(element);
            getChildren().add((Shape) element);
            Edition.setUnsave();
            Main.lbTextTab.updateOnFileElementsList();

        }
    }
    public void removeElement(Element element){

        if(element != null){
            elements.remove(element);
            getChildren().remove((Shape) element);
            Edition.setUnsave();
            Main.lbTextTab.updateOnFileElementsList();
        }
    }

    public Image getRender() {
        return render;
    }
    public void setRender(Image render) {
        this.render = render;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public ArrayList<Element> getElements() {
        return elements;
    }
}
