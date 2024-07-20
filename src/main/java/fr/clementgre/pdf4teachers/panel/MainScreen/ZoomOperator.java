/*
 * Copyright (c) 2020-2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.MainScreen;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ZoomOperator {
    
    private final Timeline timelineY = new Timeline(60);
    private final Timeline timelineX = new Timeline(60);
    private final Timeline timelineScale = new Timeline(60);
    
    private final Pane pane;
    
    public ScrollBar vScrollBar = new ScrollBar();
    public ScrollBar hScrollBar = new ScrollBar();
    
    private double aimTranslateY;
    private double aimTranslateX;
    private double aimScale;
    
    private double lastVScrollValue;
    
    public ZoomOperator(Pane pane, MainScreen mainScreen){
        this.pane = pane;
        
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
    
    
        timelineScale.setOnFinished((e) -> {
            aimScale = getPaneScale();
            updateVScrollBar(true);
            updateHScrollBar(true);
        });
        timelineY.setOnFinished((e) -> {
            updateVScrollBar(true);
            updateHScrollBar(true);
        });
        timelineX.setOnFinished((e) -> {
            updateVScrollBar(true);
            updateHScrollBar(true);
        });
        
        // Actualise la longueur des curseurs de scroll lors du zoom
        pane.scaleXProperty().addListener((observable) -> {
            hScrollBar.setVisibleAmount(getMainScreenWidth() / (pane.getWidth() * getPaneScale()));
            vScrollBar.setVisibleAmount(getMainScreenHeight() / (pane.getHeight() * getPaneScale()));
            
            // Update aimScale when not running : this is used in the zoom process
            if(!timelineScale.getStatus().equals(Animation.Status.RUNNING)) aimScale = getPaneScale();
        });
        
        // Vérifie si pane peut rentrer entièrement dans MainScreen quand MainScreen est recardé.
        // Vérifie aussi si pane ne pourrait plus rentrer dans MainScreen et vérifie les translations dans ce cas
        mainScreen.heightProperty().addListener((observable) -> {
            double scrollableHeight = pane.getHeight() * getPaneScale() - (hScrollBar.isVisible() ? mainScreen.getHeight() - hScrollBar.getHeight() : mainScreen.getHeight());
            
            if(scrollableHeight <= 0){
                vScrollBar.setVisible(false);
                setPaneY(centerTranslationY());
            }else{
                if(!vScrollBar.isVisible()){
                    vScrollBar.setVisible(true);
                    vScrollBar.setValue(0.5);
                }else{
                    vScrollBar.setVisible(true);
                    double translateY = -vScrollBar.getValue() * scrollableHeight + getPaneShiftY();
                    if(translateY != getPaneY()){
                        setPaneY(translateY);
                    }
                }
            }
            vScrollBar.setVisibleAmount(getMainScreenHeight() / (pane.getHeight() * getPaneScale()));
            
        });
        mainScreen.widthProperty().addListener((observable) -> {
            double scrollableWidth = pane.getWidth() * getPaneScale() - (vScrollBar.isVisible() ? mainScreen.getWidth() - vScrollBar.getWidth() : mainScreen.getWidth());
            
            if(scrollableWidth <= 0){
                hScrollBar.setVisible(false);
                setPaneX(centerTranslationX());
            }else{
                if(!hScrollBar.isVisible()){
                    hScrollBar.setVisible(true);
                    hScrollBar.setValue(0.5);
                }else{
                    hScrollBar.setVisible(true);
                    double translateX = -hScrollBar.getValue() * scrollableWidth + getPaneShiftX();
                    if(translateX != getPaneX()){
                        setPaneX(translateX);
                    }
                }
            }
            hScrollBar.setVisibleAmount(getMainScreenWidth() / (pane.getWidth() * pane.getScaleX()));
            
        });
        
        // Modifie translateY lorsque la valeur de la scrollBar est modifié.
        vScrollBar.valueProperty().addListener((observable) -> {
            // Update lastVScrollValue
            if(!MainWindow.mainScreen.isEditPagesMode()){
                lastVScrollValue = vScrollBar.getValue();
            }
            
            double translateY = -vScrollBar.getValue() * getScrollableHeight() + getPaneShiftY();
            if(((int) translateY) != ((int) pane.getTranslateY())) setPaneY(translateY);
        });
        // Modifie translateX lorsque la valeur de la scrollBar est modifié.
        hScrollBar.valueProperty().addListener((observable) -> {
            double translateX = -hScrollBar.getValue() * getScrollableWidth() + getPaneShiftX();
            if(((int) translateX) != ((int) pane.getTranslateX())) setPaneX(translateX);
        });
        
        // Modifie la valeur de la scrollBar lorsque translateY est modifié.
        pane.translateYProperty().addListener((observable, oldValue, newValue) -> {
            updateVScrollBar(false);
        });
        // Modifie la valeur de la scrollBar lorsque translateX est modifié.
        pane.translateXProperty().addListener((observable) -> {
            updateHScrollBar(false);
        });
        
        pane.heightProperty().addListener((observable) -> {
            updatePaneDimensions(vScrollBar.getValue(), hScrollBar.getValue());
        });
        pane.widthProperty().addListener((observable) -> {
            updatePaneDimensions(vScrollBar.getValue(), hScrollBar.getValue());
        });
        
        
    }
    
    public void updateVScrollBar(boolean force){
        if(getScrollableHeight() <= 0){
            vScrollBar.setVisible(false);
            setPaneY(centerTranslationY());
        }else{
            vScrollBar.setVisible(true);
            double vValue = MathUtils.clamp((-getPaneY() + getPaneShiftY()) / getScrollableHeight(), 0, 1);
            if(force) vScrollBar.setValue(Math.abs(vValue-.1));
            if(vValue != vScrollBar.getValue()){
                vScrollBar.setValue(vValue);
            }
        }
    }
    public void updateHScrollBar(boolean force){
        if(getScrollableWidth() <= 0){
            hScrollBar.setVisible(false);
            setPaneX(centerTranslationX());
        }else{
            hScrollBar.setVisible(true);
            double hValue = (-getPaneX() + getPaneShiftX()) / getScrollableWidth();
            if(force) hScrollBar.setValue(Math.abs(hValue-.1));
            if(hValue != hScrollBar.getValue()){
                hScrollBar.setValue(MathUtils.clamp(hValue, 0, 1));
            }
        }
    }
    
    public void updatePaneDimensions(double newVValue, double newHValue){
        
        // Définis sur 1 pour actualiser avant de donner la vraie valeur
        vScrollBar.setValue(1);
        hScrollBar.setValue(1);
        
        // Update le système
        // Copie du code du listener de translateYProperty et de translateXProperty
        if(getScrollableHeight() <= 0){
            vScrollBar.setVisible(false);
            setPaneY(centerTranslationY());
            aimTranslateY = pane.getTranslateY();
        }else{
            vScrollBar.setVisible(true);
            double vValue = (-pane.getHeight() + getPaneShiftY()) / getScrollableHeight();
            if(vValue != vScrollBar.getValue()){
                vScrollBar.setValue(MathUtils.clamp(vValue, 0, 1));
            }
        }
        if(getScrollableWidth() <= 0){
            hScrollBar.setVisible(false);
            setPaneX(centerTranslationX());
        }else{
            hScrollBar.setVisible(true);
            double hValue = (-pane.getHeight() + getPaneShiftX()) / getScrollableWidth();
            if(hValue != hScrollBar.getValue()){
                hScrollBar.setValue(MathUtils.clamp(hValue, 0, 1));
            }
        }
        
        // Repasse les bonnes valeurs
        vScrollBar.setValue(MathUtils.clamp(newVValue, 0, 1));
        hScrollBar.setValue(MathUtils.clamp(newHValue, 0, 1));
        
        vScrollBar.setVisibleAmount(getMainScreenHeight() / (pane.getHeight() * getPaneScale()));
        hScrollBar.setVisibleAmount(getMainScreenWidth() / (pane.getWidth() * getPaneScale()));
    }
    
    // x and y should be relative to Scene because MainScreen is bigger than the visible part (e.getX()/Y couldn't work)
    public void zoom(double factor, double x, double y, boolean trackpad){
        
        // determine offset that we will have to move the node
        // Since we are relative to Scene, we have to apply the current Scale transformation
        Bounds bounds = pane.localToScene(pane.getBoundsInLocal());
        double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX())) / Main.settings.zoom.getValue();
        double dy = (y - (bounds.getHeight() / 2 + bounds.getMinY())) / Main.settings.zoom.getValue();
        
        double scale = Math.min(5, Math.max(aimScale * factor, 0.1));
        double f = (scale / getPaneScale()) - 1;
    
        zoom(doRemoveZoomAnimations(factor, trackpad, false), scale, dx, dy, f);
        
    }
    public void zoomFactor(double factor, boolean removeTransitions, boolean trackpad){
        zoom(Math.min(5, Math.max(aimScale * factor, 0.1)), doRemoveZoomAnimations(factor, trackpad, removeTransitions));
    }
    public void zoom(double scale, boolean removeTransitions){
        if(scale == getPaneScale()) return;
        
        // Get bounds relative to scene
        Bounds bounds = pane.localToScene(pane.getBoundsInLocal());
        // Find the middle coordinates of MainScreen relative to scene (To zoom center);
        Bounds mainScreenBounds = MainWindow.mainScreen.localToScene(MainWindow.mainScreen.getLayoutBounds());
        double mainScreenMiddleXInScene = (mainScreenBounds.getMinX() + mainScreenBounds.getWidth() / 2);
        double mainScreenMiddleYInScene = (mainScreenBounds.getMinY() + mainScreenBounds.getHeight() / 2);
        
        // determine offset that we will have to move the node
        // Since we are relative to Scene, we have to apply the current Scale transformation
        double dx = (mainScreenMiddleXInScene - (bounds.getWidth() / 2 + bounds.getMinX())) / Main.settings.zoom.getValue();
        double dy = (mainScreenMiddleYInScene - (bounds.getHeight() / 2 + bounds.getMinY())) / Main.settings.zoom.getValue();
        
        double f = (scale / getPaneScale()) - 1;
    
        zoom(removeTransitions, scale, dx, dy, f);
        
    }
    public void zoom(boolean removeTransition, double scale, double horizontal, double vertical, double translateFactor){
        AutoTipsManager.showByAction("zoom");
        
        double newTranslateX;
        double newTranslateY;
        
        // Donnés pour le traitement de juste après
        final double paneShiftX = (pane.getWidth() * scale - pane.getWidth()) / 2;
        final double paneShiftY = (pane.getHeight() * scale - pane.getHeight()) / 2;
        final double scrollableWidth = pane.getWidth() * scale - getMainScreenWidth();
        final double scrollableHeight = pane.getHeight() * scale - getMainScreenHeight();
        
        
        // Vérifie si pane peut rentrer entièrement dans MainScreen ? centre pane : vérifie les translations
        // X
        if(scrollableWidth <= 0){
            // Centre pane dans MainScreen sur l'axe X
            hScrollBar.setVisible(false);
            newTranslateX = centerTranslationX();
        }else{
            // Vérifie les limites des translations
            hScrollBar.setVisible(true);
            newTranslateX = MathUtils.clamp(getPaneX() - translateFactor * horizontal,
                    paneShiftX - scrollableWidth, paneShiftX);
        }
        // Y
        if(scrollableHeight <= 0){
            // Centre pane dans MainScreen sur l'axe Y
            vScrollBar.setVisible(false);
            newTranslateY = centerTranslationY();
        }else{
            // Vérifie les limites des translations
            vScrollBar.setVisible(true);
            newTranslateY = MathUtils.clamp(getPaneY() - translateFactor * vertical,
                    paneShiftY - scrollableHeight, paneShiftY);
            
        }
        
        aimTranslateY = newTranslateY;
        aimTranslateX = newTranslateX;
        aimScale = scale;
        
        
        if(!removeTransition && Main.settings.animations.getValue()){
            animateY(newTranslateY);
            animateX(newTranslateX);
            animateScale(scale);
        }else{
            setPaneY(newTranslateY);
            setPaneX(newTranslateX);
            setPaneScale(scale);
            updateVScrollBar(false);
            updateHScrollBar(false);
        }
        
    }
    
    public void fitWidth(boolean removeTransition, boolean forceMultiPages){
        double pageWidth = PageRenderer.PAGE_WIDTH + 2 * PageRenderer.getPageMargin();
        double availableWidth = (MainWindow.mainScreen.getWidth() - 40);
        double targetScale;
        
        if(MainWindow.mainScreen.isGridView() && availableWidth > 1300 || forceMultiPages){ // grid
            int pages = Math.max(2, (int) ((availableWidth-PageRenderer.getPageMargin()) / (1.1*PageRenderer.PAGE_WIDTH+PageRenderer.getPageMargin())));
            targetScale = (availableWidth-PageRenderer.getPageMargin()) / (pages*(PageRenderer.PAGE_WIDTH+PageRenderer.getPageMargin()));
            
        }else targetScale = (availableWidth / pageWidth); // single page
        
        zoom(targetScale, removeTransition);
    }
    public void overviewWidth(boolean removeTransition){
        double lastVScrollValue = vScrollBar.getValue();
        zoom(.505, removeTransition);
    
        // Reset the scroll value to the original before the zoom animation
        PlatformUtils.runLaterOnUIThread(500, () ->  {
            this.lastVScrollValue = lastVScrollValue;
        });
    }
    
    // V SCROLL
    
    public void scrollDown(int factor, boolean removeTransition, boolean trackpad){
        scrollByTranslateY(factor, removeTransition, trackpad);
    }
    public void scrollUp(int factor, boolean removeTransition, boolean trackpad){
        scrollByTranslateY(-factor, removeTransition, trackpad);
    }
    
    public void scrollByTranslateY(int factor, boolean removeTransition, boolean trackpad){

        double newTranslateY = aimTranslateY - factor;
        if(newTranslateY - getPaneShiftY() > 0) newTranslateY = getPaneShiftY();
        if(newTranslateY - getPaneShiftY() < -getScrollableHeight()) newTranslateY = -getScrollableHeight() + getPaneShiftY();
        
        scrollByTranslateY(newTranslateY, !doRemoveScrollAnimations(factor, trackpad, removeTransition));
    }
    // WARNING: technical functions that must take in account the pane shift due to the scaling.
    public void scrollByTranslateY(double newTranslateY){
        scrollByTranslateY(newTranslateY, Main.settings.animations.getValue());
    }
    // WARNING: technical functions that must take in account the pane shift due to the scaling.
    public void scrollByTranslateY(double newTranslateY, boolean doAnimate){
        aimTranslateY = newTranslateY;
        if(doAnimate) animateY(newTranslateY);
        else pane.setTranslateY(newTranslateY);
    }
    public void scrollToPage(PageRenderer page){
        int toScroll = (int) ((getPaneY() - getPaneShiftY()) + (page.getTranslateY() - PageRenderer.getPageMargin() + 5) * getPaneScale());
        scrollByTranslateY(toScroll, false, false);
    }
    
    // H SCROLL
    
    public void scrollRight(int factor, boolean removeTransition, boolean trackpad){
        scrollHorizontally(factor, removeTransition, trackpad);
    }
    public void scrollLeft(int factor, boolean removeTransition, boolean trackpad){
        scrollHorizontally(-factor, removeTransition, trackpad);
    }
    public void scrollHorizontally(int factor, boolean removeTransition, boolean trackpad){
        
        double newTranslateX = aimTranslateX - factor;
        if(newTranslateX - getPaneShiftX() > 0) newTranslateX = getPaneShiftX();
        if(newTranslateX - getPaneShiftX() < -getScrollableWidth())
            newTranslateX = -getScrollableWidth() + getPaneShiftX();
        
        aimTranslateX = newTranslateX;
        
        if(!doRemoveScrollAnimations(factor, trackpad, removeTransition)){
            animateX(newTranslateX);
        }else{
            pane.setTranslateX(newTranslateX);
        }
    }
    
    private boolean doRemoveScrollAnimations(double factor, boolean trackpad, boolean removeTransition){
        return !Main.settings.animations.getValue() || removeTransition || trackpad;
        //return !Main.settings.animations.getValue() || Math.abs(factor) < 25 || removeTransition || (trackpad && Main.settings.trackpadMode.getValue());
    }
    private boolean doRemoveZoomAnimations(double factor, boolean trackpad, boolean removeTransition){
        return !Main.settings.animations.getValue() || removeTransition || trackpad;
        //return !Main.settings.animations.getValue() || Math.abs(factor) < 0.05 || removeTransition || (trackpad && Main.settings.trackpadMode.getValue());
    }
    
    // Renvoie le décalage entre les vrais coordonnés de pane et entre les coordonnés de sa partie visible.
    // Lors d'un zoom le shift est négatif | Lors d'un dé-zoom il est positif
    public double getPaneShiftY(){
        return (pane.getHeight() * getPaneScale() - pane.getHeight()) / 2;
    }
    public double getPaneShiftY(double scale){
        return (pane.getHeight() * scale - pane.getHeight()) / 2;
    }
    
    public double getPaneShiftX(){
        return (pane.getWidth() * getPaneScale() - pane.getWidth()) / 2;
    }
    
    // Renvoie les dimensions de MainScreen sans compter les scrolls bars, si elles sonts visibles.
    // Il est conseillé d'utiliser ces méthodes pour récupérer les dimensions de MainScreen.
    public double getMainScreenWidth(){
        if(MainWindow.mainScreen == null) return 0;
        if(!vScrollBar.isVisible()) return MainWindow.mainScreen.getWidth();
        return MainWindow.mainScreen.getWidth() - vScrollBar.getWidth();
    }
    
    public double getMainScreenHeight(){
        if(MainWindow.mainScreen == null) return 0;
        if(!hScrollBar.isVisible()) return MainWindow.mainScreen.getHeight();
        return MainWindow.mainScreen.getHeight() - hScrollBar.getHeight();
    }
    
    // Renvoie les dimensions de la partie visible de pane (multiplication par sa Scale pour avoir sa partie visible)
    // en enlevant les dimensions de MainScreen, on obtient donc la hauteur scrollable.
    public double getScrollableHeight(){
        return pane.getHeight() * getPaneScale() - getMainScreenHeight();
    }
    public double getScrollableHeight(double aimScale){
        return pane.getHeight() * aimScale - getMainScreenHeight();
    }
    
    public double getScrollableWidth(){
        return pane.getWidth() * getPaneScale() - getMainScreenWidth();
    }
    
    // Renvoie la translation qui centre Pane sur MainScreen
    public double centerTranslationY(){
        return (getMainScreenHeight() - pane.getHeight()) / 2;
    }
    
    public double centerTranslationX(){
        return (getMainScreenWidth() - pane.getWidth()) / 2;
    }
    
    
    
    public double getPaneX(){
        return pane.getTranslateX();
    }
    public double getPaneY(){
        return pane.getTranslateY();
    }
    public double getPaneScale(){
        return pane.getScaleX();
    }
    public double getAimScale(){
        return aimScale == 0 ? pane.getScaleX() : aimScale;
    }
    public void setPaneX(double x){
        pane.setTranslateX(x);
        aimTranslateX = x;
    }
    public void setPaneY(double y){
        pane.setTranslateY(y);
        aimTranslateY = y;
    }
    public void setPaneScale(double scale){
        pane.setScaleX(scale);
        pane.setScaleY(scale);
        aimScale = scale;
    }
    public double getLastVScrollValue(){
        return lastVScrollValue;
    }
    
    private void animateX(double newTranslateX){
        timelineX.stop();
        timelineX.getKeyFrames().clear();
        timelineX.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(200), new KeyValue(pane.translateXProperty(), newTranslateX))
        );
        timelineX.play();
    }
    private void animateY(double newTranslateY){
        timelineY.stop();
        timelineY.getKeyFrames().clear();
        timelineY.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(200), new KeyValue(pane.translateYProperty(), newTranslateY))
        );
        timelineY.play();
    }
    private void animateScale(double scale){
        timelineScale.stop();
        timelineScale.getKeyFrames().clear();
        timelineScale.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(200), new KeyValue(pane.scaleXProperty(), scale)),
                new KeyFrame(Duration.millis(200), new KeyValue(pane.scaleYProperty(), scale))
        );
        timelineScale.play();
    }
    
}
