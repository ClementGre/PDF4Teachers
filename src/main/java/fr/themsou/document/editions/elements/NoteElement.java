package fr.themsou.document.editions.elements;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.notes.LBNoteTab;
import fr.themsou.panel.leftBar.notes.NoteTreeItem;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
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

public class NoteElement extends Text implements Element {

    private StringProperty name;
    private DoubleProperty value;
    private DoubleProperty total;
    private int index;
    private String parentPath;

    private IntegerProperty realX = new SimpleIntegerProperty();
    private IntegerProperty realY = new SimpleIntegerProperty();
    private PageRenderer page;

    ContextMenu menu = new ContextMenu();

    private final int pageNumber;
    private int shiftX = 0;
    private int shiftY = 0;

    public NoteElement(int x, int y, String name, double value, double total, int index, String parentPath, PageRenderer page){

        this.pageNumber = page.getPage();
        this.realX.set(x);
        this.realY.set(y);
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleDoubleProperty(value);
        this.total = new SimpleDoubleProperty(total);
        this.index = index;
        this.parentPath = parentPath;

        setFont(LBNoteTab.getTierFont(index));
        setTextOrigin(VPos.BOTTOM);
        setFill(LBNoteTab.getTierColor(index));

        textProperty().bind(this.value.asString().concat("/").concat(this.total));

        setBoundsType(TextBoundsType.LOGICAL);

        this.page = page;

        layoutXProperty().bind(page.widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
        layoutYProperty().bind(page.heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));

        setCursor(Cursor.MOVE);

        // enable shadow if this element is selected
        Main.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
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
        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Supprimer"), -1, false);
        item1.setAccelerator("Suppr");
        item1.setToolTip(TR.tr("Supprime cet élément. Il sera donc retiré de l'édition mais restera sur le barème. La note entrée ne sera plus visible."));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Supprimer du barème"), -1, false);
        item2.setToolTip(TR.tr("Supprime cet élément du barème et de l'édition."));
        item2.disableProperty().bind(LBNoteTab.lockRatingScale);
        menu.getItems().addAll(item1, item2);
        Builders.setMenuSize(menu);

        item1.setOnAction(e -> setValue(-1));
        item1.setOnAction(e -> delete());

        setOnMousePressed(e -> {
            e.consume();

            shiftX = (int) e.getX();
            shiftY = (int) e.getY();
            menu.hide();
            select();

            if(e.getButton() == MouseButton.SECONDARY){
                menu.show(page, e.getScreenX(), e.getScreenY());
            }
        });
        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE){
                Main.mainScreen.setSelected(null);
                delete();
            }
        });


        setOnMouseDragged(e -> {

            double itemX = getLayoutX() + e.getX() - shiftX;
            double itemY = getLayoutY() + e.getY() - shiftY;

            boolean changePage = false;
            if(this.page.mouseY < -30){
                if(this.page.getPage() > 0){

                    Main.mainScreen.setSelected(null);

                    this.page.removeElement(this, false);
                    this.page = Main.mainScreen.document.pages.get(this.page.getPage() -1);
                    this.page.addElement(this, false);

                    itemY = this.page.getHeight();
                    changePage = true;
                }
            }else if(this.page.mouseY > this.page.getHeight() + 30){
                if(this.page.getPage() < Main.mainScreen.document.pages.size()-1){

                    Main.mainScreen.setSelected(null);

                    this.page.removeElement(this, false);
                    this.page = Main.mainScreen.document.pages.get(this.page.getPage() + 1);
                    this.page.addElement(this, false);

                    itemY = 0;
                    changePage = true;
                }
            }

            checkLocation(itemX, itemY);

            if(changePage){
                layoutXProperty().bind(this.page.widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
                layoutYProperty().bind(this.page.heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));
                Main.lbTextTab.onFileTextSortManager.simulateCall();
            }

        });

        textProperty().addListener((observable, oldValue, newValue) -> {

            if(getLayoutY() < getLayoutBounds().getHeight()){
                checkLocation(getLayoutX(), getLayoutY());
            }
        });
    }

    public void checkLocation(double itemX, double itemY){

        setBoundsType(TextBoundsType.VISUAL);
        double linesHeight = getLayoutBounds().getHeight();
        if(itemY < linesHeight) itemY = linesHeight;
        if(itemY > page.getHeight()) itemY = page.getHeight();
        if(itemX < 0) itemX = 0;
        if(itemX > page.getWidth() - getLayoutBounds().getWidth()) itemX = page.getWidth() - getLayoutBounds().getWidth();
        setBoundsType(TextBoundsType.LOGICAL);

        realX.set((int) (itemX / page.getWidth() * Element.GRID_WIDTH));
        realY.set((int) (itemY / page.getHeight() * Element.GRID_HEIGHT));

    }

    @Override
    public void select() {

        Main.mainScreen.setSelected(this);
        toFront();
        if(getParentPath().isEmpty()) Main.lbNoteTab.treeView.getSelectionModel().select(Main.lbNoteTab.treeView.getRoot());
        else Main.lbNoteTab.treeView.getSelectionModel().select(Main.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) Main.lbNoteTab.treeView.getRoot(), this));

        //requestFocus();
        Edition.setUnsave();
    }

    public NoteTreeItem toNoteTreeItem(){
        return new NoteTreeItem(this);
    }
    @Override
    public void delete(){
        page.removeElement(this, true);
    }

    @Override
    public void writeSimpleData(DataOutputStream writer) throws IOException {
        writer.writeByte(2);
        writeData(writer);
    }
    public void writeData(DataOutputStream writer) throws IOException {
        writer.writeByte(page.getPage());
        writer.writeShort(getRealX());
        writer.writeShort(getRealY());
        writer.writeInt(index);
        writer.writeUTF(parentPath);
        writer.writeDouble(value.getValue());
        writer.writeDouble(total.getValue());
        writer.writeUTF(name.getValue());
    }

    public static NoteElement readDataAndGive(DataInputStream reader) throws IOException {

        byte page = reader.readByte();
        short x = reader.readShort();
        short y = reader.readShort();
        int index = reader.readInt();
        String parentPath = reader.readUTF();
        double value = reader.readDouble();
        double total = reader.readDouble();
        String name = reader.readUTF();

        return new NoteElement(x, y, name, value, total, index, parentPath, Main.mainScreen.document.pages.get(page));

    }
    public static void consumeData(DataInputStream reader) throws IOException {
        byte page = reader.readByte();
        short x = reader.readShort();
        short y = reader.readShort();
        int index = reader.readInt();
        String parentPath = reader.readUTF();
        double value = reader.readDouble();
        double total = reader.readDouble();
        String name = reader.readUTF();
    }
    public static void readDataAndCreate(DataInputStream reader) throws IOException {

        NoteElement element = readDataAndGive(reader);

        if(Main.mainScreen.document.pages.size() > element.page.getPage())
            Main.mainScreen.document.pages.get(element.page.getPage()).addElementSimple(element);

    }


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
    }
    public String getParentPath() {
        return parentPath;
    }
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public int getRealX() {
        return realX.get();
    }
    public IntegerProperty RealXProperty() {
        return realX;
    }
    public void setRealX(int x) {
        this.realX.set(x);
    }

    public int getRealY() {
        return realY.get();
    }
    public IntegerProperty RealYProperty() {
        return realY;
    }
    public void setRealY(int y) {
        this.realY.set(y);
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }
    @Override
    public int getCurrentPageNumber() {
        return page.getPage();
    }

    @Override
    public Element clone() {
        return new NoteElement(getRealX(), getRealY(), name.getValue(), value.getValue(), total.getValue(), index, parentPath, page);
    }
}
