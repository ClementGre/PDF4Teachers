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
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.apache.pdfbox.text.TextPosition;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
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
    private PageRenderer page;

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

        updateFont();

        if(page == null) return;
        this.page = page;
        this.maxY = (int) page.getHeight();

        setCursor(Cursor.MOVE);
        setVisible(getValue() != -1);
        layoutXProperty().bind(page.widthProperty().multiply(this.realX.divide(Element.GRID_WIDTH)));
        layoutYProperty().bind(page.heightProperty().multiply(this.realY.divide(Element.GRID_HEIGHT)));

        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Réinitialiser"), -1, false);
        item1.setToolTip(TR.tr("Réinitialise la note entrée et toutes ses sous-notes."));
        item1.setToolTip(TR.tr("suppr"));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Supprimer du barème"), -1, false);
        item2.setToolTip(TR.tr("Supprime cet élément du barème et de l'édition."));
        item2.disableProperty().bind(Main.lbNoteTab.isLockRatingScaleProperty());
        menu.getItems().addAll(item1, item2);
        Builders.setMenuSize(menu);

        item1.setOnAction(e -> {
            NoteTreeItem treeItemElement;
            if(((NoteTreeItem) Main.lbNoteTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (NoteTreeItem) Main.lbNoteTab.treeView.getRoot();
            else treeItemElement = Main.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) Main.lbNoteTab.treeView.getRoot(), this);
            treeItemElement.noteField.setText("");
        });
        item2.setOnAction(e -> {
            if(((NoteTreeItem) Main.lbNoteTab.treeView.getRoot()).getCore().equals(this)){
                // Regenerate Root if this is Root
                delete();
                Main.lbNoteTab.treeView.generateRoot();
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
                setName(TR.tr("Nouvelle note")); return;
            }

            NoteTreeItem treeItemElement;
            if(((NoteTreeItem) Main.lbNoteTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (NoteTreeItem) Main.lbNoteTab.treeView.getRoot();
            else{
                treeItemElement = Main.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) Main.lbNoteTab.treeView.getRoot(), this);
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
                calculateMinAndMaxY();
                if(oldValue.intValue() == -1) setRealY((int) (page.mouseY * Element.GRID_HEIGHT / page.getHeight()));

                setVisible(true);
                setText((LBNoteTab.getTierShowName(NoteTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + Main.format.format(newValue) + "/" + Main.format.format(getTotal()));
            }

            if(((NoteTreeItem) Main.lbNoteTab.treeView.getRoot()).getCore().equals(this)){// this is Root
                if(newValue.intValue() == -1) ((NoteTreeItem) Main.lbNoteTab.treeView.getRoot()).resetChildrenValues();

            }else{
                NoteTreeItem treeItemElement = Main.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) Main.lbNoteTab.treeView.getRoot(), this);
                if(treeItemElement.hasSubNote() && newValue.intValue() == -1) treeItemElement.resetChildrenValues();
                ((NoteTreeItem) treeItemElement.getParent()).makeSum();
            }
        });
        totalProperty().addListener((observable, oldValue, newValue) -> {
            Edition.setUnsave();
            setText((LBNoteTab.getTierShowName(NoteTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + Main.format.format(getValue()) + "/" + Main.format.format(getTotal()));

            if(((NoteTreeItem) Main.lbNoteTab.treeView.getRoot()).getCore().equals(this)) return; // This is Root
            ((NoteTreeItem) Main.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) Main.lbNoteTab.treeView.getRoot(), this).getParent()).makeSum();
        });

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

        setOnMousePressed(e -> {
            e.consume();

            shiftX = (int) e.getX();
            shiftY = (int) e.getY();

            calculateMinAndMaxY();

            menu.hide();
            select();

            if(e.getButton() == MouseButton.SECONDARY){
                menu.show(page, e.getScreenX(), e.getScreenY());
            }
        });
        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE){
                NoteTreeItem treeItemElement;
                if(((NoteTreeItem) Main.lbNoteTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (NoteTreeItem) Main.lbNoteTab.treeView.getRoot();
                else treeItemElement = Main.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) Main.lbNoteTab.treeView.getRoot(), this);
                treeItemElement.noteField.setText("");
            }
        });

        setOnMouseDragged(e -> {

            Edition.setUnsave();
            double itemX = getLayoutX() + e.getX() - shiftX;
            double itemY = getLayoutY() + e.getY() - shiftY;

            boolean changePage = false;
            if(this.page.mouseY < -30  && this.page.getPage() > minYPage){ // Monter d'une page
                if(this.page.getPage() > 0){

                    Main.mainScreen.setSelected(null);

                    this.page.switchElementPage(this, Main.mainScreen.document.pages.get(this.page.getPage() -1));

                    itemY = this.page.getHeight();
                    changePage = true;
                }
            }else if(this.page.mouseY > this.page.getHeight() + 30 && this.page.getPage() < maxYPage){ // Descendre d'une page
                if(this.page.getPage() < Main.mainScreen.document.pages.size()-1){

                    Main.mainScreen.setSelected(null);

                    this.page.switchElementPage(this, Main.mainScreen.document.pages.get(this.page.getPage() +1));

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
    }

    public void calculateMinAndMaxY(){

        NoteTreeItem treeItemElement;
        if(((NoteTreeItem) Main.lbNoteTab.treeView.getRoot()).getCore().equals(this)) treeItemElement = (NoteTreeItem) Main.lbNoteTab.treeView.getRoot();
        else treeItemElement = Main.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) Main.lbNoteTab.treeView.getRoot(), this);

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
            minYPage = beforeItem.getCore().getCurrentPageNumber();
            minY = (int) beforeItem.getCore().getLayoutY();
            minY = Math.max(minY, 0);
        }
        if(afterItem == null){
            maxYPage = 999999;
            maxY = (int) page.getHeight();
        }else{
            maxYPage = afterItem.getCore().getCurrentPageNumber();
            maxY = (int) (afterItem.getCore().getLayoutY() - afterItem.getCore().getLayoutBounds().getHeight());
            maxY = (int) Math.min(maxY, page.getHeight());
        }

        //System.out.println((beforeItem == null ? "null" : beforeItem.getCore().getName()) + " < " + getName() + " < " + (afterItem == null ? "null" : afterItem.getCore().getName()));
        //System.out.println(minYPage + ":" + minY + " < " + getName() + " < " + maxYPage + ":" + maxY);
    }

    public void checkLocation(double itemX, double itemY){

        setBoundsType(TextBoundsType.VISUAL);

        double height = getLayoutBounds().getHeight();

        int minY = page.getPage() > minYPage ? 0 : this.minY;
        int maxY = page.getPage() < maxYPage ? (int) page.getHeight() : this.maxY;

        //System.out.println("minY = " + minY + "  |  maxY = " + maxY);

        if(itemY > maxY) itemY = maxY;
        if(itemY < height + minY) itemY = height + minY;

        if(itemX < 0) itemX = 0;
        if(itemX > page.getWidth() - getLayoutBounds().getWidth()) itemX = page.getWidth() - getLayoutBounds().getWidth();

        setBoundsType(TextBoundsType.LOGICAL);

        realX.set((int) (itemX / page.getWidth() * Element.GRID_WIDTH));
        realY.set((int) (itemY / page.getHeight() * Element.GRID_HEIGHT));

    }

    public void updateFont(){
        setFont(LBNoteTab.getTierFont(NoteTreeView.getElementTier(parentPath)));
        setFill(LBNoteTab.getTierColor(NoteTreeView.getElementTier(parentPath)));
        setText((LBNoteTab.getTierShowName(NoteTreeView.getElementTier(parentPath)) ? getName() + " : " : "") + (getValue() == -1 ? "" : Main.format.format(getValue())) + "/" + Main.format.format(getTotal()));
    }

    @Override
    public void select() {

        Main.mainScreen.setSelected(this);
        toFront();

        // Sélectionne l'élément associé dans l'arbre
        NoteTreeItem noteElement;
        if(getParentPath().isEmpty()) noteElement = (NoteTreeItem) Main.lbNoteTab.treeView.getRoot();
        else noteElement = Main.lbNoteTab.treeView.getNoteTreeItem((NoteTreeItem) Main.lbNoteTab.treeView.getRoot(), this);
        Main.lbNoteTab.treeView.getSelectionModel().select(noteElement);

        requestFocus();

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
        writer.writeByte(page == null ? pageNumber : page.getPage());
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

        return new NoteElement(x, y, name, value, total, index, parentPath, page, hasPage ? Main.mainScreen.document.pages.get(page) : null);

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

        if(Builders.cleanArray(parentPath.split(Pattern.quote("\\"))).length == 0){
            return new double[]{value, total};
        }else{
            return new double[]{value};
        }

    }
    public static void readDataAndCreate(DataInputStream reader) throws IOException {

        NoteElement element = readDataAndGive(reader, true);

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
        Edition.setUnsave();
    }
    public String getParentPath() {
        return parentPath;
    }
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
        Edition.setUnsave();
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

    public PageRenderer getPage() {
        return page;
    }
    public void setPage(PageRenderer page) {
        this.page = page;
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
        return new NoteElement(getRealX(), getRealY(), name.getValue(), value.getValue(), total.getValue(), index, parentPath, pageNumber, page);
    }

    public NoteRating toNoteRating() {
        return new NoteRating(total.get(), name.get(), index, parentPath);
    }
}
