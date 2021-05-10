package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.components.menus.NodeMenu;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.Edition;
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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradeElement extends Element{
    
    private ScratchText text = new ScratchText();
    
    private StringProperty name;
    private DoubleProperty value;
    private DoubleProperty total;
    private int index;
    private String parentPath;
    private boolean alwaysVisible;
    
    public int nextRealYToUse = 0;
    
    public GradeElement(int x, int y, int pageNumber, boolean hasPage, double value, double total, int index, String parentPath, String name, boolean alwaysVisible){
        super(x, y, pageNumber);
        this.pageNumber = pageNumber;
        this.realX.set(x);
        this.realY.set(y);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleDoubleProperty(value);
        this.total = new SimpleDoubleProperty(total);
        this.index = index;
        this.parentPath = parentPath;
        this.alwaysVisible = alwaysVisible;
        
        text.setBoundsType(TextBoundsType.LOGICAL);
        text.setTextOrigin(VPos.TOP);
        
        setVisible(isShouldVisible());
        updateFont();
        
        if(hasPage){
            if(getPage() == null){
                if(MainWindow.mainScreen.hasDocument(false))
                    this.pageNumber = MainWindow.mainScreen.document.pages.size() - 1;
                else return;
            }
            setupGeneral(true, this.text);
        }
        
        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE || (e.getCode() == KeyCode.BACK_SPACE && e.isShortcutDown())){
                e.consume();
                getGradeTreeItem().gradeField.setText("");
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
            
            text.setText((GradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "?" : MainWindow.gradesDigFormat.format(getValue())) + "/" + MainWindow.gradesDigFormat.format(getTotal()));
            Edition.setUnsave();
            
            // Check if name is null
            if(newValue.isBlank()){
                setName(TR.tr("gradeTab.gradeDefaultName"));
                return;
            }
            // Check if exist twice
            GradeTreeItem treeItemElement = getGradeTreeItem();
            if(!treeItemElement.isRoot() && ((GradeTreeItem) treeItemElement.getParent()).isExistTwice(getName()))
                setName(getName() + "(1)");
            
            // Redefine children parentPath
            if(treeItemElement.hasSubGrade()) treeItemElement.resetParentPathChildren();
            
            // Update total if switch/unSwitch to Bonus
            if(GradeElement.isBonus(oldValue) != GradeElement.isBonus(newValue)){
                if(treeItemElement.hasSubGrade() && getValue() == -1) treeItemElement.resetChildrenValues();
                else if(!treeItemElement.isRoot()) ((GradeTreeItem) treeItemElement.getParent()).makeSum(false);
            }
        });
        // make sum when value or total change
        valueProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave();
            if(!isShouldVisible()){
                setVisible(false);
                text.setText((GradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "?" : MainWindow.gradesDigFormat.format(getValue())) + "/" + MainWindow.gradesDigFormat.format(getTotal()));
            }else{
                if(oldValue.intValue() == -1 && !alwaysVisible){ // Deviens visible
                    
                    if(MainWindow.mainScreen.document.getLastCursorOverPage() != -1 && MainWindow.mainScreen.document.getLastCursorOverPage() != getPage().getPage()){
                        switchPage(MainWindow.mainScreen.document.getLastCursorOverPage());
                    }
                    setRealX((int) ((getPage().getMouseX() <= 0 ? 60 : getPage().getMouseX()) * Element.GRID_WIDTH / getPage().getWidth()));
                    
                    if(nextRealYToUse != 0){
                        setRealY(nextRealYToUse);
                        nextRealYToUse = 0;
                    }else{
                        setRealY((int) (getPage().getMouseY() * Element.GRID_HEIGHT / getPage().getHeight()));
                        centerOnCoordinatesY();
                    }
                }
                setVisible(true);
                text.setText((GradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "?" : MainWindow.gradesDigFormat.format(getValue())) + "/" + MainWindow.gradesDigFormat.format(getTotal()));
            }
            
            GradeTreeItem treeItemElement = getGradeTreeItem();
            if(treeItemElement.hasSubGrade() && newValue.intValue() == -1) treeItemElement.resetChildrenValues();
            else if(!treeItemElement.isRoot())
                ((GradeTreeItem) treeItemElement.getParent()).makeSum(getPageNumber(), getRealY());
            
        });
        totalProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave();
            text.setText((GradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "?" : MainWindow.gradesDigFormat.format(getValue())) + "/" + MainWindow.gradesDigFormat.format(getTotal()));
            
            if((GradeTreeView.getTotal()).getCore().equals(this)) return; // This is Root
            ((GradeTreeItem) MainWindow.gradeTab.treeView.getGradeTreeItem(GradeTreeView.getTotal(), this).getParent()).makeSum(false);
        });
    }
    
    @Override
    protected void setupMenu(){
        menu.getItems().clear();
        
        NodeMenuItem item1 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.setMax"));
        item1.setToolTip(TR.tr("gradeTab.gradeMenu.setMax.tooltip"));
        NodeMenuItem item2 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.unFill"));
        item2.setToolTip(TR.tr("gradeTab.gradeMenu.unFill.tooltip"));
        NodeMenuItem item3 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.delete"));
        item3.setToolTip(TR.tr("gradeTab.gradeMenu.delete.tooltip"));
        item3.disableProperty().bind(MainWindow.gradeTab.isLockGradeScaleProperty());
        NodeMenuItem item4 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.set0"));
        item4.setToolTip(TR.tr("gradeTab.gradeMenu.set0.tooltip"));
        NodeMenuItem item5 = new NodeMenuItem(TR.tr("gradeTab.gradeMenu.hideUnfilled"));
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
        if(alwaysVisible && getValue() == -1) menu.getItems().add(item5);
        
        item1.setOnAction(e -> {
            GradeTreeItem treeItemElement = getGradeTreeItem();
            treeItemElement.gradeField.setText(MainWindow.gradesDigFormat.format(getTotal()));
            treeItemElement.setChildrenValuesToMax();
        });
        item2.setOnAction(e -> {
            GradeTreeItem treeItemElement = getGradeTreeItem();
            treeItemElement.gradeField.setText("");
            treeItemElement.resetChildrenValues();
        });
        item3.setOnAction(e -> {
            if((GradeTreeView.getTotal()).getCore().equals(this)){
                // Regenerate Root if this is Root
                delete();
                MainWindow.gradeTab.treeView.generateRoot(true);
            }else delete();
        });
        item4.setOnAction(e -> {
            GradeTreeItem treeItemElement = getGradeTreeItem();
            treeItemElement.gradeField.setText("0");
            treeItemElement.setChildrenValuesTo0();
            if(!getGradeTreeItem().hasSubGrade()){
                AutoTipsManager.showByAction("gradereset");
            }
        });
        item5.setOnAction(e -> {
            setAlwaysVisible(false);
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
    public void doubleClick(){
        if(!getGradeTreeItem().hasSubGrade()){
            setValue(0);
            AutoTipsManager.showByAction("gradereset");
        }
    }
    
    @Override
    public void delete(){
        if(getPage() != null){
            getPage().removeElement(this, !isRoot());
        }
    }
    
    @Override
    public void addedToDocument(boolean silent){
        MainWindow.gradeTab.treeView.addElement(this);
    }
    
    @Override
    public void removedFromDocument(boolean silent){
        MainWindow.gradeTab.treeView.removeElement(this);
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
        data.put("name", name.getValue());
        data.put("alwaysVisible", alwaysVisible);
        
        return data;
    }
    
    public static double[] getYAMLDataStats(HashMap<String, Object> data){
        // 2args (Root) : [0] => Value [1] => Total  |  1args (Other) : [0] => Value
        String parentPath = Config.getString(data, "parentPath");
        double value = Config.getDouble(data, "value");
        double total = Config.getDouble(data, "total");
        
        if(StringUtils.cleanArray(parentPath.split(Pattern.quote("\\"))).length == 0)
            return new double[]{value, total};
        else return new double[]{value};
    }
    
    public static GradeElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage){
        
        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int page = (int) Config.getLong(data, "page");
        int index = (int) Config.getLong(data, "index");
        String parentPath = Config.getString(data, "parentPath");
        double value = Config.getDouble(data, "value");
        double total = Config.getDouble(data, "total");
        boolean alwaysVisible = Config.getBoolean(data, "alwaysVisible");
        String name = Config.getString(data, "name");
        
        return new GradeElement(x, y, page, hasPage, value, total, index, parentPath, name, alwaysVisible);
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data){
        GradeElement element = readYAMLDataAndGive(data, true);
        if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
            MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElement(element, false);
    }
    
    public static GradeElement readDataAndGive(DataInputStream reader, boolean hasPage) throws IOException{
        
        byte page = reader.readByte();
        short x = reader.readShort();
        short y = reader.readShort();
        int index = reader.readInt();
        String parentPath = reader.readUTF();
        double value = reader.readDouble();
        double total = reader.readDouble();
        String name = reader.readUTF();
        
        return new GradeElement(x, y, page, hasPage, value, total, index, parentPath, name, false);
    }
    
    public static void readDataAndCreate(DataInputStream reader) throws IOException{
        GradeElement element = readDataAndGive(reader, true);
        element.setRealY((int) (element.getRealY() - element.getBaseLineY() / element.getPage().getHeight() * Element.GRID_HEIGHT));
        if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
            MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElement(element, false);
    }
    
    // SPECIFIC METHODS
    
    public boolean isFilled(){
        return getValue() != -1 || alwaysVisible;
    }
    
    public boolean isShouldVisible(){
        return isShouldVisibleOnExport() || alwaysVisible;
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
        text.setText((GradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "?" : MainWindow.gradesDigFormat.format(getValue())) + "/" + MainWindow.gradesDigFormat.format(getTotal()));
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
    public float getAlwaysHeight(){
        return (float) text.getLayoutBounds().getHeight();
    }
    
    // ELEMENT DATA GETTERS AND SETTERS
    
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
    
    public int getIndex(){
        return index;
    }
    
    public void setIndex(int index){
        this.index = index;
        Edition.setUnsave();
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
        Edition.setUnsave();
    }
    
    public boolean isAlwaysVisible(){
        return alwaysVisible;
    }
    
    public void setAlwaysVisible(boolean alwaysVisible){
        this.alwaysVisible = alwaysVisible;
        updateFont();
        setupMenu();
        if(!alwaysVisible){
            GradeTreeItem treeItemElement = getGradeTreeItem();
            treeItemElement.setChildrenAlwaysVisibleToFalse();
        }
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
        return new GradeElement(getRealX(), getRealY(), pageNumber, true, value.getValue(), total.getValue(), index, parentPath, name.getValue(), alwaysVisible);
    }
    
    public GradeRating toGradeRating(){
        return new GradeRating(total.get(), name.get(), index, parentPath, alwaysVisible, getRealX(), getRealY(), pageNumber);
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