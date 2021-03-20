package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Random;
import java.util.regex.Pattern;

public class GradeTreeItem extends TreeItem {

    private GradeElement core;

    // JavaFX
    private TreeCell<String> cell;
    public HBox pane;

    Region spacer = new Region();
    private Text name = new Text();
    private Text value = new Text();
    private Text slash = new Text("/");
    private Text total = new Text();

    private Button newGrade;

    private TextArea nameField;
    public TextArea gradeField;
    private TextArea totalField;

    private ContextMenu pageContextMenu = null;

    // EVENTS
    private EventHandler<MouseEvent> mouseEnteredEvent;
    private EventHandler<MouseEvent> mouseExitedEvent;
    private ChangeListener<Boolean> selectedListener;

    public GradeTreeItem(GradeElement core){
        this.core = core;

        setupGraphic();
        setupEvents();
    }

    public void setupEvents(){
        
        selectedListener = (observable, oldValue, newValue) -> {
            if(newValue){ // Est sélectionné
                newGrade.setVisible(true);
                //newGrade.setStyle("-fx-background-color: #0078d7");

                nameField.setText(core.getName());
                if(!isRoot() && getParent() != null){
                    if(((GradeTreeItem) getParent()).isExistTwice(core.getName())) core.setName(core.getName() + "(1)");
                }

                gradeField.setText(core.getValue() == -1 ? "" : MainWindow.format.format(core.getValue()));
                totalField.setText(MainWindow.format.format(core.getTotal()));
                pane.getChildren().clear();

                if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
                    if(hasSubGrade()){
                        pane.getChildren().addAll(name, spacer, value, slash, total, newGrade);
                    }else{
                        pane.getChildren().addAll(name, spacer, gradeField, slash, total, newGrade);
                        Platform.runLater(() -> {
                            gradeField.requestFocus();
                        });
                    }
                }else{
                    if(hasSubGrade()){
                        pane.getChildren().addAll(nameField, spacer, value, slash, total, newGrade);
                        Platform.runLater(() -> {
                            nameField.requestFocus();
                        });
                    }else{
                        pane.getChildren().addAll(nameField, spacer, gradeField, slash, totalField, newGrade);
                        Platform.runLater(() -> {
                            if(name.getText().contains(TR.tr("gradeTab.gradeDefaultName"))) nameField.requestFocus();
                            else if(total.getText().equals("0")) totalField.requestFocus();
                            else gradeField.requestFocus();
                        });
                    }
                }

            }else if(oldValue){ // n'est plus selectionné
                newGrade.setVisible(false);
                newGrade.setStyle(null);

                pane.getChildren().clear();
                pane.getChildren().addAll(name, spacer, value, slash, total, newGrade);
            }
        };

        mouseEnteredEvent = event -> {
            if(!cell.isFocused()) newGrade.setVisible(true);
            if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
                if(cell.getTooltip() == null) cell.setTooltip(new Tooltip(TR.tr("gradeTab.lockGradeScale.unableToEditTooltip")));
            }else if(cell.getTooltip() != null){
                cell.setTooltip(null);
            }
        };

        mouseExitedEvent = event -> {
            if(!cell.isFocused()) newGrade.setVisible(false);
        };

        newGrade.setOnAction(event -> {
            setExpanded(true);
            GradeElement element = MainWindow.gradeTab.newGradeElementAuto(this);
            element.select();

            // Update total (Fix the bug when a total is predefined (with no children))
            makeSum(false);
            AutoTipsManager.showByAction("gradecreate");
        });

    }

    public void setupGraphic(){

        pane = new HBox();
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(18);
        pane.setStyle("-fx-padding: -6 -6 -6 -5;"); // top - right - bottom - left

        // TEXTS

        HBox.setMargin(name, new Insets(0, 0, 0, 5));
        name.textProperty().bind(core.nameProperty());

        HBox.setMargin(value, new Insets(0, 0, 0, 5));
        value.textProperty().bind(Bindings.createStringBinding(() -> (core.getValue() == -1 ? "?" : MainWindow.format.format(core.getValue())), core.valueProperty()));

        HBox.setMargin(total, new Insets(0, 5, 0, 0));
        total.textProperty().bind(Bindings.createStringBinding(() -> MainWindow.format.format(core.getTotal()), core.totalProperty()));

        // FIELDS

        nameField = getField(FieldType.NAME, true);
        gradeField = getField(FieldType.GRADE, true);
        totalField = getField(FieldType.TOTAL, true);

        // OTHER

        HBox.setHgrow(spacer, Priority.ALWAYS);

        newGrade = new Button();
        newGrade.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/more.png")+"", 0, 0, ImageUtils.defaultFullDarkColorAdjust));

        PaneUtils.setPosition(newGrade, 0, 0, 30, 30, true);
        newGrade.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.gradeTab.isLockGradeScaleProperty().get() || GradeTreeView.getElementTier(getCore().getParentPath()) >= 4, MainWindow.gradeTab.isLockGradeScaleProperty()));
        newGrade.setVisible(false);
        newGrade.setTooltip(PaneUtils.genToolTip(TR.tr("gradeTab.newGradeButton.tooltip", name.getText())));
        name.textProperty().addListener((observable, oldValue, newValue) -> newGrade.setTooltip(PaneUtils.genToolTip(TR.tr("gradeTab.newGradeButton.tooltip", name.getText()))));

        pane.getChildren().addAll(name, spacer, value, slash, total, newGrade);

    }
    public HBox getEditGraphics(int width, ContextMenu menu){

        Region spacer = new Region();
        Text name = new Text();

        Text value = new Text();
        Text slash = new Text("/");
        Text total = new Text();

        HBox pane = new HBox();
        pane.setAlignment(Pos.CENTER);
        pane.setPrefHeight(18);
        pane.setStyle("-fx-padding: -6 -6 -6 0;"); // top - right - bottom - left

        name.textProperty().bind(core.nameProperty());

        HBox.setMargin(value, new Insets(0, 0, 0, 5));
        value.textProperty().bind(Bindings.createStringBinding(() -> (core.getValue() == -1 ? "?" : MainWindow.format.format(core.getValue())), core.valueProperty()));

        HBox.setMargin(total, new Insets(0, 5, 0, 0));
        total.textProperty().bind(Bindings.createStringBinding(() -> MainWindow.format.format(core.getTotal()), core.totalProperty()));

        // SETUP

        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextArea gradeField = getField(FieldType.GRADE, false);
        gradeField.setText(core.getValue() == -1 ? "" : MainWindow.format.format(core.getValue()));
        if(!isRoot() && getParent() != null){
            if(((GradeTreeItem) getParent()).isExistTwice(core.getName())) core.setName(core.getName() + "(1)");
        }

        if(hasSubGrade()){
            pane.getChildren().addAll(name, spacer, value, slash, total);
        }else{
            pane.getChildren().addAll(name, spacer, gradeField, slash, total);
            Platform.runLater(gradeField::requestFocus);
        }

        pageContextMenu = menu;

        pane.setOnMouseEntered(e -> {
            gradeField.requestFocus();
            MainWindow.gradeTab.treeView.getSelectionModel().select(this);
        });

        pane.setMinWidth(width);

        pane.widthProperty().addListener((e) -> {
            if(pane.getWidth() < width){
                pane.setPrefWidth(width);
            }
        });

        return pane;
    }

    public void updateCell(TreeCell<String> cell){

        if(cell == null) return;
        if(this.cell != null) this.cell.selectedProperty().removeListener(selectedListener);
        this.cell = cell;
        cell.setGraphic(pane);
        cell.setStyle(null);
        cell.setStyle("-fx-padding: 6 6 6 2;");
        cell.setContextMenu(core.menu);
        cell.setOnMouseEntered(mouseEnteredEvent);
        cell.setOnMouseExited(mouseExitedEvent);

        cell.selectedProperty().addListener(selectedListener);

        if(MainWindow.gradeTab.isLockGradeScaleProperty().get()){
            if(cell.getTooltip() == null) cell.setTooltip(new Tooltip(TR.tr("gradeTab.lockGradeScale.unableToEditTooltip")));
        }else if(cell.getTooltip() != null){
            cell.setTooltip(null);
        }

        // DEBUG
        if(Main.DEBUG) cell.setTooltip(new Tooltip(core.getParentPath() + " - n°" + (core.getIndex()+1) + "\nPage n°" + core.getPageNumber()));

    }

    public GradeTreeItem getBeforeItem(){
        if(isRoot()) return null;

        GradeTreeItem parent = (GradeTreeItem) getParent();

        if(core.getIndex() == 0) return parent;

        // Descend le plus possible dans les enfants du parent pour retrouver le dernier
        GradeTreeItem newParent = (GradeTreeItem) parent.getChildren().get(core.getIndex()-1);
        while(newParent.hasSubGrade()){
            newParent = (GradeTreeItem) newParent.getChildren().get(newParent.getChildren().size()-1);
        }
        return newParent;
    }
    public GradeTreeItem getAfterItem(){

        if(hasSubGrade()) return (GradeTreeItem) getChildren().get(0);
        if(isRoot()) return null;

        GradeTreeItem parent = (GradeTreeItem) getParent();
        GradeTreeItem children = this;

        // Remonte dans les parents jusqu'a trouver un parent qui as un élément après celui-ci
        while(children.getCore().getIndex() == parent.getChildren().size()-1){
            children = parent;
            if(parent.isRoot()) return null;
            parent = (GradeTreeItem) parent.getParent();
        }
        return (GradeTreeItem) parent.getChildren().get(children.getCore().getIndex()+1);
    }

    public GradeTreeItem getBeforeChildItem(){
        GradeTreeItem beforeItem = getBeforeItem();
        while(beforeItem != null){
            GradeTreeItem beforeAfterItem = beforeItem.getBeforeItem();
            if(!beforeItem.hasSubGrade()) return beforeItem;
            if(beforeAfterItem == null) return null;
            beforeItem = beforeAfterItem;
        }
        return null;
    }
    public GradeTreeItem getAfterChildItem(){
        GradeTreeItem afterItem = getAfterItem();
        while(afterItem != null){
            GradeTreeItem afterAfterItem = afterItem.getAfterItem();
            if(!afterItem.hasSubGrade()) return afterItem;
            if(afterAfterItem == null) return null;
            afterItem = afterAfterItem;
        }
        return null;
    }
    public void makeSum(boolean updateLocation){
       if(!updateLocation) makeSum(-1, 0);
       else throw new RuntimeException("use makeSum(int previousPage, int previousRealY) to update Location");
    }
    public void makeSum(int previousPage, int previousRealY){
        boolean hasValue = false;
        double value = 0;
        double total = 0;

        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);

            // Don't count the "Bonus" children in the Total
            if(!children.getCore().isBonus()){
                total += children.getCore().getTotal(); // count total
            }

            if(children.getCore().getValue() >= 0){
                hasValue = true;
                if(!MainWindow.gradeTab.sumByDecrement.isSelected()){
                    value += children.getCore().getValue();
                }else{
                    if(children.hasSubGrade()){
                        value += children.getCore().getTotal() - children.getCore().getValue();
                    }else{
                        value += children.getCore().getValue();
                    }
                }

            }
        }
        if(MainWindow.gradeTab.sumByDecrement.isSelected()){
            value = total - value;
        }

        if(hasValue){
            if(!core.isFilled() && previousPage != -1){
                if(previousPage != core.getPageNumber()) core.switchPage(previousPage);
                core.nextRealYToUse = previousRealY - core.getRealHeight();
            }
            core.setValue(value);
        }else core.setValue(-1);

        core.setTotal(total);

        if(getParent() != null){
            ((GradeTreeItem) getParent()).makeSum(core.getPageNumber(), core.getRealY());
        }
    }

    public void resetChildrenValues(){
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setValue(-1);
            children.resetChildrenValues();
        }
    }
    public void setChildrenValuesTo0(){
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setValue(0);
            children.setChildrenValuesTo0();
        }
    }
    public void setChildrenValuesToMax(){
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setValue(children.getCore().getTotal());
            children.setChildrenValuesToMax();
        }
    }
    public void setChildrenAlwaysVisibleToFalse() {
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setAlwaysVisible(false);
        }
    }

    public boolean hasSubGrade(){
        return getChildren().size() != 0;
    }
    public boolean isRoot(){
        return StringUtils.cleanArray(core.getParentPath().split(Pattern.quote("\\"))).length == 0;
        //return Main.gradeTab.treeView.getRoot().equals(this);
    }

    public GradeElement getCore() {
        return core;
    }
    public void setCore(GradeElement core) {
        this.core = core;
    }
    public TreeCell<String> getCell() {
        return cell;
    }

    public void reIndexChildren() {

        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setIndex(i);
        }

    }
    public void resetParentPathChildren() {

        String path = GradeTreeView.getElementPath(this);

        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            children.getCore().setParentPath(path);
            if(children.hasSubGrade()) children.resetParentPathChildren();
        }

    }

    public void deleteChildren() {
        while(hasSubGrade()){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(0);
            if(children.hasSubGrade()) children.deleteChildren();
            children.getCore().delete();
        }

    }

    public boolean isExistTwice(String name){
        if(isRoot()) return false;
        int k = 0;
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            if(children.getCore().getName().equals(name)) k++;
        }

        return k >= 2;
    }



    public enum FieldType{
        NAME,
        GRADE,
        TOTAL
    }

    public TextArea getField(FieldType type, boolean contextMenu){

        TextArea field = new TextArea("😉😉😉");

        field.setStyle("-fx-font-size: 13;");
        field.setMinHeight(29);
        field.setMaxHeight(29);
        field.setMinWidth(29);

        if(type == FieldType.GRADE) HBox.setMargin(field, new Insets(0, 0, 0, 5));
        if(type == FieldType.TOTAL) HBox.setMargin(field, new Insets(0, 5, 0, 0));

        if(contextMenu) field.setContextMenu(core.menu);
        else field.setContextMenu(null);

        field.focusedProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if(newValue){
                    if(field.getCaretPosition() == field.getText().length() || field.getCaretPosition() == 0 || type != FieldType.NAME){
                        field.positionCaret(field.getText().length());
                        field.selectAll();
                    }
                }else field.deselect();
            });
        });
        ScratchText meter = new ScratchText();
        meter.setFont(new Font(field.getFont().getFamily(), 13));

        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.contains("\n")){ // Enter : Switch to the next grade
                if(pageContextMenu != null) pageContextMenu.hide();

                GradeTreeItem afterItem = getAfterChildItem();
                MainWindow.gradeTab.treeView.getSelectionModel().select(afterItem);
                if(afterItem != null) Platform.runLater(() -> {
                    switch (type){
                        case NAME -> afterItem.nameField.requestFocus();
                        case GRADE -> afterItem.gradeField.requestFocus();
                        case TOTAL -> afterItem.totalField.requestFocus();
                    }
                });
                field.setText(oldValue);
                return;
            }

            if(newValue.contains("\u0009")){ // TAB
                if(core.getTotal() == 0){
                    switch (type){
                        case NAME, GRADE -> totalField.requestFocus();
                        case TOTAL -> gradeField.requestFocus();
                    }
                }else{
                    switch (type){
                        case NAME, TOTAL -> gradeField.requestFocus();
                        case GRADE -> totalField.requestFocus();
                    }
                }
                field.setText(oldValue);
                return;
            }

            String newText;
            if(type == FieldType.NAME){
                newText = newValue.replaceAll("[^ -\\[\\]-~À-ÿ]", "");
                if(newText.length() >= 20) newText = newText.substring(0, 20);
            }else{
                newText = newValue.replaceAll("[^0123456789.,]", "");
                if(newText.length() >= 5) newText = newText.substring(0, 5);
            }

            field.setText(newText); meter.setText(newText);
            field.setMaxWidth(meter.getLayoutBounds().getWidth()+20);

            if(type == FieldType.GRADE && field != gradeField){
                gradeField.setText(newText);
            }

            switch (type){
                case NAME:
                    core.setName(newText);
                    if(new Random().nextInt(10) == 0) AutoTipsManager.showByAction("graderename");
                    break;
                case GRADE:
                    // dont accept a value higher than the total
                    try{
                        double value = Double.parseDouble(newText.replaceAll(Pattern.quote(","), "."));
                        if(value > core.getTotal() && !hasSubGrade()){
                            field.setText(MainWindow.format.format(core.getTotal()));
                            gradeField.setText(MainWindow.format.format(core.getTotal()));
                        }else core.setValue(value);
                    }catch(NumberFormatException e){ core.setValue(-1); }
                    break;
                case TOTAL:
                    try{
                        core.setTotal(Double.parseDouble(newText.replaceAll(Pattern.quote(","), ".")));
                    }catch(NumberFormatException e){ core.setTotal(0); }
                    break;
            }

        });

        return field;
    }

}
