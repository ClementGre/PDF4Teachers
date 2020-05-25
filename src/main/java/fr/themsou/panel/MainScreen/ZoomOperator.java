package fr.themsou.panel.MainScreen;

import fr.themsou.main.Main;
import fr.themsou.windows.MainWindow;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ZoomOperator {

    private Timeline timeline;
    private boolean isPlaying = false;

    private Pane pane;

    public ScrollBar vScrollBar = new ScrollBar();
    public ScrollBar hScrollBar = new ScrollBar();

    private double aimTranslateY = 0;
    private double aimTranslateX = 0;
    private double aimScale = 0;

    public ZoomOperator(Pane pane, MainScreen mainScreen){

        this.pane = pane;

        this.timeline = new Timeline(60);
        timeline.setOnFinished((ActionEvent e) -> {
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                }catch(InterruptedException ex){ ex.printStackTrace(); }
                isPlaying = false;

            }).start();
        });

        vScrollBar.setOrientation(Orientation.VERTICAL);
        hScrollBar.setOrientation(Orientation.HORIZONTAL);
        vScrollBar.setVisible(false);
        hScrollBar.setVisible(false);
        vScrollBar.setMax(1);
        hScrollBar.setMax(1);

        vScrollBar.layoutXProperty().bind(mainScreen.widthProperty().subtract(vScrollBar.widthProperty()));
        hScrollBar.layoutYProperty().bind(mainScreen.heightProperty().subtract(hScrollBar.heightProperty()));
        vScrollBar.prefHeightProperty().bind(mainScreen.heightProperty());
        hScrollBar.prefWidthProperty().bind(Bindings.createDoubleBinding(this::getMainScreenWidth, mainScreen.widthProperty(), vScrollBar.visibleProperty()));

        mainScreen.getChildren().addAll(hScrollBar, vScrollBar);



        // Actualise la longeur des curseur de scroll lors du zoom
        pane.scaleXProperty().addListener((observable, oldValue, newValue) -> {
            hScrollBar.setVisibleAmount(getMainScreenWidth() / (pane.getWidth()*newValue.doubleValue()));
            vScrollBar.setVisibleAmount(getMainScreenHeight() / (pane.getHeight()*newValue.doubleValue()));
        });

        // Vérifie si pane peut rentrer entièrement dans MainScreen quand MainScreen est recardé.
        // Vérifie aussi si pane ne pourait plus rentrer dans MainScreen et vérifie les translations dans ce cas
        mainScreen.heightProperty().addListener((observable, oldValue, newValue) -> {
            double scrollableHeight = pane.getHeight()*pane.getScaleX() - (hScrollBar.isVisible() ? newValue.doubleValue()-hScrollBar.getHeight() : newValue.doubleValue());

            if(scrollableHeight <= 0){
                vScrollBar.setVisible(false);
                pane.setTranslateY(centerTranslationY());
                aimTranslateY = pane.getTranslateY();
            }else{
                if(!vScrollBar.isVisible()){
                    vScrollBar.setVisible(true);
                    vScrollBar.setValue(0.5);
                }else{
                    vScrollBar.setVisible(true);
                    double translateY = -vScrollBar.getValue() * scrollableHeight + getPaneShiftY();
                    if(translateY != pane.getTranslateY()){
                        pane.setTranslateY(translateY);
                        aimTranslateY = pane.getTranslateY();
                    }
                }
            }
            vScrollBar.setVisibleAmount(getMainScreenHeight() / (pane.getHeight()*pane.getScaleX()));

        });
        mainScreen.widthProperty().addListener((observable, oldValue, newValue) -> {
            double scrollableWidth = pane.getWidth()*pane.getScaleX() - (vScrollBar.isVisible() ? newValue.doubleValue()-vScrollBar.getWidth() : newValue.doubleValue());

            if(scrollableWidth <= 0){
                hScrollBar.setVisible(false);
                pane.setTranslateX(centerTranslationX());
                aimTranslateX = pane.getTranslateX();
            }else{
                if(!hScrollBar.isVisible()){
                    hScrollBar.setVisible(true);
                    hScrollBar.setValue(0.5);
                }else{
                    hScrollBar.setVisible(true);
                    double translateX = -hScrollBar.getValue() * scrollableWidth + getPaneShiftX();
                    if(translateX != pane.getTranslateX()){
                        pane.setTranslateX(translateX);
                        aimTranslateX = pane.getTranslateX();
                    }
                }
            }
            hScrollBar.setVisibleAmount(getMainScreenWidth() / (pane.getWidth()*pane.getScaleX()));

        });

        // Modifie translateY lorsque la valeur de la scrollBar est modifié.
        vScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {

            double translateY = -newValue.doubleValue() * getScrollableHeight() + getPaneShiftY();
            if(((int)translateY) != ((int)pane.getTranslateY())){
                pane.setTranslateY(translateY);
                aimTranslateY = pane.getTranslateY();
            }
        });
        // Modifie la valeur de la scrollBar lorsque translateY est modifié.
        pane.translateYProperty().addListener((observable, oldValue, newValue) -> {

            if(getScrollableHeight() <= 0){
                vScrollBar.setVisible(false);
                pane.setTranslateY(centerTranslationY());
                aimTranslateY = pane.getTranslateY();
            }else{
                vScrollBar.setVisible(true);
                double vValue = (-newValue.doubleValue() + getPaneShiftY()) / getScrollableHeight();
                if(vValue != vScrollBar.getValue()){
                    vScrollBar.setValue(vValue);
                }
            }
        });

        // Modifie translateX lorsque la valeur de la scrollBar est modifié.
        hScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {

            double translateX = -newValue.doubleValue() * getScrollableWidth() + getPaneShiftX();
            if(((int)translateX) != ((int)pane.getTranslateX())){
                pane.setTranslateX(translateX);
                aimTranslateX = pane.getTranslateX();
            }
        });
        // Modifie la valeur de la scrollBar lorsque translateX est modifié.
        pane.translateXProperty().addListener((observable, oldValue, newValue) -> {

            if(getScrollableWidth() <= 0){
                hScrollBar.setVisible(false);
                pane.setTranslateX(centerTranslationX());
                aimTranslateX = pane.getTranslateX();
            }else{
                hScrollBar.setVisible(true);
                double hValue = (-newValue.doubleValue() + getPaneShiftX()) / getScrollableWidth();
                if(hValue != hScrollBar.getValue()){
                    hScrollBar.setValue(hValue);
                }
            }
        });

        pane.heightProperty().addListener((observable, oldValue, newValue) -> {
            updatePaneHeight(vScrollBar.getValue(), hScrollBar.getValue());
        });


    }
    public void updatePaneHeight(double newVValue, double newHValue){
        aimScale = pane.getScaleX();

        // Définis sur 1 pour actualiser avant de donner la vraie valeur
        vScrollBar.setValue(1);
        hScrollBar.setValue(1);

        // Update le système
        // Copie du code du listener de translateYProperty et de translateXProperty
        if(getScrollableHeight() <= 0){
            vScrollBar.setVisible(false);
            pane.setTranslateY(centerTranslationY());
            aimTranslateY = pane.getTranslateY();
        }else{
            vScrollBar.setVisible(true);
            double vValue = (-pane.getHeight() + getPaneShiftY()) / getScrollableHeight();
            if(vValue != vScrollBar.getValue()){
                vScrollBar.setValue(vValue);
            }
        }
        if(getScrollableWidth() <= 0){
            hScrollBar.setVisible(false);
            pane.setTranslateX(centerTranslationX());
            aimTranslateX = pane.getTranslateX();
        }else{
            hScrollBar.setVisible(true);
            double hValue = (-pane.getHeight() + getPaneShiftX()) / getScrollableWidth();
            if(hValue != hScrollBar.getValue()){
                hScrollBar.setValue(hValue);
            }
        }

        // Repasse les bonnes valeurs
        vScrollBar.setValue(newVValue);
        hScrollBar.setValue(newHValue);

        vScrollBar.setVisibleAmount(getMainScreenHeight() / (pane.getHeight()*pane.getScaleX()));
        hScrollBar.setVisibleAmount(getMainScreenWidth() / (pane.getWidth()*pane.getScaleX()));
    }

    public void zoom(double factor, double x, double y) {

        if(!isPlaying){
            aimTranslateY = pane.getTranslateY();
            aimTranslateX = pane.getTranslateX();
            aimScale = pane.getScaleX();
        }


        // determine scale
        double oldScale = pane.getScaleX();
        double scale = Math.min(5, Math.max(aimScale * factor, 0.05));
        double f = (scale / oldScale) - 1;

        // determine offset that we will have to move the node
        Bounds bounds = pane.localToScene(pane.getBoundsInLocal());
        double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX()));
        double dy = (y - (bounds.getHeight() / 2 + bounds.getMinY()));

        double newTranslateX;
        double newTranslateY;

        // Donnés pour le traitement de juste après
        final double paneShiftX = (pane.getWidth()*scale - pane.getWidth()) / 2;
        final double paneShiftY = (pane.getHeight()*scale - pane.getHeight()) / 2;
        final double scrollableWidth = pane.getWidth()*scale - getMainScreenWidth();
        final double scrollableHeight = pane.getHeight()*scale - getMainScreenHeight();


        // Vérifie si pane peut rentrer entièrement dans MainScreen ? centre pane : vérifie les translations
        // X
        if(scrollableWidth <= 0){
            // Centre pane dans MainScreen sur l'axe X
            hScrollBar.setVisible(false);
            newTranslateX = centerTranslationX();
        }else{
            // Vérifie les limites des translations
            hScrollBar.setVisible(true);
            newTranslateX = pane.getTranslateX() - f * dx;
            if(newTranslateX - paneShiftX > 0) newTranslateX = paneShiftX;
            else if(newTranslateX - paneShiftX < -scrollableWidth) newTranslateX = -scrollableWidth + paneShiftX;
        }
        // Y
        if(scrollableHeight <= 0){
            // Centre pane dans MainScreen sur l'axe Y
            vScrollBar.setVisible(false);
            newTranslateY = centerTranslationY();
        }else{
            // Vérifie les limites des translations
            vScrollBar.setVisible(true);
            newTranslateY = pane.getTranslateY() - f * dy;
            if(newTranslateY - paneShiftY > 0) newTranslateY = paneShiftY;
            else if(newTranslateY - paneShiftY < -scrollableHeight) newTranslateY = -scrollableHeight + paneShiftY;

        }

        aimTranslateY = newTranslateY;
        aimTranslateX = newTranslateX;
        aimScale = scale;

        if(Main.settings.isZoomAnimations() && factor > 0.05){

            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(pane.translateXProperty(), newTranslateX)),
                    new KeyFrame(Duration.millis(200), new KeyValue(pane.translateYProperty(), newTranslateY)),
                    new KeyFrame(Duration.millis(200), new KeyValue(pane.scaleXProperty(), scale)),
                    new KeyFrame(Duration.millis(200), new KeyValue(pane.scaleYProperty(), scale))
            );
            timeline.stop();
            isPlaying = true;
            timeline.play();
        }else{
            pane.setTranslateY(newTranslateY);
            pane.setTranslateX(newTranslateX);
            pane.setScaleY(scale);
            pane.setScaleX(scale);
        }

    }

    public void scrollDown(int factor, boolean removeTransition){
        if(!isPlaying){
            aimTranslateY = pane.getTranslateY();
            aimTranslateX = pane.getTranslateX();
            aimScale = pane.getScaleX();
        }

        double newTranslateY = aimTranslateY - factor;
        if(newTranslateY - getPaneShiftY() < -getScrollableHeight()) newTranslateY = -getScrollableHeight() + getPaneShiftY();

        aimTranslateY = newTranslateY;

        if(Main.settings.isZoomAnimations() && factor > 25 && !removeTransition){
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(pane.translateYProperty(), aimTranslateY))
            );
            timeline.stop();
            isPlaying = true;
            timeline.play();

        }else{
            pane.setTranslateY(aimTranslateY);
        }
    }

    public void scrollUp(int factor, boolean removeTransition){
        if(!isPlaying){
            aimTranslateY = pane.getTranslateY();
            aimTranslateX = pane.getTranslateX();
            aimScale = pane.getScaleX();
        }

        double newTranslateY = aimTranslateY + factor;
        if(newTranslateY - getPaneShiftY() > 0) newTranslateY = getPaneShiftY();

        aimTranslateY = newTranslateY;

        if(Main.settings.isZoomAnimations() && factor > 25 && !removeTransition){
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(pane.translateYProperty(), newTranslateY))
            );
            timeline.stop();
            isPlaying = true;
            timeline.play();
        }else{
            pane.setTranslateY(newTranslateY);
        }
    }

    public void scrollRight(int factor, boolean removeTransition){
        if(!isPlaying){
            aimTranslateY = pane.getTranslateY();
            aimTranslateX = pane.getTranslateX();
            aimScale = pane.getScaleX();
        }

        double newTranslateX = aimTranslateX - factor;
        if(newTranslateX - getPaneShiftX() < -getScrollableWidth()) newTranslateX = -getScrollableWidth() + getPaneShiftX();

        aimTranslateX = newTranslateX;

        if(Main.settings.isZoomAnimations() && factor > 25 && !removeTransition){
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(pane.translateXProperty(), newTranslateX))
            );
            timeline.stop();
            isPlaying = true;
            timeline.play();
        }else{
            pane.setTranslateX(newTranslateX);
        }
    }
    public void scrollLeft(int factor, boolean removeTransition){
        if(!isPlaying){
            aimTranslateY = pane.getTranslateY();
            aimTranslateX = pane.getTranslateX();
            aimScale = pane.getScaleX();
        }

        double newTranslateX = aimTranslateX + factor;
        if(newTranslateX - getPaneShiftX() > 0) newTranslateX = getPaneShiftX();

        aimTranslateX = newTranslateX;

        if(Main.settings.isZoomAnimations() && factor > 25 && !removeTransition){
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(pane.translateXProperty(), aimTranslateX))
            );
            timeline.stop();
            isPlaying = true;
            timeline.play();

        }else{
            pane.setTranslateX(aimTranslateX);
        }
    }

    // Renvoie le décalage entre les vrais coordonés de pane et entre les coordonés de sa partie visible.
    // Lors d'un zoom le shift est négatif | Lors d'un dé-zoom il est positif
    public double getPaneShiftY(){
        return (pane.getHeight()*pane.getScaleX() - pane.getHeight()) / 2;
    }
    public double getPaneShiftX(){
        return (pane.getWidth()*pane.getScaleX() - pane.getWidth()) / 2;
    }

    // Renvoie les dimensions de MainScreen sans compter les scrolls bars, si elles sonts visibles.
    // Il est conseillé d'utiliser ces méthodes pour récupérer les dimensions de MainScreen.
    public double getMainScreenWidth(){
        if(MainWindow.mainScreen == null) return 0;
        if(!vScrollBar.isVisible()) return MainWindow.mainScreen.getWidth();
        else return MainWindow.mainScreen.getWidth() - vScrollBar.getWidth();
    }
    public double getMainScreenHeight(){
        if(MainWindow.mainScreen == null) return 0;
        if(!hScrollBar.isVisible()) return MainWindow.mainScreen.getHeight();
        else return MainWindow.mainScreen.getHeight() - hScrollBar.getHeight();
    }

    // Renvoie les dimensions de la partie visible de pane (multiplication par sa Scale pour avoir sa partie visible)
    // en enlevant les dimensions de MainScreen, on obtient donc la hauteur scrollable.
    public double getScrollableHeight(){
        return pane.getHeight()*pane.getScaleX() - getMainScreenHeight();
    }
    public double getScrollableWidth(){
        return pane.getWidth()*pane.getScaleX() - getMainScreenWidth();
    }

    // Renvoie la translation qui centre Pane sur MainScreen
    public double centerTranslationY(){
        return (getMainScreenHeight()-pane.getHeight())/2;
    }
    public double centerTranslationX(){
        return (getMainScreenWidth()-pane.getWidth())/2;
    }
}