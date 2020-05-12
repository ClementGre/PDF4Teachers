package fr.themsou.document.editions.elements;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.grades.LBGradeTab;
import fr.themsou.panel.leftBar.grades.GradeRating;
import fr.themsou.panel.leftBar.grades.GradeTreeItem;
import fr.themsou.panel.leftBar.grades.GradeTreeView;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import fr.themsou.yaml.Config;
import javafx.beans.property.*;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class GradeElement extends Element {

    private Text text = new Text();

    private StringProperty name;
    private DoubleProperty value;
    private DoubleProperty total;
    private int index;
    private String parentPath;

    public GradeElement(int x, int y, int pageNumber, String name, double value, double total, int index, String parentPath, boolean hasPage){
        super(x, y, pageNumber);
        this.pageNumber = pageNumber;
        this.realX.set(x);
        this.realY.set(y);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleDoubleProperty(value);
        this.total = new SimpleDoubleProperty(total);
        this.index = index;
        this.parentPath = parentPath;

        text.setBoundsType(TextBoundsType.LOGICAL);
        text.setTextOrigin(VPos.TOP);

        setVisible(getValue() != -1);
        updateFont();

        if(hasPage && getPage() != null) setupGeneral(this.text);

        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE){
                getGradeTreeItem().gradeField.setText("");
            }
        });
    }

    // SETUP / EVENT CALL BACK

    @Override
    protected void setupBindings() {
        // Forse to be hide when value == -1
        visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && getValue() == -1) setVisible(false);
        });

        text.textProperty().addListener((observable, oldValue, newValue) -> {
            checkLocation(false);
        });
        nameProperty().addListener((observable, oldValue, newValue) -> {

            text.setText((LBGradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "" : Main.format.format(getValue())) + "/" + Main.format.format(getTotal()));

            Edition.setUnsave();
            if(newValue.isBlank()){
                setName(TR.tr("Nouvelle note"));
                return;
            }

            GradeTreeItem treeItemElement;
            if(getParentPath().isEmpty()) treeItemElement = (GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot();
            else{
                treeItemElement = MainWindow.lbGradeTab.treeView.getGradeTreeItem((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot(), this);
                // Check if exist twice
                if(((GradeTreeItem) treeItemElement.getParent()).isExistTwice(getName())) setName(getName() + "(1)");
            }

            // ReIndex childrens
            if(treeItemElement.hasSubGrade()) treeItemElement.resetParentPathChildren();
        });
        // make sum when value or total change
        valueProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave();

            if(newValue.intValue() == -1){
                setVisible(false);
                text.setText((LBGradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + newValue + "/" + Main.format.format(getTotal()));
            }else{
                if(oldValue.intValue() == -1){ // Deviens visible
                    if(MainWindow.mainScreen.document.getCurrentPage() != -1 && MainWindow.mainScreen.document.getCurrentPage() != getPage().getPage()){
                        switchPage(MainWindow.mainScreen.document.getCurrentPage());
                    }
                    setRealX((int) ((getPage().getMouseX() <= 0 ? 60 : getPage().getMouseX()) * Element.GRID_WIDTH / getPage().getWidth()));
                    setRealY((int) (getPage().getMouseY() * Element.GRID_HEIGHT / getPage().getHeight()));
                }
                setVisible(true);
                text.setText((LBGradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + Main.format.format(newValue) + "/" + Main.format.format(getTotal()));
            }

            GradeTreeItem treeItemElement = getGradeTreeItem();
            if(treeItemElement.isRoot()){// this is Root
                if(newValue.intValue() == -1) ((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot()).resetChildrenValues();
            }else{
                if(treeItemElement.hasSubGrade() && newValue.intValue() == -1) treeItemElement.resetChildrenValues();
                ((GradeTreeItem) treeItemElement.getParent()).makeSum();
            }
        });
        totalProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave();
            text.setText((LBGradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + Main.format.format(getValue()) + "/" + Main.format.format(getTotal()));

            if(((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot()).getCore().equals(this)) return; // This is Root
            ((GradeTreeItem) MainWindow.lbGradeTab.treeView.getGradeTreeItem((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot(), this).getParent()).makeSum();
        });
    }
    @Override
    protected void setupMenu() {
        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Réinitialiser"), -1, false);
        item1.setToolTip(TR.tr("Réinitialise la note entrée et toutes ses sous-notes."));
        item1.setToolTip(TR.tr("suppr"));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Supprimer du barème"), -1, false);
        item2.setToolTip(TR.tr("Supprime cet élément du barème et de l'édition."));
        item2.disableProperty().bind(MainWindow.lbGradeTab.isLockGradeScaleProperty());
        menu.getItems().addAll(item1, item2);
        Builders.setMenuSize(menu);

        item1.setOnAction(e -> {
            GradeTreeItem treeItemElement;
            if(((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot();
            else treeItemElement = MainWindow.lbGradeTab.treeView.getGradeTreeItem((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot(), this);
            treeItemElement.gradeField.setText("");
        });
        item2.setOnAction(e -> {
            if(((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot()).getCore().equals(this)){
                // Regenerate Root if this is Root
                delete();
                MainWindow.lbGradeTab.treeView.generateRoot(true);
            }else delete();
        });
    }
    @Override
    protected void onMouseRelease() {
        GradeTreeView.defineNaNLocations();
    }

    // ACTIONS

    @Override
    public void select() {
        super.selectPartial();
        MainWindow.leftBar.getSelectionModel().select(2);

        // Sélectionne l'élément associé dans l'arbre
        MainWindow.lbGradeTab.treeView.getSelectionModel().select(getGradeTreeItem());

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

        return data;
    }
    public static double[] getYAMLDataStats(HashMap<String, Object> data){
        // 2args (Root) : [0] => Value [1] => Total  |  1args (Other) : [0] => Value
        String parentPath = Config.getString(data, "parentPath");
        double value = Config.getDouble(data, "value");
        double total = Config.getDouble(data, "total");

        if(Builders.cleanArray(parentPath.split(Pattern.quote("\\"))).length == 0) return new double[]{value, total};
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
        String name = Config.getString(data, "name");

        return new GradeElement(x, y, page, name, value, total, index, parentPath, hasPage);
    }
    public static void readYAMLDataAndCreate(HashMap<String, Object> data){
        GradeElement element = readYAMLDataAndGive(data, true);
        if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
            MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElementSimple(element);
    }

    public static GradeElement readDataAndGive(DataInputStream reader, boolean hasPage) throws IOException {

        byte page = reader.readByte();
        short x = reader.readShort();
        short y = reader.readShort();
        int index = reader.readInt();
        String parentPath = reader.readUTF();
        double value = reader.readDouble();
        double total = reader.readDouble();
        String name = reader.readUTF();

        return new GradeElement(x, y, page, name, value, total, index, parentPath, hasPage);
    }
    public static void readDataAndCreate(DataInputStream reader) throws IOException {
        GradeElement element = readDataAndGive(reader, true);
        if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
            MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElementSimple(element);
    }

    // SPECIFIC METHODS

    public void updateFont(){
        text.setFont(LBGradeTab.getTierFont(GradeTreeView.getElementTier(parentPath)));
        text.setFill(LBGradeTab.getTierColor(GradeTreeView.getElementTier(parentPath)));
        text.setText((LBGradeTab.getTierShowName(GradeTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "" : Main.format.format(getValue())) + "/" + Main.format.format(getTotal()));
    }
    public GradeTreeItem getGradeTreeItem(){
        GradeTreeItem treeItemElement;
        if(getParentPath().isEmpty()) treeItemElement = (GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot();
        else treeItemElement = MainWindow.lbGradeTab.treeView.getGradeTreeItem((GradeTreeItem) MainWindow.lbGradeTab.treeView.getRoot(), this);
        return treeItemElement;
    }
    public boolean isDefaultRoot(){
        if(getParentPath().isEmpty()){
            return (getValue() == -1 && getTotal() == 20 && getName().equals(TR.tr("Total")));
        }
        return false;
    }

    public float getBaseLineY(){
        return (float) (text.getBaselineOffset());
    }
    @Override
    public float getAlwaysHeight(){
        return (float) text.getLayoutBounds().getHeight();
    }

    // ELEMENT DATA GETTERS AND SETTERS

    public String getName() {
        return name.get();
    }
    public StringProperty nameProperty() {
        return name;
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public double getValue() {
        return value.get();
    }
    public DoubleProperty valueProperty() {
        return value;
    }
    public void setValue(double value) {
        this.value.set(value);
    }
    public double getTotal() {
        return total.get();
    }
    public DoubleProperty totalProperty() {
        return total;
    }
    public void setTotal(double total) {
        this.total.set(total);
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
        Edition.setUnsave();
    }
    public String getParentPath() {
        return parentPath;
    }
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
        Edition.setUnsave();
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
    public Element clone() {
        return new GradeElement(getRealX(), getRealY(), pageNumber, name.getValue(), value.getValue(), total.getValue(), index, parentPath, true);
    }
    public GradeRating toGradeRating(){
        return new GradeRating(total.get(), name.get(), index, parentPath);
    }
    public GradeTreeItem toGradeTreeItem(){
        return new GradeTreeItem(this);
    }
}