/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.FontComboBox;
import fr.clementgre.pdf4teachers.components.ShortcutsTextArea;
import fr.clementgre.pdf4teachers.components.ShortcutsTextField;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.datasaving.settings.Settings;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToDoubleConverter;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Random;
import java.util.regex.Pattern;

public class TextTab extends SideTab {

    public VBox pane = new VBox();
    public VBox optionPane = new VBox();

    private final HBox combosBox = new HBox();
    public FontComboBox fontCombo = new FontComboBox(true);
    public final Spinner<Double> sizeSpinner = new Spinner<>(2d, 999d, 14d, 2d);

    private final HBox colorAndParamsBox = new HBox();
    public final SyncColorPicker colorPicker = new SyncColorPicker();
    private final ToggleButton boldBtn = new ToggleButton("");
    private final ToggleButton itBtn = new ToggleButton("");

    public TextArea txtArea = new ShortcutsTextArea();

    private final HBox btnBox = new HBox();
    private final Button deleteBtn = new Button(TR.tr("actions.delete"));
    public Button newBtn = new Button(TR.tr("actions.new"));

    public static final String TEXT_TREE_ITEM_DRAG_KEY = "TextTreeItemDrag";
    public static TextTreeItem draggingItem;
    public static TextElement draggingElement;

    // FIELDS

    public boolean isNew;

    // TREEVIEW
    public TextTreeView treeView;

    // OTHER

    private boolean txtAreaScrollBarListenerIsSetup;

    public TextTab(){
        super("text", SVGPathIcons.TEXT_LETTER, 26, 460/500d);

        draggingItem = null;
        draggingElement = null;
        setContent(pane);
        setup();

        pane.getChildren().addAll(optionPane, treeView);
    }

    public void setup(){
        treeView = new TextTreeView(pane);
        optionPane.setMinWidth(200);

        PaneUtils.setHBoxPosition(fontCombo, -1, 30, 2.5);
        fontCombo.setStyle("-fx-font-size: 13; -fx-border: null; -fx-padding: 0 4;");
        fontCombo.setMaxHeight(25);
        fontCombo.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        fontCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(isNew) MainWindow.userData.textLastFontName = newValue;
        });

        PaneUtils.setHBoxPosition(sizeSpinner, 95, 30, 2.5);
        ShortcutsTextField.registerNewInput(sizeSpinner);
        sizeSpinner.setStyle("-fx-font-size: 13");
        sizeSpinner.setEditable(true);
        sizeSpinner.getValueFactory().setConverter(new StringToDoubleConverter(14));
        sizeSpinner.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        sizeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(isNew) MainWindow.userData.textLastFontSize = newValue;
        });

        PaneUtils.setHBoxPosition(colorPicker, -1, 30, 2.5);
        colorPicker.setStyle("-fx-font-size: 13");
        colorPicker.setValue(Color.BLACK);
        colorPicker.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(isNew) MainWindow.userData.textLastFontColor = newValue.toString();
        });

        PaneUtils.setHBoxPosition(boldBtn, 45, 29, 2.5);
        boldBtn.setCursor(Cursor.HAND);
        boldBtn.setGraphic(ImageUtils.buildImage(String.valueOf(getClass().getResource("/img/TextTab/bold.png")), 0, 0, ImageUtils.defaultFullDarkColorAdjust));
        boldBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        boldBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(isNew) MainWindow.userData.textLastFontBold = newValue;
        });

        PaneUtils.setHBoxPosition(itBtn, 45, 29, 2.5);
        itBtn.setCursor(Cursor.HAND);
        itBtn.setGraphic(ImageUtils.buildImage(String.valueOf(getClass().getResource("/img/TextTab/italic.png")), 0, 0, ImageUtils.defaultFullDarkColorAdjust));
        itBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        itBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(isNew) MainWindow.userData.textLastFontItalic = newValue;
        });

        PaneUtils.setHBoxPosition(txtArea, -1, 30, 0);
        if(Main.settings.textSmall.getValue()) txtArea.setStyle("-fx-font-size: 12");
        else txtArea.setStyle("-fx-font-size: 13");
        txtArea.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.getSelected() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        updateTextAreaPromptText();
        Main.settings.defaultTextMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateTextAreaPromptText();
            if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                String text = TextElement.invertBySettings(txtArea.getText(), oldValue.intValue());
                txtArea.setText(TextElement.invertBySettings(text, newValue.intValue()));
            }
        });

        txtArea.setId("no-vertical-scroll-bar");
        txtArea.setFocusTraversable(false);

        PaneUtils.setHBoxPosition(deleteBtn, -1, 30, 2.5);
        deleteBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));

        PaneUtils.setHBoxPosition(newBtn, -1, 30, 2.5);
        newBtn.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));

        combosBox.getChildren().addAll(fontCombo, sizeSpinner);
        colorAndParamsBox.getChildren().addAll(colorPicker, boldBtn, itBtn);
        btnBox.getChildren().addAll(deleteBtn, newBtn);

        VBox.setMargin(combosBox, new Insets(2.5, 2.5, 0, 2.5));
        VBox.setMargin(colorAndParamsBox, new Insets(0, 2.5, 0, 2.5));
        VBox.setMargin(txtArea, new Insets(2.5, 5, 2.5, 5));
        VBox.setMargin(btnBox, new Insets(0, 2.5, 7.5, 2.5));
        optionPane.getChildren().addAll(combosBox, colorAndParamsBox, txtArea, btnBox);


        MainWindow.mainScreen.selectedProperty().addListener((ObservableValue<? extends Element> observable, Element oldElement, Element newElement) -> {
            isNew = false;
            if(oldElement instanceof TextElement current){
                current.textProperty().unbind();
                current.fontProperty().unbind();

                if(((TextElement) oldElement).hasEmptyText()){
                    oldElement.delete(true, UType.ELEMENT_NO_COUNT_BEFORE);
                }

                if(!(newElement instanceof TextElement)) txtArea.clear();
            }
            if(newElement instanceof TextElement current){

                txtArea.setText(TextElement.invertMathIfNeeded(current.getText()));
                boldBtn.setSelected(FontUtils.getFontWeight(current.getFont()) == FontWeight.BOLD);
                itBtn.setSelected(FontUtils.getFontPosture(current.getFont()) == FontPosture.ITALIC);
                colorPicker.setValue(current.getColor());
                fontCombo.getSelectionModel().select(current.getFont().getFamily());
                sizeSpinner.getValueFactory().setValue(current.getFont().getSize());

                current.fontProperty().bind(Bindings.createObjectBinding(() -> {
                    Edition.setUnsave("TextElement FontChanged");
                    return getFont();
                }, fontCombo.getSelectionModel().selectedItemProperty(), sizeSpinner.valueProperty(), itBtn.selectedProperty(), boldBtn.selectedProperty()));
            }
        });

        txtArea.setContextMenu(null);

        txtArea.disableProperty().addListener((observable, oldValue, newValue) -> treeView.updateAutoComplete());
        MainWindow.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> treeView.updateAutoComplete());

        txtArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

            if(newValue.contains("\t")){ // TAB
                txtArea.setText(newValue.replaceAll(Pattern.quote("\t"), ""));
                return;
            }

            // Default LaTeX
            newValue = TextElement.invertMathIfNeeded(newValue);

            if(MainWindow.mainScreen.getSelected() instanceof TextElement element){
                treeView.updateAutoComplete();

                updateHeightAndYLocations(getHorizontalSB(txtArea).isVisible());
                if(!txtAreaScrollBarListenerIsSetup){
                    getHorizontalSB(txtArea).visibleProperty().addListener((ObservableValue<? extends Boolean> observableTxt, Boolean oldTxtValue, Boolean newTxtValue) -> updateHeightAndYLocations(newTxtValue));
                    txtAreaScrollBarListenerIsSetup = true;
                }
                element.setText(newValue);
                if(new Random().nextInt(10) == 0) AutoTipsManager.showByAction("textedit");
            }
        });
        txtArea.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE || (e.getCode() == KeyCode.BACK_SPACE && e.isShortcutDown())){
                e.consume();
                if(txtArea.getCaretPosition() == txtArea.getText().length()){
                    Element element = MainWindow.mainScreen.getSelected();
                    if(element != null){
                        MainWindow.mainScreen.setSelected(null);
                        element.delete(true, UType.ELEMENT);
                    }
                }
            }else if(e.getCode() == KeyCode.TAB){
                e.consume();
                MainWindow.paintTab.select();

            }else if(e.getCode() == KeyCode.DOWN && txtArea.getText().split("\n").length == 1){
                e.consume();
                if(TextTreeItem.lastKeyPressTime > System.currentTimeMillis() - 100) return;
                TextTreeItem.lastKeyPressTime = System.currentTimeMillis();
                pane.requestFocus();
                if(!treeView.selectNextInSelection()){
                    txtArea.requestFocus();
                }
            }else if(e.getCode() == KeyCode.UP && txtArea.getText().split("\n").length == 1){
                e.consume();
                if(TextTreeItem.lastKeyPressTime > System.currentTimeMillis() - 100) return;
                TextTreeItem.lastKeyPressTime = System.currentTimeMillis();
                pane.requestFocus();
                if(!treeView.selectPreviousInSelection()){
                    txtArea.requestFocus();
                }
            }
        });
        colorPicker.valueProperty().addListener(o -> {
            if(MainWindow.mainScreen.getSelected() != null){
                if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                    ((TextElement) MainWindow.mainScreen.getSelected()).setColor(colorPicker.getValue());
                    Edition.setUnsave("TextElement color changed");
                }
            }
        });
        newBtn.setOnAction(e -> newTextElement(true));
        deleteBtn.setOnAction(e -> {
            MainWindow.mainScreen.getSelected().delete(true, UType.ELEMENT);
            MainWindow.mainScreen.setSelected(null);
        });
    }

    public TextElement newTextElement(boolean addToLasts){
        PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();

        MainWindow.mainScreen.setSelected(null);

        fontCombo.getSelectionModel().select(MainWindow.userData.textLastFontName.isEmpty() ? "Open Sans" : MainWindow.userData.textLastFontName);
        sizeSpinner.getValueFactory().setValue(MainWindow.userData.textLastFontSize);
        colorPicker.setValue(Color.valueOf(MainWindow.userData.textLastFontColor.isEmpty() ? "#000000" : MainWindow.userData.textLastFontColor));
        boldBtn.setSelected(MainWindow.userData.textLastFontBold);
        itBtn.setSelected(MainWindow.userData.textLastFontItalic);

        TextElement current = new TextElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(),
                true, "", colorPicker.getValue(), getFont(), 0);
        
        page.addElement(current, true, UType.ELEMENT);
        current.centerOnCoordinatesY();
        MainWindow.mainScreen.setSelected(current);
        isNew = true;

        txtArea.setText("");
        if(addToLasts) TextTreeView.addSavedElement(current.toNoDisplayTextElement(TextTreeSection.LAST_TYPE, true));
        txtArea.requestFocus();

        AutoTipsManager.showByAction("newtextelement");

        return current;
    }

    private void updateTextAreaPromptText(){
        if(Main.settings.defaultTextMode.getValue() == Settings.TEXT_MODE_LATEX){
            txtArea.setPromptText(TR.tr("textTab.textAreaPromptText.latexInverted"));
        }else if(Main.settings.defaultTextMode.getValue() == Settings.TEXT_MODE_STARMATH){
            txtArea.setPromptText(TR.tr("textTab.textAreaPromptText.starMathInverted"));
        }else{
            txtArea.setPromptText(TR.tr("textTab.textAreaPromptText"));
        }

    }

    public void updateHeightAndYLocations(boolean sbIsVisible){

        int lineNumber = txtArea.getParagraphs().size();
        int height = lineNumber >= 3 ? 70 : lineNumber * 20 + 10;

        if(sbIsVisible) height += 16;

        if(txtArea.getHeight() != height){
            txtArea.minHeightProperty().bind(new SimpleDoubleProperty(height));
            deleteBtn.setLayoutY(80 + height);
            newBtn.setLayoutY(80 + height);
        }

    }

    public void selectItem(){
        PlatformUtils.runLaterOnUIThread(50, () -> {
            String text = txtArea.getText();
            txtArea.setText(text);
            txtArea.positionCaret(txtArea.getText().length());
            txtArea.requestFocus();
        });
    }

    private Font getFont(){
        return FontUtils.getFont(fontCombo.getSelectionModel().getSelectedItem(), itBtn.isSelected(), boldBtn.isSelected(), sizeSpinner.getValueFactory().getValue());
    }

    private ScrollBar getHorizontalSB(final TextArea scrollPane){
        return scrollPane.lookupAll(".scroll-bar")
                .stream()
                .filter(node -> node instanceof ScrollBar)
                .map(node -> (ScrollBar) node)
                .filter(sb -> sb.getOrientation() == Orientation.HORIZONTAL)
                .findFirst()
                .orElse(null);
    }
}
