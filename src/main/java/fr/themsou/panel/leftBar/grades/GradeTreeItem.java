package fr.themsou.panel.leftBar.grades;

import fr.themsou.document.editions.elements.GradeElement;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.utils.components.ScratchText;
import fr.themsou.windows.MainWindow;
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

import java.math.BigDecimal;
import java.util.ArrayList;
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

    private TextArea nameField = new TextArea("☺");
    public TextArea gradeField = new TextArea("☺");
    private TextArea totalField = new TextArea("☺");

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

                gradeField.setText(core.getValue() == -1 ? "" : Main.format.format(core.getValue()));
                totalField.setText(Main.format.format(core.getTotal()));
                pane.getChildren().clear();

                if(MainWindow.lbGradeTab.isLockGradeScaleProperty().get()){
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
                            if(name.getText().contains(TR.tr("Nouvelle note"))) nameField.requestFocus();
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
            if(MainWindow.lbGradeTab.isLockGradeScaleProperty().get()){
                if(cell.getTooltip() == null) cell.setTooltip(new Tooltip(TR.tr("Clic sur le cadenas pour éditer le barème")));
            }else if(cell.getTooltip() != null){
                cell.setTooltip(null);
            }
        };

        mouseExitedEvent = event -> {
            if(!cell.isFocused()) newGrade.setVisible(false);
        };

        newGrade.setOnAction(event -> {
            setExpanded(true);
            GradeElement element = MainWindow.lbGradeTab.newGradeElementAuto(this);
            element.select();

            // Update total (Fix the bug when a total is predefined (with no children))
            makeSum();
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
        value.textProperty().bind(Bindings.createStringBinding(() -> (core.getValue() == -1 ? "?" : Main.format.format(core.getValue())), core.valueProperty()));

        HBox.setMargin(total, new Insets(0, 5, 0, 0));
        total.textProperty().bind(Bindings.createStringBinding(() -> Main.format.format(core.getTotal()), core.totalProperty()));

        // FIELDS

        nameField.setStyle("-fx-font-size: 13;");
        nameField.setMinHeight(29);
        nameField.setMaxHeight(29);
        nameField.setMinWidth(29);

        gradeField.setStyle("-fx-font-size: 13;");
        gradeField.setMinHeight(29);
        gradeField.setMaxHeight(29);
        gradeField.setMinWidth(29);
        HBox.setMargin(gradeField, new Insets(0, 0, 0, 5));

        totalField.setStyle("-fx-font-size: 13;");
        totalField.setMinHeight(29);
        totalField.setMaxHeight(29);
        totalField.setMinWidth(29);
        HBox.setMargin(totalField, new Insets(0, 5, 0, 0));

        ScratchText meter = new ScratchText();
        meter.setFont(new Font(nameField.getFont().getFamily(), 13));

        nameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if(newValue){
                    if(nameField.getCaretPosition() == nameField.getText().length() || nameField.getCaretPosition() == 0) {
                        nameField.positionCaret(nameField.getText().length());
                        nameField.selectAll();
                    }
                }else{
                    nameField.deselect();
                }
            });
        });
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.contains("\n")){
                GradeTreeItem afterItem = getAfterItem();
                MainWindow.lbGradeTab.treeView.getSelectionModel().select(afterItem);
                if(afterItem != null) Platform.runLater(() -> afterItem.nameField.requestFocus());
            }

            String newText = newValue.replaceAll("[^ -\\[\\]-~À-ÿ]", "");
            if(newText.length() >= 20) newText = newText.substring(0, 20);

            nameField.setText(newText);
            meter.setText(newText);
            nameField.setMaxWidth(meter.getLayoutBounds().getWidth()+20);

            core.setName(newText);
        });
        gradeField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if(newValue){
                    gradeField.positionCaret(gradeField.getText().length());
                    gradeField.selectAll();
                }else{
                    gradeField.deselect();
                }
            });
        });
        gradeField.textProperty().addListener((observable, oldTextValue, newValue) -> {
            if(newValue.contains("/")){
                totalField.requestFocus();
                totalField.positionCaret(totalField.getText().length());
            }
            if(newValue.contains("\n")){ // Enter : Switch to the next grade
                if(pageContextMenu != null){
                    pageContextMenu.hide();
                    pageContextMenu.getItems().clear();
                }
                GradeTreeItem afterItem = getAfterChildItem();
                MainWindow.lbGradeTab.treeView.getSelectionModel().select(afterItem);
                if(afterItem != null) Platform.runLater(() -> afterItem.gradeField.requestFocus());
            }
            String newText = newValue.replaceAll("[^0123456789.,]", "");
            if(newText.length() >= 5) newText = newText.substring(0, 5);

            gradeField.setText(newText);
            meter.setText(newText);
            gradeField.setMaxWidth(meter.getLayoutBounds().getWidth()+20);

            // dont accept a value higher than the total
            try{
                double value = Double.parseDouble(newText.replaceAll(Pattern.quote(","), "."));
                if(value > core.getTotal() && !hasSubGrade()){
                    gradeField.setText(Main.format.format(core.getTotal()));
                }else core.setValue(value);
            }catch(NumberFormatException e){
                core.setValue(-1);
            }

        });
        totalField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if(newValue){
                    totalField.positionCaret(totalField.getText().length());
                    totalField.selectAll();
                }else{
                    totalField.deselect();
                }
            });
        });
        totalField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.contains("\n")){ // Enter : Switch to the next grade
                GradeTreeItem afterItem = getAfterChildItem();
                MainWindow.lbGradeTab.treeView.getSelectionModel().select(afterItem);
                if(afterItem != null) Platform.runLater(() -> afterItem.totalField.requestFocus());
            }

            String newText = newValue.replaceAll("[^0123456789.,]", "");
            if(newText.length() >= 5) newText = newText.substring(0, 5);

            totalField.setText(newText);
            meter.setText(newText);
            totalField.setMaxWidth(meter.getLayoutBounds().getWidth()+20);

            try{
                core.setTotal(Double.parseDouble(newText.replaceAll(Pattern.quote(","), ".")));
            }catch(NumberFormatException e){
                core.setTotal(0);
            }
        });

        // OTHER

        HBox.setHgrow(spacer, Priority.ALWAYS);

        newGrade = new Button();
        newGrade.setGraphic(Builders.buildImage(getClass().getResource("/img/GradesTab/more.png")+"", 0, 0));
        Builders.setPosition(newGrade, 0, 0, 30, 30, true);
        newGrade.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.lbGradeTab.isLockGradeScaleProperty().get() || GradeTreeView.getElementTier(getCore().getParentPath()) >= 4, MainWindow.lbGradeTab.isLockGradeScaleProperty()));
        newGrade.setVisible(false);
        newGrade.setTooltip(Builders.genToolTip(TR.tr("Créer une nouvelle sous-note de") + " " + name.getText()));
        name.textProperty().addListener((observable, oldValue, newValue) -> newGrade.setTooltip(Builders.genToolTip(TR.tr("Créer une nouvelle sous-note de") + " " + name.getText())));

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
        value.textProperty().bind(Bindings.createStringBinding(() -> (core.getValue() == -1 ? "?" : Main.format.format(core.getValue())), core.valueProperty()));

        HBox.setMargin(total, new Insets(0, 5, 0, 0));
        total.textProperty().bind(Bindings.createStringBinding(() -> Main.format.format(core.getTotal()), core.totalProperty()));

        // SETUP

        HBox.setHgrow(spacer, Priority.ALWAYS);

        gradeField.setText(core.getValue() == -1 ? "" : Main.format.format(core.getValue()));
        if(!isRoot() && getParent() != null){
            if(((GradeTreeItem) getParent()).isExistTwice(core.getName())) core.setName(core.getName() + "(1)");
        }

        if(hasSubGrade()){
            pane.getChildren().addAll(name, spacer, value, slash, total);
        }else{
            pane.getChildren().addAll(name, spacer, gradeField, slash, total);
            Platform.runLater(() -> {
                gradeField.requestFocus();
            });
        }

        pageContextMenu = menu;

        pane.setOnMouseEntered(e -> {
            gradeField.requestFocus();
        });
        pane.setPrefWidth(width);
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

        if(MainWindow.lbGradeTab.isLockGradeScaleProperty().get()){
            if(cell.getTooltip() == null) cell.setTooltip(new Tooltip(TR.tr("Clic sur le cadenas pour éditer le barème")));
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

    public void makeSum(){
        boolean hasValue = false;
        double value = 0;
        double total = 0;

        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);

            // Don't count the "Bonus" children in the Total
            if(!children.getCore().getName().equalsIgnoreCase(TR.tr("Bonus"))){
                total += children.getCore().getTotal();
            }

            if(children.getCore().getValue() >= 0){
                hasValue = true;
                value += children.getCore().getValue();
            }
        }

        if(hasValue) core.setValue(value);
        else core.setValue(-1);
        core.setTotal(total);

        if(getParent() != null){
            ((GradeTreeItem) getParent()).makeSum();
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

    public boolean hasSubGrade(){
        return getChildren().size() != 0;
    }
    public boolean isRoot(){
        return Builders.cleanArray(core.getParentPath().split(Pattern.quote("\\"))).length == 0;
        //return Main.lbGradeTab.treeView.getRoot().equals(this);
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
        int k = 0;
        for(int i = 0; i < getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) getChildren().get(i);
            if(children.getCore().getName().equals(name)) k++;
        }

        return k >= 2;
    }
}
