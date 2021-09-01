/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.ObservableChangedUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoEngine;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeRating;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTab;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.VPos;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextBoundsType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradeElement extends Element {
    
    private final ScratchText text = new ScratchText();
    
    private final StringProperty name;
    private final DoubleProperty value;
    private final DoubleProperty total;
    private final DoubleProperty outOfTotal;
    private int index;
    private String parentPath;
    private final BooleanProperty alwaysVisible;
    
    public int nextRealYToUse = 0;
    
    public GradeElement(int x, int y, int pageNumber, boolean hasPage, double value, double total, double outOfTotal, int index, String parentPath, String name, boolean alwaysVisible){
        super(x, y, pageNumber);
        this.pageNumber = pageNumber;
        this.realX.set(x);
        this.realY.set(y);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleDoubleProperty(value);
        this.total = new SimpleDoubleProperty(total);
        this.outOfTotal = new SimpleDoubleProperty(outOfTotal);
        this.index = index;
        this.parentPath = parentPath;
        this.alwaysVisible = new SimpleBooleanProperty(alwaysVisible);
        
        text.setBoundsType(TextBoundsType.LOGICAL);
        text.setTextOrigin(VPos.TOP);
        
        setVisible(isShouldVisible());
        updateFont();
        
        if(hasPage){
            if(getPage() == null){
                if(MainWindow.mainScreen.hasDocument(false))
                    this.pageNumber = MainWindow.mainScreen.document.getPagesNumber() - 1;
                else return;
            }
            setupGeneral(true, this.text);
        }
        
        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE || (e.getCode() == KeyCode.BACK_SPACE && e.isShortcutDown())){
                e.consume();
                getGradeTreeItem().getPanel().gradeField.setText("");
            }
        });
    }
    
    // SETUP / EVENT CALL BACK
    
    @Override
    protected void setupBindings(){
        
        // Forse to be hide when value == -1
        visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && !isShouldVisible()) setVisible(false);
        });
        
        text.textProperty().addListener((observable, oldValue, newValue) -> {
            checkLocation(false);
        });
        nameProperty().addListener((observable, oldValue, newValue) -> {
            updateText();
            Edition.setUnsave("GradeNameChanged");
            
            // Check if name is null
            if(newValue.isBlank()){
                setName(TR.tr("gradeTab.gradeDefaultName"));
                return;
            }
            // Check if exist twice
            GradeTreeItem treeItemElement = getGradeTreeItem();
            if(!treeItemElement.isRoot() && ((GradeTreeItem) treeItemElement.getParent()).doExistTwice(getName())){
                setName(getName() + "(1)");
                return;
            }
            
            // Redefine children parentPath
            if(treeItemElement.hasSubGrade()) treeItemElement.resetParentPathChildren();
            
            // Update total if switch/unSwitch to Bonus
            if(GradeElement.isBonus(oldValue) != GradeElement.isBonus(newValue)){
                if(treeItemElement.hasSubGrade() && getValue() == -1) treeItemElement.resetChildrenValues();
                else if(!treeItemElement.isRoot()) ((GradeTreeItem) treeItemElement.getParent()).makeSum(false);
            }
            
            // When this is called due to a undo action, need to update GradeTreeItem
            if(UndoEngine.isUndoingThings){
                GradeTreeItem treeItem = getGradeTreeItem();
                if(treeItem != null){
                    treeItem.getPanel().nameField.setText(newValue);
                }
            }else{
                // New word added OR this is the first registration of this action/property.
                if(StringUtils.countSpaces(oldValue) != StringUtils.countSpaces(newValue)
                        || !UndoEngine.isNextUndoActionProperty(nameProperty())){
                    
                    MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, nameProperty(), oldValue.trim(), UType.UNDO));
                }
            }
        });
        // make sum when value or total change
        valueProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave("GradeValueChanged");
            if(!isShouldVisible()){
                setVisible(false);
                updateText();
            }else{
                if(oldValue.intValue() == -1 && !isAlwaysVisible()){ // Deviens visible
                    
                    if(MainWindow.mainScreen.document.getLastCursorOverPage() != -1 && MainWindow.mainScreen.document.getLastCursorOverPage() != getPage().getPage()){
                        switchPage(MainWindow.mainScreen.document.getLastCursorOverPage());
                    }
                    setRealX(getPage().toGridX(getPage().getMouseX() <= 0 ? 60 : getPage().getMouseX()));
                    
                    if(nextRealYToUse != 0){
                        setRealY(nextRealYToUse);
                        nextRealYToUse = 0;
                    }else{
                        setRealY(getPage().getNewElementYOnGrid());
                        centerOnCoordinatesY();
                        select();
                    }
                }
                setVisible(true);
                updateText();
            }
            
            GradeTreeItem treeItem = getGradeTreeItem();
            if(treeItem.hasSubGrade() && newValue.intValue() == -1) treeItem.resetChildrenValues();
            else if(!treeItem.isRoot())
                ((GradeTreeItem) treeItem.getParent()).makeSum(getPageNumber(), getRealY());
            
            // When this is called due to a undo action, need to update GradeTreeItem
            if(UndoEngine.isUndoingThings){
                treeItem.getPanel().gradeField.setText(newValue.doubleValue() == -1 ? "" : MainWindow.gradesDigFormat.format(newValue));
            }else if(!getGradeTreeItem().hasSubGrade()){ // Parents have an auto-defined value so otherwise, this is useless
                // This is the first registration of this action/property.
                if(!UndoEngine.isNextUndoActionProperty(nameProperty())){
                    MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, valueProperty(), oldValue, UType.UNDO));
                }
            }
        });
        totalProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave("GradeTotalChanged");
            updateText();
            
            if((GradeTreeView.getTotal()).getCore().equals(this)) return; // This is Root
            ((GradeTreeItem) MainWindow.gradeTab.treeView.getGradeTreeItem(GradeTreeView.getTotal(), this).getParent()).makeSum(false);
            
            
            // When this is called due to a undo action, need to update GradeTreeItem
            if(UndoEngine.isUndoingThings){
                GradeTreeItem treeItem = getGradeTreeItem();
                if(treeItem != null)
                    treeItem.getPanel().totalField.setText(MainWindow.gradesDigFormat.format(newValue));
                
            }else if(!getGradeTreeItem().hasSubGrade()){ // Parents have an auto-defined total so otherwise, this is useless
                // This is the first registration of this action/property.
                if(!UndoEngine.isNextUndoActionProperty(nameProperty())){
                    MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, totalProperty(), oldValue, UType.UNDO));
                }
            }
        });
        
        alwaysVisible.addListener((observable, oldValue, newValue) -> {
            updateFont();
            setupMenu();
            getGradeTreeItem().setChildrenAlwaysVisible(isAlwaysVisible(), false);
            
            // The undo system is in the setter method to prevent multiple undo for only one action.
        });
    }
    
    @Override
    protected void setupMenu(){
        menu.getItems().clear();
        
        NodeMenuItem item1 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.setMax"), false);
        item1.setToolTip(TR.tr("gradeTab.gradeMenu.setMax.tooltip"));
        NodeMenuItem item2 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.unFill"), false);
        item2.setToolTip(TR.tr("gradeTab.gradeMenu.unFill.tooltip"));
        NodeMenuItem item3 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.delete"), false);
        item3.setToolTip(TR.tr("gradeTab.gradeMenu.delete.tooltip"));
        item3.disableProperty().bind(MainWindow.gradeTab.isLockGradeScaleProperty());
        NodeMenuItem item4 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.set0"), false);
        item4.setToolTip(TR.tr("gradeTab.gradeMenu.set0.tooltip"));
        NodeMenuItem item5 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.hideUnfilled"), false);
        item5.setToolTip(TR.tr("gradeTab.gradeMenu.hideUnfilled.tooltip"));
        
        
        menu.setOnShowing((e) -> {
            Platform.runLater(() -> {
                MenuItem menuItem = getGradeTreeItem().getEditMenuItem(menu);
                
                if(menu.getItems().size() == 4) menu.getItems().add(0, menuItem);
                else menu.getItems().set(0, menuItem);
                NodeMenuItem.setupMenuNow(menu);
            });
            
        });
        
        menu.getItems().addAll(item1, item4, item2, item3);
        if(isAlwaysVisible() && getValue() == -1) menu.getItems().add(item5);
        
        item1.setOnAction(e -> {
            GradeTreeItem treeItemElement = getGradeTreeItem();
            treeItemElement.getPanel().gradeField.setText(MainWindow.gradesDigFormat.format(getTotal()));
            treeItemElement.setChildrenValuesToMax();
        });
        item2.setOnAction(e -> {
            GradeTreeItem treeItemElement = getGradeTreeItem();
            treeItemElement.getPanel().gradeField.setText("");
            treeItemElement.resetChildrenValues();
        });
        item3.setOnAction(e -> {
            if((GradeTreeView.getTotal()).getCore().equals(this)){
                // Regenerate Root if this is Root
                MainWindow.gradeTab.treeView.clearElements(true, true);
            }else delete(true, UType.UNDO);
        });
        item4.setOnAction(e -> {
            GradeTreeItem treeItemElement = getGradeTreeItem();
            treeItemElement.getPanel().gradeField.setText("0");
            treeItemElement.setChildrenValuesTo0();
            if(!getGradeTreeItem().hasSubGrade()){
                AutoTipsManager.showByAction("gradereset");
            }
        });
        item5.setOnAction(e -> {
            setAlwaysVisible(false, true);
        });
    }
    
    @Override
    protected void onMouseRelease(){
        GradeTreeView.defineNaNLocations();
    }
    
    
    // ACTIONS
    
    @Override
    public void select(){
        super.selectPartial();
        // Sélectionne l'élément associé dans l'arbre
        MainWindow.gradeTab.treeView.getSelectionModel().select(getGradeTreeItem());
        AutoTipsManager.showByAction("gradeselect");
    }
    @Override
    public void onDoubleClickAfterSelected(){
    
    }
    @Override
    public void onDoubleClick(){
        if(!getGradeTreeItem().hasSubGrade()){
            setValue(0);
            AutoTipsManager.showByAction("gradereset");
        }
    }
    
    @Override
    public void delete(boolean markAsUnsave, UType undoType){
        if(getPage() != null){
            getPage().removeElement(this, markAsUnsave, undoType);
        }
    }
    
    @Override
    public void addedToDocument(boolean markAsUnsave){
        MainWindow.gradeTab.treeView.addElement(this);
    }
    @Override
    public void removedFromDocument(boolean markAsUnsave){
        super.removedFromDocument(markAsUnsave);
        MainWindow.gradeTab.treeView.removeElement(this, markAsUnsave);
    }
    // READER AND WRITERS
    
    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("page", getPageNumber());
        data.put("index", index);
        data.put("parentPath", parentPath);
        data.put("value", value.getValue());
        data.put("total", total.getValue());
        data.put("outOfTotal", outOfTotal.getValue());
        data.put("name", name.getValue());
        data.put("alwaysVisible", alwaysVisible.get());
        
        return data;
    }
    
    public static double[] getYAMLDataStats(HashMap<String, Object> data){
        // 2args (Root) : [0] => Value [1] => Total  |  1args (Other) : [0] => Value
        String parentPath = Config.getString(data, "parentPath");
        double value = Config.getDouble(data, "value");
        double total = Config.getDouble(data, "total");
        double outOfTotal = Config.getDouble(data, "outOfTotal");
        if(outOfTotal > 0){
            value = value / total * outOfTotal;
            total = outOfTotal;
        }
        
        if(StringUtils.cleanArray(parentPath.split(Pattern.quote("\\"))).length == 0)
            return new double[]{value, total};
        else return new double[]{value};
    }
    
    public static GradeElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage, boolean upscaleGrid){
        
        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int page = (int) Config.getLong(data, "page");
        int index = (int) Config.getLong(data, "index");
        String parentPath = Config.getString(data, "parentPath");
        double value = Config.getDouble(data, "value");
        double total = Config.getDouble(data, "total");
        double outOfTotal = Config.getDouble(data, "outOfTotal");
        boolean alwaysVisible = Config.getBoolean(data, "alwaysVisible");
        String name = Config.getString(data, "name");
        
        if(upscaleGrid){ // Between 1.2.1 and 1.3.0, the grid size was multiplied by 100
            x *= 100;
            y *= 100;
        }
        
        return new GradeElement(x, y, page, hasPage, value, total, outOfTotal, index, parentPath, name, alwaysVisible);
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data, boolean upscaleGrid){
        GradeElement element = readYAMLDataAndGive(data, true, upscaleGrid);
        
        if(MainWindow.mainScreen.document.getPagesNumber() > element.getPageNumber())
            MainWindow.mainScreen.document.getPage(element.getPageNumber()).addElement(element, false, UType.NO_UNDO);
    }
    
    
    // SPECIFIC METHODS
    
    public void updateText(){
        String outOfText = "";
        if(isRoot() && getOutOfTotal() > 0){
            outOfText = "\n->";
            if(getTotal() <= 0) outOfText += "0/";
            else outOfText += MainWindow.twoDigFormat.format(getValue() / getTotal() * getOutOfTotal()) + "/";
            outOfText += MainWindow.twoDigFormat.format(getOutOfTotal());
        }
        
        text.setText((GradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "")
                + (getValue() == -1 ? "?" : MainWindow.gradesDigFormat.format(getValue()))
                + "/" + MainWindow.gradesDigFormat.format(getTotal()) + outOfText);
    }
    
    public boolean isFilled(){
        return getValue() != -1 || isAlwaysVisible();
    }
    
    public boolean isShouldVisible(){
        return isShouldVisibleOnExport() || isAlwaysVisible();
    }
    
    public boolean isShouldVisibleOnExport(){
        int tier = GradeTreeView.getElementTier(parentPath);
        return getValue() != -1
                && !GradeTab.getTierHide(tier)
                && !(GradeTab.getTierHideWhenAllPoints(tier) && getValue() == getTotal());
    }
    
    public void updateFont(){
        text.setFont(GradeTab.getTierFont(GradeTreeView.getElementTier(parentPath)));
        text.setFill(GradeTab.getTierColor(GradeTreeView.getElementTier(parentPath)));
        updateText();
        setVisible(isShouldVisible());
    }
    
    public GradeTreeItem getGradeTreeItem(){
        GradeTreeItem treeItemElement;
        if(isRoot()) treeItemElement = GradeTreeView.getTotal();
        else treeItemElement = MainWindow.gradeTab.treeView.getGradeTreeItem(GradeTreeView.getTotal(), this);
        
        return treeItemElement;
    }
    
    public boolean isDefaultGrade(){
        return (getValue() == -1 && getTotal() == 0 && getName().equals(TR.tr("gradeTab.gradeDefaultName.total")));
    }
    
    public boolean isRoot(){
        return getParentPath().isEmpty();
    }
    
    public boolean isBonus(){
        return isBonus(getName());
    }
    
    public static boolean isBonus(String name){
        return name.toLowerCase().startsWith(TR.tr("gradeTab.gradeDefaultName.bonus").toLowerCase());
    }
    
    public float getBaseLineY(){
        return (float) (text.getBaselineOffset());
    }
    
    @Override
    public float getBoundsHeight(){
        return (float) text.getLayoutBounds().getHeight();
    }
    
    public float getBoundsWidth(){
        return (float) text.getLayoutBounds().getWidth();
    }
    
    // ELEMENT DATA GETTERS AND SETTERS
    
    @Override
    public String getElementName(boolean plural){
        return getElementNameStatic(plural);
    }
    public static String getElementNameStatic(boolean plural){
        if(plural) return TR.tr("elements.name.grades");
        else return TR.tr("elements.name.grade");
    }
    
    public String getName(){
        return name.get();
    }
    
    public StringProperty nameProperty(){
        return name;
    }
    
    public void setName(String name){
        this.name.set(name);
    }
    
    public double getValue(){
        return value.get();
    }
    
    public double getVisibleValue(){
        return Math.max(0, value.get());
    }
    
    public DoubleProperty valueProperty(){
        return value;
    }
    
    public void setValue(double value){
        this.value.set(value);
    }
    
    public double getTotal(){
        return total.get();
    }
    
    public DoubleProperty totalProperty(){
        return total;
    }
    
    public void setTotal(double total){
        this.total.set(total);
    }
    
    public double getOutOfTotal(){
        return outOfTotal.get();
    }
    public DoubleProperty outOfTotalProperty(){
        return outOfTotal;
    }
    public void setOutOfTotal(double outOfTotal){
        this.outOfTotal.set(outOfTotal);
    }
    public int getIndex(){
        return index;
    }
    
    public void setIndex(int index){
        this.index = index;
        Edition.setUnsave("GradeIndexChanged");
    }
    
    public String getParentPath(){
        return parentPath;
    }
    
    public String getPath(){
        return getParentPath() + "\\" + getName();
    }
    
    public String[] getParentPathArray(){
        return StringUtils.cleanArray(getParentPath().split(Pattern.quote("\\")));
    }
    
    public void setParentPath(String parentPath){
        this.parentPath = parentPath;
        Edition.setUnsave("GradeParentPathChanged");
    }
    
    public boolean isAlwaysVisible(){
        return alwaysVisible.get();
    }
    
    public void setAlwaysVisible(boolean alwaysVisible, boolean registerUndo){
        if(registerUndo)
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, this.alwaysVisible, this.alwaysVisible.get(), UType.UNDO));
        this.alwaysVisible.set(alwaysVisible);
    }
    
    // shortcuts
    public String getText(){
        return text.getText();
    }
    
    public StringProperty textProperty(){
        return text.textProperty();
    }
    
    public void setText(String text){
        this.text.setText(text);
    }
    
    public void setColor(Color color){
        text.setFill(color);
    }
    
    public ObjectProperty<Paint> fillProperty(){
        return text.fillProperty();
    }
    
    public Color getColor(){
        return (Color) text.getFill();
    }
    
    public java.awt.Color getAwtColor(){
        return new java.awt.Color((float) getColor().getRed(),
                (float) getColor().getGreen(),
                (float) getColor().getBlue(),
                (float) getColor().getOpacity());
    }
    
    public void setFont(Font font){
        text.setFont(font);
    }
    
    public ObjectProperty<Font> fontProperty(){
        return text.fontProperty();
    }
    
    public Font getFont(){
        return text.getFont();
    }
    
    // TRANSFORMATIONS
    
    @Override
    public Element clone(){
        return new GradeElement(getRealX(), getRealY(), pageNumber, true, value.getValue(), total.getValue(), outOfTotal.getValue(), index, parentPath, name.getValue(), alwaysVisible.get());
    }
    
    public GradeRating toGradeRating(){
        return new GradeRating(value.get(), total.get(), outOfTotal.get(), name.get(), index, parentPath, isAlwaysVisible(), getRealX(), getRealY(), pageNumber);
    }
    
    public GradeTreeItem toGradeTreeItem(){
        return new GradeTreeItem(this);
    }
    
    // SORTING
    
    public static ArrayList<GradeElement> sortGrades(List<GradeElement> grades){
        
        ArrayList<GradeElement> gradesOutput = new ArrayList<>(grades.size());
        gradesOutput.addAll(grades);
        
        gradesOutput.sort((grade1, grade2) -> grade1.compareTo(grade2, grades));
        return gradesOutput;
    }
    
    public static List<Element> sortGradesBetweenNormalElements(List<Element> elements){
        
        List<GradeElement> gradeElements = elements.stream()
                .filter(element -> element instanceof GradeElement)
                .map(element -> (GradeElement) element)
                .collect(Collectors.toList());
        
        List<Element> otherElements = elements.stream()
                .filter(element -> !(element instanceof GradeElement))
                .collect(Collectors.toList());
        
        
        gradeElements = sortGrades(gradeElements);
        
        otherElements.addAll(gradeElements);
        return otherElements;
    }
    
    public int compareTo(GradeElement grade, List<GradeElement> grades){
        
        // grade1 is the parent of grade2 ?
        if(getParentPathArray().length < grade.getParentPathArray().length){
            if(grade.getParentPath().contains(getPath())){
                return -1;
            }
        }
        
        // grade2 is the parent of grade1 ?
        if(grade.getParentPathArray().length < getParentPathArray().length){
            if(getParentPath().contains(grade.getPath())){
                return 1;
            }
        }
        
        GradeElement grade1Parent = this;
        GradeElement grade2Parent = grade;
        
        // while grades are at the same level
        while(!grade1Parent.getParentPath().equals(grade2Parent.getParentPath())){
            if(grade1Parent.getParentPathArray().length < grade2Parent.getParentPathArray().length){ // grade1Parent is at a higher level
                for(GradeElement parent : grades){
                    if((parent.getPath()).equals(grade2Parent.getParentPath()))
                        grade2Parent = parent; // get the parent of grade2Parent
                }
            }else{
                for(GradeElement parent : grades){
                    if((parent.getPath()).equals(grade1Parent.getParentPath()))
                        grade1Parent = parent; // get the parent of grade1Parent
                }
            }
        }
        return grade1Parent.getIndex() - grade2Parent.getIndex();
    }
    
}