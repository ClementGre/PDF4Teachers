package fr.themsou.document.editions.elements;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.notes.LBNoteTab;
import fr.themsou.panel.leftBar.notes.NoteRating;
import fr.themsou.panel.leftBar.notes.NoteTreeItem;
import fr.themsou.panel.leftBar.notes.NoteTreeView;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.beans.property.*;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class NoteElement extends Text implements Element {

    private StringProperty name;
    private DoubleProperty value;
    private DoubleProperty total;
    private int index;
    private String parentPath;

    public ContextMenu menu = new ContextMenu();

    private IntegerProperty realX = new SimpleIntegerProperty();
    private IntegerProperty realY = new SimpleIntegerProperty();

    private int pageNumber; // WARNING : Don't use this value (Only for simpleLoading)
    private int shiftX = 0;
    private int shiftY = 0;

    private int maxY;
    private int maxYPage = 999999;
    private int minY = 0;
    private int minYPage = 0;

    public NoteElement(int x, int y, String name, double value, double total, int index, String parentPath, int pageNumber, PageRenderer page){
        this.pageNumber = pageNumber;
        this.realX.set(x);
        this.realY.set(y);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleDoubleProperty(value);
        this.total = new SimpleDoubleProperty(total);
        this.index = index;
        this.parentPath = parentPath;

        setBoundsType(TextBoundsType.LOGICAL);
        setTextOrigin(VPos.BASELINE);

        setVisible(getValue() != -1);
        updateFont();

        if(page == null) return;
        this.maxY = (int) page.getHeight();

        setCursor(Cursor.MOVE);
        layoutXProperty().bind(page.widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
        layoutYProperty().bind(page.heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));

        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Réinitialiser"), -1, false);
        item1.setToolTip(TR.tr("Réinitialise la note entrée et toutes ses sous-notes."));
        item1.setToolTip(TR.tr("suppr"));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Supprimer du barème"), -1, false);
        item2.setToolTip(TR.tr("Supprime cet élément du barème et de l'édition."));
        item2.disableProperty().bind(MainWindow.lbNoteTab.isLockRatingScaleProperty());
        menu.getItems().addAll(item1, item2);
        Builders.setMenuSize(menu);

        item1.setOnAction(e -> {
            NoteTreeItem treeItemElement;
            if(((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot();
            else treeItemElement = MainWindow.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot(), this);
            treeItemElement.noteField.setText("");
        });
        item2.setOnAction(e -> {
            if(((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot()).getCore().equals(this)){
                // Regenerate Root if this is Root
                delete();
                MainWindow.lbNoteTab.treeView.generateRoot();
            }else delete();

        });

        // Forse to be hide when value == -1
        visibleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && getValue() == -1) setVisible(false);
        });

        textProperty().addListener((observable, oldValue, newValue) -> {
            checkLocation(getLayoutX(), getLayoutY());
        });
        nameProperty().addListener((observable, oldValue, newValue) -> {

            setText((LBNoteTab.getTierShowName(NoteTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "" : Main.format.format(getValue())) + "/" + Main.format.format(getTotal()));

            Edition.setUnsave();
            if(newValue.isBlank()){
                setName(TR.tr("Nouvelle note"));
                return;
            }

            NoteTreeItem treeItemElement;
            if(((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot();
            else{
                treeItemElement = MainWindow.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot(), this);
                // Check if exist twice
                if(((NoteTreeItem) treeItemElement.getParent()).isExistTwice(getName())) setName(getName() + "(1)");
            }

            // ReIndex childrens
            if(treeItemElement.hasSubNote()) treeItemElement.resetParentPathChildren();
        });
        // make sum when value or total change
        valueProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave();

            if(newValue.intValue() == -1){
                setVisible(false);
                setText((LBNoteTab.getTierShowName(NoteTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + newValue + "/" + Main.format.format(getTotal()));
            }else{
                if(oldValue.intValue() == -1){ // Deviens visible
                    if(MainWindow.mainScreen.document.getCurrentPage() != -1 && MainWindow.mainScreen.document.getCurrentPage() != getPage().getPage()){
                        switchPage(MainWindow.mainScreen.document.getCurrentPage());
                    }
                    setRealX((int) ((getPage().getMouseX() <= 60 ? 60 : getPage().getMouseX()) * Element.GRID_WIDTH / getPage().getWidth()));
                    setRealY((int) (getPage().getMouseY() * Element.GRID_HEIGHT / getPage().getHeight()));
                }
                calculateMinAndMaxY();
                setVisible(true);
                setText((LBNoteTab.getTierShowName(NoteTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + Main.format.format(newValue) + "/" + Main.format.format(getTotal()));
            }

            if(((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot()).getCore().equals(this)){// this is Root
                if(newValue.intValue() == -1) ((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot()).resetChildrenValues();

            }else{
                NoteTreeItem treeItemElement = MainWindow.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot(), this);
                if(treeItemElement.hasSubNote() && newValue.intValue() == -1) treeItemElement.resetChildrenValues();
                ((NoteTreeItem) treeItemElement.getParent()).makeSum();
            }
        });
        totalProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave();
            setText((LBNoteTab.getTierShowName(NoteTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + Main.format.format(getValue()) + "/" + Main.format.format(getTotal()));

            if(((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot()).getCore().equals(this)) return; // This is Root
            ((NoteTreeItem) MainWindow.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot(), this).getParent()).makeSum();
        });

        // enable shadow if this element is selected
        MainWindow.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue == this && newValue != this){
                setEffect(null);
                menu.hide();
            }else if(oldValue != this && newValue == this){
                DropShadow ds = new DropShadow();
                ds.setOffsetY(3.0f);
                ds.setColor(Color.color(0f, 0f, 0f));
                setEffect(ds);
                setCache(true);
                requestFocus();
            }
        });

        setOnMousePressed(e -> {
            e.consume();

            shiftX = (int) e.getX();
            shiftY = (int) e.getY();

            calculateMinAndMaxY();

            menu.hide();
            select();

            if(e.getButton() == MouseButton.SECONDARY){
                menu.show(getPage(), e.getScreenX(), e.getScreenY());
            }
        });
        setOnMouseReleased(event -> NoteTreeView.defineNaNLocations());
        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE){
                NoteTreeItem treeItemElement;
                if(((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot();
                else treeItemElement = MainWindow.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot(), this);
                treeItemElement.noteField.setText("");
            }
        });

        setOnMouseDragged(e -> {

            Edition.setUnsave();
            double itemX = getLayoutX() + e.getX() - shiftX;
            double itemY = getLayoutY() + e.getY() - shiftY;

            boolean changePage = false;
            if(getPage().getRealMouseY() < -30 && getPage().getPage() > minYPage){ // Monter d'une page
                if(getPage().getPage() > 0){

                    MainWindow.mainScreen.setSelected(null);
                    switchPage(getPageNumber() -1);

                    itemY = getPage().getHeight();
                    changePage = true;
                }
            }else if(getPage().getRealMouseY() > getPage().getHeight() + 30 && getPage().getPage() < maxYPage){ // Descendre d'une page
                if(getPage().getPage() < MainWindow.mainScreen.document.pages.size()-1){

                    MainWindow.mainScreen.setSelected(null);
                    switchPage(getPageNumber() +1);

                    itemY = 0;
                    changePage = true;
                }
            }

            checkLocation(itemX, itemY);

            if(changePage){
                layoutXProperty().bind(getPage().widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
                layoutYProperty().bind(getPage().heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));
                MainWindow.lbTextTab.onFileTextSortManager.simulateCall();
            }

        });
    }

    // SPECIFIC METHODS

    public void calculateMinAndMaxY(){

        NoteTreeItem treeItemElement;
        if(((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot();
        else treeItemElement = MainWindow.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot(), this);

        NoteTreeItem beforeItem = treeItemElement.getBeforeItem();
        while(beforeItem != null){
            if(beforeItem.getCore().getValue() != -1) break;
            beforeItem = beforeItem.getBeforeItem();
        }

        NoteTreeItem afterItem = treeItemElement.getAfterItem();
        while(afterItem != null){
            if(afterItem.getCore().getValue() != -1) break;
            afterItem = afterItem.getAfterItem();
        }

        if(beforeItem == null){
            minYPage = 0;
            minY = 0;
        }else{
            minYPage = beforeItem.getCore().getPageNumber();
            minY = (int) beforeItem.getCore().getLayoutY();
            minY = Math.max(minY, 0);
        }
        if(afterItem == null){
            maxYPage = 999999;
            maxY = (int) getPage().getHeight();
        }else{
            maxYPage = afterItem.getCore().getPageNumber();
            maxY = (int) (afterItem.getCore().getLayoutY() - afterItem.getCore().getLayoutBounds().getHeight());
            maxY = (int) Math.min(maxY, getPage().getHeight());
        }
    }
    public void updateFont(){
        setFont(LBNoteTab.getTierFont(NoteTreeView.getElementTier(parentPath)));
        setFill(LBNoteTab.getTierColor(NoteTreeView.getElementTier(parentPath)));
        setText((LBNoteTab.getTierShowName(NoteTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "" : Main.format.format(getValue())) + "/" + Main.format.format(getTotal()));
    }

    // CHECK LOCATION

    @Override
    public void checkLocation(double itemX, double itemY){

        setBoundsType(TextBoundsType.VISUAL);

        double height = getLayoutBounds().getHeight();

        int minY = getPageNumber() > minYPage ? 0 : this.minY;
        int maxY = getPageNumber() < maxYPage ? (int) getPage().getHeight() : this.maxY;

        //System.out.println("minY = " + minY + "  |  maxY = " + maxY);

        if(itemY > maxY) itemY = maxY;
        if(itemY < height + minY) itemY = height + minY;

        if(itemX < 0) itemX = 0;
        if(itemX > getPage().getWidth() - getLayoutBounds().getWidth()) itemX = getPage().getWidth() - getLayoutBounds().getWidth();

        setBoundsType(TextBoundsType.LOGICAL);

        realX.set((int) (itemX / getPage().getWidth() * Element.GRID_WIDTH));
        realY.set((int) (itemY / getPage().getHeight() * Element.GRID_HEIGHT));

    }

    // SELECT - DELETE - SWITCH PAGE

    @Override
    public void select() {

        MainWindow.leftBar.getSelectionModel().select(2);
        MainWindow.mainScreen.setSelected(this);
        toFront();

        // Sélectionne l'élément associé dans l'arbre
        NoteTreeItem noteElement;
        if(getParentPath().isEmpty()) noteElement = (NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot();
        else noteElement = MainWindow.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) MainWindow.lbNoteTab.treeView.getRoot(), this);
        MainWindow.lbNoteTab.treeView.getSelectionModel().select(noteElement);

    }
    @Override
    public void delete(){
        if(getPage() != null){
            if(getPage().getChildren().contains(this)) getPage().removeElement(this, true);
        }
    }
    public void switchPage(int page){
        if(true){
            getPage().switchElementPage(this, MainWindow.mainScreen.document.pages.get(page));
            NoteTreeView.defineNaNLocations();
        }
    }

    // READER AND WRITERS

    @Override
    public void writeSimpleData(DataOutputStream writer) throws IOException {
        writer.writeByte(2);
        writeData(writer);
    }
    @Override
    public void writeData(DataOutputStream writer) throws IOException {
        writer.writeByte(pageNumber);
        writer.writeShort(getRealX());
        writer.writeShort(getRealY());
        writer.writeInt(index);
        writer.writeUTF(parentPath);
        writer.writeDouble(value.getValue());
        writer.writeDouble(total.getValue());
        writer.writeUTF(name.getValue());
    }

    public static NoteElement readDataAndGive(DataInputStream reader, boolean hasPage) throws IOException {

        byte page = reader.readByte();
        short x = reader.readShort();
        short y = reader.readShort();
        int index = reader.readInt();
        String parentPath = reader.readUTF();
        double value = reader.readDouble();
        double total = reader.readDouble();
        String name = reader.readUTF();

        return new NoteElement(x, y, name, value, total, index, parentPath, page, hasPage ? MainWindow.mainScreen.document.pages.get(page) : null);
    }
    public static void readDataAndCreate(DataInputStream reader) throws IOException {
        NoteElement element = readDataAndGive(reader, true);
        if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
            MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElementSimple(element);
    }
    // 2args (Root) : [0] => Value [1] => Total  |  1args (Other) : [0] => Value
    public static double[] consumeData(DataInputStream reader) throws IOException {
        reader.readByte();
        reader.readShort();
        reader.readShort();
        reader.readInt();
        String parentPath = reader.readUTF();
        double value = reader.readDouble();
        double total = reader.readDouble();
        reader.readUTF();

        if(Builders.cleanArray(parentPath.split(Pattern.quote("\\"))).length == 0) return new double[]{value, total};
        else return new double[]{value};
    }

    // ELEMENT DATA GETTERS ANS SETTERS

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

    // COORDINATES GETTERS ANS SETTERS

    @Override
    public int getRealX() {
        return realX.get();
    }
    @Override
    public IntegerProperty RealXProperty() {
        return realX;
    }
    @Override
    public void setRealX(int x) {
        this.realX.set(x);
    }
    @Override
    public int getRealY() {
        return realY.get();
    }
    @Override
    public IntegerProperty RealYProperty() {
        return realY;
    }
    @Override
    public void setRealY(int y) {
        this.realY.set(y);
    }

    // PAGE GETTERS ANS SETTERS

    @Override
    public PageRenderer getPage() {
        if(MainWindow.mainScreen.document.pages.size() > pageNumber){
            return MainWindow.mainScreen.document.pages.get(pageNumber);
        }
        return null;
    }
    @Override
    public int getPageNumber() {
        return pageNumber;
    }
    @Override
    public void setPage(PageRenderer page) {
        this.pageNumber = page.getPage();
    }
    @Override
    public void setPage(int pageNumber){
        this.pageNumber = pageNumber;
    }

    // TRANSFORMATIONS

    @Override
    public Element clone() {
        return new NoteElement(getRealX(), getRealY(), name.getValue(), value.getValue(), total.getValue(), index, parentPath, pageNumber, getPage());
    }
    public NoteRating toNoteRating(){
        return new NoteRating(total.get(), name.get(), index, parentPath);
    }
    public NoteTreeItem toNoteTreeItem(){
        return new NoteTreeItem(this);
    }
}