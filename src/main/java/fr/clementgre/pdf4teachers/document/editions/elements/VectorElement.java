/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.ObservableChangedUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.document.render.display.VectorElementPageDrawer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorData;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorListPane;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.exceptions.PathParseException;
import fr.clementgre.pdf4teachers.utils.svg.SVGUtils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class VectorElement extends GraphicElement{
    
    private final SVGPath noScaledSvgPath = new SVGPath();
    private SVGPath svgPath = new SVGPath();
    
    private final StringProperty path = new SimpleStringProperty();
    private final BooleanProperty doFill = new SimpleBooleanProperty();
    private final ObjectProperty<Color> fill = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> stroke = new SimpleObjectProperty<>();
    private final IntegerProperty strokeWidth = new SimpleIntegerProperty();
    
    private final IntegerProperty arrowLength = new SimpleIntegerProperty(0);
    
    private final BooleanProperty invertX = new SimpleBooleanProperty();
    private final BooleanProperty invertY = new SimpleBooleanProperty();
    
    private VectorData linkedVectorData;
    
    public VectorElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode,
                         boolean doFill, Color fill, Color stroke, int strokeWidth, String path, boolean invertX, boolean invertY, int arrowLength){
        this(x, y, pageNumber, hasPage, width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, arrowLength, null);
    }
    public VectorElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode,
                         boolean doFill, Color fill, Color stroke, int strokeWidth, String path, boolean invertX, boolean invertY, int arrowLength, VectorData linkedVectorData){
        super(x, y, pageNumber, width, height, repeatMode, resizeMode);
    
        this.path.set(path);
        this.doFill.set(doFill);
        this.fill.set(fill);
        this.stroke.set(stroke);
        this.strokeWidth.set(strokeWidth);
        this.invertX.set(invertX);
        this.invertY.set(invertY);
        this.arrowLength.set(arrowLength);
        noScaledSvgPath.setContent(getPath());
    
        if(linkedVectorData != null) setLinkedVectorData(linkedVectorData);
        
        if(hasPage && getPage() != null) setupPage(true);
    }
    
    private void setupPage(boolean checkSize){
        updateFill();
        updateStroke();
        onSizeChanged();
    
        widthProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
        heightProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
        
        noScaledSvgPath.setContent(getPath());
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        svgPath.setFillRule(FillRule.NON_ZERO);
        setupGeneral(svgPath);
        
        if(checkSize && getRealWidth() == 0 && getRealHeight() == 0){
            defineSizeAuto();
        }else{
            // checkLocation must be called after centerOnCoordinatesX() or centerOnCoordinatesY()
            Platform.runLater(() -> checkLocation(getRealX() * getPage().getWidth() / GRID_WIDTH, getRealY() * getPage().getHeight() / GRID_HEIGHT,
                    getRealWidth() * getPage().getWidth() / GRID_WIDTH, getRealHeight() * getPage().getHeight() / GRID_HEIGHT, false));
        }
    }
    
    @Override
    public void initializePage(int pageNumber, double x, double y){
        this.pageNumber = pageNumber;
        
        setupPage(false);
        checkLocation(x, y, false);
    }
    
    // SETUP / EVENT CALL BACK
    
    
    /**
     *  {@link #noScaledSvgPath} is just used to measure the actual size of the svg path, so we can scale it into {@link #svgPath}.
     *  The SvgPath of {@link VectorElementPageDrawer} is also bound to {@link #noScaledSvgPath}.
     *  This function makes sure the coordinates of {@link #noScaledSvgPath} and {@link #path} are relative to the page coordinate space.
     *  The path is scaled to have a width getWidth() and a height getHeight(), which are the JavaFX region dimensions.
     *  Since the UndoAction must be a {@link UType#ELEMENT_NO_COUNT_AFTER}, {@link #isPathScaledToPage} is set to {@code true}
     */
    private boolean isPathScaledToPage;
    
    private void updateFill(){
        svgPath.setFill(isDoFill() ? getFill() : null);
    }
    private void updateStroke(){
        svgPath.setStroke(getStroke());
        svgPath.setStrokeWidth(getStrokeWidth());
        svgPath.setStrokeMiterLimit(Math.max(1, getStrokeWidth()));
    }
    
    @Override
    protected void onMouseRelease(){
        super.onMouseRelease();
    }
    
    @Override
    public void onDoubleClick(){
        enterEditMode();
    }
    
    @Override
    protected void setupMenu(){
        super.setupMenu();
    
        NodeMenuItem addToFavorites = new NodeMenuItem(TR.tr("elementMenu.addToFavouriteList"), false);
        NodeMenuItem addToLast = new NodeMenuItem(TR.tr("elementMenu.addToPreviousList"), false);
        NodeMenuItem enterEditMode = new NodeMenuItem(TR.tr("paintTab.vectorsEditMode"), false);
        enterEditMode.setToolTip(TR.tr("paintTab.vectorsEditMode.tooltip"));
        
        addToFavorites.setOnAction((event) -> linkedVectorData = VectorListPane.addFavoriteVector(this));
        addToLast.setOnAction((event) -> linkedVectorData = VectorListPane.addLastVector(this));
        enterEditMode.setOnAction((e) -> enterEditMode());
    
        menu.getItems().addAll(addToFavorites, addToLast, enterEditMode);
    }
    
    private void onSizeChanged(){
        if(getWidth() == 0 && getHeight() == 0) return;
    
        double padding = getSVGPadding();
        svgPath.setLayoutX(padding);
        svgPath.setLayoutY(padding);
        
        try{
            if(getRepeatMode() == GraphicElement.RepeatMode.CROP){
                if(getLayoutBounds().getWidth() > getLayoutBounds().getHeight()*getRatio()){ // Crop Y
                    svgPath.setContent(getScaledPath((float) getLayoutBounds().getWidth(), (float) (getLayoutBounds().getWidth()/getRatio()), (float) padding));
                }else{ // Crop X
                    svgPath.setContent(getScaledPath((float) (getLayoutBounds().getHeight()*getRatio()), (float) getLayoutBounds().getHeight(), (float) padding));
                }
            }else if(getRepeatMode() == RepeatMode.MULTIPLY){
                svgPath.setContent(getRepeatedPath((float) getLayoutBounds().getWidth(), (float) getLayoutBounds().getHeight(), (float) padding));
            }else{
                svgPath.setContent(getScaledPath((float) getLayoutBounds().getWidth(), (float) getLayoutBounds().getHeight(), (float) padding));
            }
        }catch(PathParseException e){
            Log.e("PathParseException: " + e.getMessage());
        }
        
        svgPath.setClip(new Rectangle(-padding - getClipPadding(), -padding - getClipPadding(),
                getLayoutBounds().getWidth() + getClipPadding()*2, getLayoutBounds().getHeight() + getClipPadding()*2));
        
    }
    
    public String getScaledPath(float wantedWidth, float wantedHeight, float padding) throws PathParseException{
        return getScaledPath(getPath(), noScaledSvgPath, wantedWidth, wantedHeight, padding, isInvertX(), isInvertY(), arrowLength.get(), -1);
    }
    public String getScaledPathScaled(float wantedWidth, float wantedHeight, float padding, double pageWidth) throws PathParseException{
        return getScaledPath(getPath(), noScaledSvgPath, wantedWidth, wantedHeight, padding, isInvertX(), isInvertY(), (float) getArrowLengthScaled(pageWidth), -1);
    }
    public static String getScaledPath(String path, SVGPath sourceSVG, float wantedWidth, float wantedHeight, float padding, boolean invertX, boolean invertY, float arrowLength, int decimals) throws PathParseException{
        double finalCurrentWidth = Math.max(1d, sourceSVG.getLayoutBounds().getWidth());
        double finalCurrentHeight = Math.max(1d, sourceSVG.getLayoutBounds().getHeight());
        
        String scaledPath = (decimals == -1 ? "M0 0 " : "") + SVGUtils.transformPath(path,
                (float) ((wantedWidth-padding*2) / finalCurrentWidth),
                (float) ((wantedHeight-padding*2) / finalCurrentHeight),
                (float) -sourceSVG.getBoundsInLocal().getMinX(),
                (float) -sourceSVG.getBoundsInLocal().getMinY(),
                invertX, invertY,
                (float) finalCurrentWidth,
                (float) finalCurrentHeight,
                decimals);
        
        if(arrowLength > 0){
            return SVGUtils.addArrowsToPath(scaledPath, arrowLength, decimals);
        }
        return scaledPath;
    }
    public record ScaledVectorInfo(String path, Bounds withoutArrowBounds){}
    
    public String getRepeatedPath(float wantedWidth, float wantedHeight, float padding) throws PathParseException{
        return getRepeatedPath(getPath(), noScaledSvgPath, wantedWidth, wantedHeight, padding, isInvertX(), isInvertY(), getArrowLength(), -1);
    }
    public String getRepeatedPathScaled(float wantedWidth, float wantedHeight, float padding, double pageWidth) throws PathParseException{
        return getRepeatedPath(getPath(), noScaledSvgPath, wantedWidth, wantedHeight, padding, isInvertX(), isInvertY(), (float) getArrowLengthScaled(pageWidth), -1);
    }
    public static String getRepeatedPath(String path, SVGPath sourceSVG, float wantedWidth, float wantedHeight, float padding, boolean invertX, boolean invertY, float arrowLength, int decimals) throws PathParseException{
        double finalCurrentWidth = Math.max(1d, sourceSVG.getLayoutBounds().getWidth());
        double finalCurrentHeight = Math.max(1d, sourceSVG.getLayoutBounds().getHeight());
        double ratio = sourceSVG.getLayoutBounds().getWidth() / sourceSVG.getLayoutBounds().getHeight();
        
        StringBuilder newPath = new StringBuilder(decimals == -1 ? "M0 0 " : "");
        
        if(wantedWidth > wantedHeight*ratio){ // Multiply X
    
            float unitWidth = (float) ((wantedHeight - padding * 2) * ratio);
            float startX = 0;
            while(startX < wantedWidth && unitWidth > 0){
                newPath.append(SVGUtils.transformPath(path,
                        (float) ((unitWidth) / finalCurrentWidth),
                        (float) ((wantedHeight - padding * 2) / finalCurrentHeight),
                        (float) (-sourceSVG.getBoundsInLocal().getMinX() + (invertX ? -1 : 1) * startX/(wantedHeight - padding * 2)*finalCurrentHeight),
                        (float) -sourceSVG.getBoundsInLocal().getMinY(),
                        invertX, invertY,
                        (float) finalCurrentWidth,
                        (float) finalCurrentHeight,
                        decimals));
    
                startX += unitWidth;
            }
            
        }else{ // Multiply Y
            
            float unitHeight = (float) ((wantedWidth - padding * 2) / ratio);
            float startY = 0;
            while(startY < wantedHeight && unitHeight > 0){
                newPath.append(SVGUtils.transformPath(path,
                        (float) ((wantedWidth - padding * 2) / finalCurrentWidth),
                        (float) ((unitHeight) / finalCurrentHeight),
                        (float) -sourceSVG.getBoundsInLocal().getMinX(),
                        (float) (-sourceSVG.getBoundsInLocal().getMinY() + (invertY ? -1 : 1) * startY/(wantedWidth - padding * 2)*finalCurrentWidth),
                        invertX, invertY,
                        (float) finalCurrentWidth,
                        (float) finalCurrentHeight,
                        decimals));
        
                startY += unitHeight;
            }
            
        }
        if(arrowLength > 0){
            return SVGUtils.addArrowsToPath(newPath.toString(), arrowLength, decimals);
        }
        return newPath.toString();
    }
    
    public double getSVGPadding(){
        return getResizeMode() == ResizeMode.OPPOSITE_CORNERS ? 0 : getStrokeWidth()/2d;
    }
    public static double getSVGPadding(VectorData vectorData){
        return vectorData.getResizeMode() == ResizeMode.OPPOSITE_CORNERS ? 0 : vectorData.getStrokeWidth()/2d;
    }
    public double getSVGPaddingScaled(double pageWidth){
        return getResizeMode() == ResizeMode.OPPOSITE_CORNERS ? 0 : getStrokeWidthScaled(pageWidth)/2d;
    }
    public double getClipPadding(){
        if(getRepeatMode() == GraphicElement.RepeatMode.CROP || getRepeatMode() == RepeatMode.MULTIPLY){
            return 0;
        }
        double padding = getStrokeWidth();
        if(getArrowLength() != 0){ // Arrows need special clip padding since the forks can overflow the element region.
            padding += getArrowLength();
        }
        return padding;
    }
    public static double getClipPadding(VectorData vectorData){
        if(vectorData.getRepeatMode() == GraphicElement.RepeatMode.CROP || vectorData.getRepeatMode() == RepeatMode.MULTIPLY){
            return 0;
        }
        double padding = vectorData.getStrokeWidth();
        if(vectorData.getArrowLength() != 0){ // Arrows need special clip padding since the forks can overflow the element region.
            padding += vectorData.getArrowLength();
        }
        return padding;
    }
    public double getClipPaddingScaled(double pageWidth){
        if(getRepeatMode() == GraphicElement.RepeatMode.CROP || getRepeatMode() == RepeatMode.MULTIPLY){
            return 0;
        }
        double padding = getStrokeWidthScaled(pageWidth);
        if(getArrowLength() != 0){ // Arrows need special clip padding since the forks can overflow the element region.
            padding += getArrowLengthScaled(pageWidth);
        }
        return padding;
    }
    
    // ACTIONS
    @Override
    public void select(){
        super.select();
        SideBar.selectTab(MainWindow.paintTab);
    }
    @Override
    protected void setupBindings(){
        super.setupBindings();
        
        path.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setPath(newValue);
            noScaledSvgPath.setContent(getPath());
            svgPath.setContent(newValue);
            onSizeChanged();
            Edition.setUnsave("VectorElement changed");
    
            // New move added OR this is the first registration of this action/property.
            if(StringUtils.count(oldValue.toLowerCase(), 'm') != StringUtils.count(newValue.toLowerCase(), 'm')
                    || !MainWindow.mainScreen.isNextUndoActionProperty(path)){
                // ELEMENT_NO_COUNT_BEFORE because this action should be undone before undoing the following action that
                // has no link with it, but that comes before. If it is a page action, it will consume an action.
                MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, path, oldValue.trim(), isPathScaledToPage ? UType.ELEMENT_NO_COUNT_BEFORE : UType.ELEMENT));
            }
        });
        
        fill.addListener((observable, oldValue, newValue) ->{
            if(linkedVectorData != null) linkedVectorData.setFill(newValue);
            updateFill();
            Edition.setUnsave("VectorElement changed");
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, fill, oldValue, UType.ELEMENT));
        });
        doFill.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setDoFill(newValue);
            updateFill();
            Edition.setUnsave("VectorElement changed");
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, doFill, oldValue, UType.ELEMENT));
        });
        
        stroke.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setStroke(newValue);
            updateStroke();
            Edition.setUnsave("VectorElement changed");
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, stroke, oldValue, UType.ELEMENT));
        });
        strokeWidth.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setStrokeWidth(newValue.intValue());
            updateStroke();
            onSizeChanged();
            Edition.setUnsave("VectorElement changed");
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, strokeWidth, oldValue, UType.ELEMENT));
        });
    
        repeatModeProperty().addListener((o, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setRepeatMode(newValue);
            onSizeChanged();
            Edition.setUnsave("VectorElement changed");
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, repeatMode, oldValue, UType.ELEMENT));
        });
        resizeModeProperty().addListener((o, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setResizeMode(newValue);
            onSizeChanged();
            Edition.setUnsave("VectorElement changed");
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, resizeMode, oldValue, UType.ELEMENT));
        });
        
        invertXProperty().addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setInvertX(newValue);
            onSizeChanged();
            Edition.setUnsave("VectorElement changed");
        });
        invertYProperty().addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setInvertY(newValue);
            onSizeChanged();
            Edition.setUnsave("VectorElement changed");
        });
        
        realWidthProperty().addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setWidth(newValue.intValue());
            Edition.setUnsave("VectorElement changed");
        });
        realHeightProperty().addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setHeight(newValue.intValue());
            Edition.setUnsave("VectorElement changed");
        });
        
        arrowLength.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setArrowLength(newValue.intValue());
            onSizeChanged();
            Edition.setUnsave("VectorElement changed");
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, arrowLength, oldValue, UType.ELEMENT));
        });
    }
    
    @Override
    public void addedToDocument(boolean markAsUnsave){
    
    }
    @Override
    public void removedFromDocument(boolean markAsUnsave){
        super.removedFromDocument(markAsUnsave);
        linkedVectorData = null;
    }
    
    // READER AND WRITERS
    
    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("doFill", doFill.get());
        data.put("fill", fill.get().toString());
        data.put("stroke", stroke.get().toString());
        data.put("strokeWidth", strokeWidth.get());
        data.put("path", path.get());
        data.put("invertX", invertX.get());
        data.put("invertY", invertY.get());
        data.put("arrowLength", arrowLength.get());
        
        return data;
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data, int page){
        VectorElement element = readYAMLDataAndGive(data, true, page);
        if(MainWindow.mainScreen.document.getPagesNumber() > element.getPageNumber())
            MainWindow.mainScreen.document.getPage(element.getPageNumber()).addElement(element, false, UType.NO_UNDO);
    }
    
    public static VectorElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage, int page){
        
        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        boolean doFill = Config.getBoolean(data, "doFill");
        Color fill = Color.DARKGRAY;
        Color stroke = Color.BLACK;
        try{
            fill = Color.valueOf(Config.getString(data, "fill"));
            stroke = Color.valueOf(Config.getString(data, "stroke"));
        }catch(IllegalArgumentException e){
            Log.e("Unable to parse VectorElement color: " + e.getMessage());
        }
        int strokeWidth = (int) Config.getLong(data, "strokeWidth");
        String path = Config.getString(data, "path");
    
        boolean invertX = Config.getBoolean(data, "invertX");
        boolean invertY = Config.getBoolean(data, "invertY");
        
        int arrowLength = (int) Config.getLong(data, "arrowLength");
        
        RepeatMode repeatMode = RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        ResizeMode resizeMode = ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        
        return new VectorElement(x, y, page, hasPage, width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, arrowLength);
    }
    
    // EDIT MODE
    
    public void enterEditMode(){
        // In case button is still not selected in PaintTab, we select it, and then, this method will be re-called.
        if(!MainWindow.paintTab.vectorEditMode.isSelected()){
            MainWindow.paintTab.vectorEditMode.setSelected(true);
        }else{
            getPage().getVectorElementPageDrawer().enterEditMode(this);
            Platform.runLater(() -> AutoTipsManager.showByAction("enterVectorEditMode"));
        }
    }
    @Override
    protected void onDeSelected(){
        super.onDeSelected();
        VectorElementPageDrawer drawer = getPage().getVectorElementPageDrawerNull();
        if(drawer != null && drawer.isEditMode() && drawer.getVectorElement() == this){
            // Element is not anymore selected so it will not be done automatically.
            drawer.quitEditMode();
            
            getPage().getVectorElementPageDrawer().quitEditMode();
            if(path.isEmpty().get()) delete(true, UType.ELEMENT_NO_COUNT_BEFORE);
            else requestFocus();
            MainWindow.paintTab.vectorEditMode.setSelected(false);
        }
        
        if(getPath().isBlank()){
            delete(false, UType.ELEMENT_NO_COUNT_BEFORE);
        }
    }
    public boolean isEditMode(){
        return getPage().isVectorEditMode() && getPage().getVectorElementPageDrawer().getVectorElement() == this;
    }
    public void quitEditMode(){
        // In case button is still selected in PaintTab, we de-select it, and then, this method will be re-called.
        if(MainWindow.paintTab.vectorEditMode.isSelected()){
            MainWindow.paintTab.vectorEditMode.setSelected(false);
        }else{
            getPage().getVectorElementPageDrawer().quitEditMode();
        }
        if(path.isEmpty().get()) delete(true, UType.ELEMENT_NO_COUNT_BEFORE);
        else requestFocus();
    }
    public void formatNoScaledSvgPathToPage(){
        float padding = (float) getSVGPadding();
        float width = (float) getWidth();
        float height = (float) getHeight();
    
        if(getRepeatMode() == RepeatMode.MULTIPLY){
            float ratio = (float) (noScaledSvgPath.getLayoutBounds().getWidth() / noScaledSvgPath.getLayoutBounds().getHeight());
            if(width > height*ratio){ // Multiply X
                width = (height -2*padding) * ratio + 2*padding;
            }else{ // Multiply Y
                height = (width -2*padding) / ratio + 2*padding;
            }
        }else if(getRepeatMode() == RepeatMode.CROP){
            float ratio = (float) (noScaledSvgPath.getLayoutBounds().getWidth() / noScaledSvgPath.getLayoutBounds().getHeight());
            if(width > height*ratio){ // Crop Y
                height = (width -2*padding) / ratio + 2*padding;
            }else{ // Crop X
                width = (height -2*padding) * ratio + 2*padding;
            }
        }
        
        try{
            isPathScaledToPage = true;
            setPath(getScaledPath(getPath(), noScaledSvgPath, width, height, padding, false, false, 0, 6));
        }catch(PathParseException e){
            Log.eNotified(e);
        }finally{
            isPathScaledToPage = false;
        }
    }
    
    public void undoLastAction(){
        undo("m", "z", "l", "h", "v", "a", "c", "s", "t", "q");
    }
    public void undoLastLines(){
        undo("m");
    }
    
    public void undo(String... toUndoActions){
        
        Bounds beforeBounds = noScaledSvgPath.getLayoutBounds();
        if(toUndoActions.length == 1){ // Optimize code when length == 1
            setPath(StringUtils.removeAfterLastOccurrenceIgnoringCase(getPath(), toUndoActions[0]));
        }else{
            setPath(StringUtils.removeAfterLastOccurrenceIgnoringCase(getPath(), toUndoActions));
        }
        
        
        // correct element dimensions
        if(!isEditMode()){
            correctDimensions(beforeBounds);
        }
    }
    
    // Dimensions must not be corrected if element is in edit mode
    public void correctDimensions(Bounds beforeBounds){
        Bounds afterBounds = noScaledSvgPath.getLayoutBounds();
        
        // noScaledSvgPath is not always in the page coordinate context:
        double scaleRatioX = (getWidth() - 2*getSVGPadding()) / beforeBounds.getWidth();
        double scaleRatioY = (getHeight() - 2*getSVGPadding()) / beforeBounds.getHeight();
        
        // The dimensions are corrected only if the element is on the page coordinate context (aka come from the EditMode)
        // Math.round(a * 100.0) / 100.0; gets the rounded value at 3 digits.
        if(Math.round(scaleRatioX * 1000.0) / 1000.0 != 1 || Math.round(scaleRatioY * 1000.0) / 1000.0 != 1) return;
    
        double xShift = afterBounds.getMinX() - beforeBounds.getMinX();
        double yShift = afterBounds.getMinY() - beforeBounds.getMinY();
        double wShift = afterBounds.getWidth() - beforeBounds.getWidth();
        double hShift = afterBounds.getHeight() - beforeBounds.getHeight();
        
        checkLocation(getLayoutX() + xShift, getLayoutY() + yShift,
                getWidth() + wShift, getHeight() + hShift, false);
    }
    
    // SPECIFIC METHODS
    @Override
    public void incrementUsesAndLastUse(){
    
    }
    
    @Override
    public double getRatio(){
        return noScaledSvgPath.getLayoutBounds().getWidth() / noScaledSvgPath.getLayoutBounds().getHeight();
    }
    
    @Override
    public void defineSizeAuto(){
        double width;
        double height;
        
        if(getResizeMode() == GraphicElement.ResizeMode.SIDE_EDGES){
            height = getHeight() < 5 ? 10 : getHeight();
            width = 50;
            
        }else{
            double imgWidth = noScaledSvgPath.getLayoutBounds().getWidth();
            double imgHeight = noScaledSvgPath.getLayoutBounds().getHeight();
            width = MathUtils.clamp(imgWidth, 50, getPage().getWidth()/3);
            height = imgHeight * width/imgWidth;
        }
        
        checkLocation(getRealX() * getPage().getWidth() / GRID_WIDTH, getRealY() * getPage().getHeight() / GRID_HEIGHT,
                width, height, false);
    }
    
    @Override
    public Element clone(){
        return new VectorElement(getRealX(), getRealY(), getPageNumber(), true, getRealWidth(), getRealHeight(), getRepeatMode(), getResizeMode(),
                isDoFill(), getFill(), getStroke(), getStrokeWidth(), getPath(), isInvertX(), isInvertY(), getArrowLength());
    }
    @Override
    public Element cloneHeadless(){
        return new VectorElement(getRealX(), getRealY(), getPageNumber(), false, getRealWidth(), getRealHeight(), getRepeatMode(), getResizeMode(),
                isDoFill(), getFill(), getStroke(), getStrokeWidth(), getPath(), isInvertX(), isInvertY(), getArrowLength());
    }

    public void setLinkedVectorData(VectorData vectorData){
        linkedVectorData = vectorData;
    }
    
    // GETTER / SETTER
    
    
    @Override
    public String getElementName(boolean plural){
        return getElementNameStatic(plural);
    }
    public static String getElementNameStatic(boolean plural){
        if(plural) return TR.tr("elements.name.vectors");
        return TR.tr("elements.name.vector");
    }
    
    public SVGPath getSvgPath(){
        return svgPath;
    }
    public void setSvgPath(SVGPath svgPath){
        this.svgPath = svgPath;
    }
    public String getPath(){
        return path.get();
    }
    public StringProperty pathProperty(){
        return path;
    }
    public void setPath(String path){
        this.path.set(path);
    }
    public boolean isDoFill(){
        return doFill.get();
    }
    public BooleanProperty doFillProperty(){
        return doFill;
    }
    public void setDoFill(boolean doFill){
        this.doFill.set(doFill);
    }
    public Color getFill(){
        return fill.get();
    }
    public ObjectProperty<Color> fillProperty(){
        return fill;
    }
    public void setFill(Color fill){
        this.fill.set(fill);
    }
    public Color getStroke(){
        return stroke.get();
    }
    public ObjectProperty<Color> strokeProperty(){
        return stroke;
    }
    public void setStroke(Color stroke){
        this.stroke.set(stroke);
    }
    public int getStrokeWidth(){
        return strokeWidth.get();
    }
    public double getStrokeWidthScaled(double pageWidth){
        return ((double) strokeWidth.get()) / PageRenderer.PAGE_WIDTH * pageWidth;
    }
    public IntegerProperty strokeWidthProperty(){
        return strokeWidth;
    }
    public void setStrokeWidth(int strokeWidth){
        this.strokeWidth.set(strokeWidth);
    }
    public boolean isInvertX(){
        return invertX.get();
    }
    public BooleanProperty invertXProperty(){
        return invertX;
    }
    public void setInvertX(boolean invertX){
        this.invertX.set(invertX);
    }
    public boolean isInvertY(){
        return invertY.get();
    }
    public BooleanProperty invertYProperty(){
        return invertY;
    }
    public void setInvertY(boolean invertY){
        this.invertY.set(invertY);
    }
    public SVGPath getNoScaledSvgPath(){
        return noScaledSvgPath;
    }
    public int getArrowLength(){
        return arrowLength.get();
    }
    public double getArrowLengthScaled(double pageWidth){
        return ((double) arrowLength.get()) / PageRenderer.PAGE_WIDTH * pageWidth;
    }
    public IntegerProperty arrowLengthProperty(){
        return arrowLength;
    }
    public void setArrowLength(int arrowLength){
        this.arrowLength.set(arrowLength);
    }
    
    public void invertInversions(){
        setInvertX(!isInvertX());
        setInvertY(!isInvertY());
    }
}
