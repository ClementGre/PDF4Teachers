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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;
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

    // AUTOCOMPLETE DROPDOWN
    private final Popup autocompletePopup = new Popup();
    private final ListView<TextTreeItem> autocompleteList = new ListView<>();
    private boolean isSelectingFromPopup = false;

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

        // Setup autocomplete popup
        setupAutocompletePopup();

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

        txtArea.disableProperty().addListener((observable, oldValue, newValue) -> {
            treeView.updateAutoComplete();
            if(newValue) autocompletePopup.hide();
        });
        MainWindow.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
            treeView.updateAutoComplete();
            autocompletePopup.hide();
        });

        txtArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

            if(newValue.contains("\u0009")){ // TAB
                txtArea.setText(newValue.replaceAll(Pattern.quote("\u0009"), ""));
                return;
            }

            // Default LaTeX
            newValue = TextElement.invertMathIfNeeded(newValue);

            if(MainWindow.mainScreen.getSelected() instanceof TextElement element){
                // Only update autocomplete if not selecting from popup
                if(!isSelectingFromPopup){
                    treeView.updateAutoComplete();
                    updateAutocompletePopup();
                }

                updateHeightAndYLocations(getHorizontalSB(txtArea).isVisible());
                if(!txtAreaScrollBarListenerIsSetup){
                    getHorizontalSB(txtArea).visibleProperty().addListener((ObservableValue<? extends Boolean> observableTxt, Boolean oldTxtValue, Boolean newTxtValue) -> updateHeightAndYLocations(newTxtValue));
                    txtAreaScrollBarListenerIsSetup = true;
                }
                element.setText(newValue);
                if(new Random().nextInt(10) == 0) AutoTipsManager.showByAction("textedit");
            }
        });

        // Event filter for KEY_PRESSED to intercept Enter key
        txtArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            System.out.println("DEBUG: KEY_PRESSED filter - Key: " + e.getCode() + ", Popup: " + autocompletePopup.isShowing() + ", Char: '" + e.getCharacter() + "'");

            if(autocompletePopup.isShowing()){
                // Check for ENTER or UNDEFINED with empty/newline character
                boolean isEnterKey = e.getCode() == KeyCode.ENTER ||
                                    (e.getCode() == KeyCode.UNDEFINED &&
                                     (e.getCharacter().isEmpty() ||
                                      e.getCharacter().equals("\r") ||
                                      e.getCharacter().equals("\n")));

                if(isEnterKey){
                    System.out.println("DEBUG: Enter detected in KEY_PRESSED - calling selectAutocompleteItem()");
                    e.consume();
                    selectAutocompleteItem();
                }
            }
        });

        // Event filter for KEY_TYPED to catch Enter as typed character
        txtArea.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            System.out.println("DEBUG: KEY_TYPED filter - Char: '" + e.getCharacter() + "', Popup: " + autocompletePopup.isShowing());

            if(autocompletePopup.isShowing() &&
               (e.getCharacter().equals("\r") || e.getCharacter().equals("\n"))){
                System.out.println("DEBUG: Enter detected in KEY_TYPED - calling selectAutocompleteItem()");
                e.consume();
                selectAutocompleteItem();
            }
        });

        txtArea.setOnKeyPressed(e -> {
            // Handle autocomplete popup navigation
            if(autocompletePopup.isShowing()){
                if(e.getCode() == KeyCode.ENTER){
                    // Already handled by event filter
                    e.consume();
                    return;
                }else if(e.getCode() == KeyCode.DOWN){
                    e.consume();
                    int currentIndex = autocompleteList.getSelectionModel().getSelectedIndex();
                    if(currentIndex == -1){
                        // No selection, select first
                        autocompleteList.getSelectionModel().selectFirst();
                        autocompleteList.scrollTo(0);
                    }else if(currentIndex < autocompleteList.getItems().size() - 1){
                        // Move down
                        autocompleteList.getSelectionModel().select(currentIndex + 1);
                        autocompleteList.scrollTo(currentIndex + 1);
                    }
                    // At last item, stay there - don't fall through to tree view navigation
                    return;
                }else if(e.getCode() == KeyCode.UP){
                    e.consume();
                    int currentIndex = autocompleteList.getSelectionModel().getSelectedIndex();
                    if(currentIndex == -1){
                        // No selection, select last
                        autocompleteList.getSelectionModel().selectLast();
                        autocompleteList.scrollTo(autocompleteList.getItems().size() - 1);
                    }else if(currentIndex > 0){
                        // Move up
                        autocompleteList.getSelectionModel().select(currentIndex - 1);
                        autocompleteList.scrollTo(currentIndex - 1);
                    }
                    // At first item, stay there - don't fall through to tree view navigation
                    return;
                }else if(e.getCode() == KeyCode.ESCAPE){
                    e.consume();
                    autocompletePopup.hide();
                    // Remove focus from txtArea to enable app-level keyboard navigation
                    pane.requestFocus();
                    return;
                }
            }

            // ESC key when popup is NOT showing - still remove focus to enable app navigation
            if(e.getCode() == KeyCode.ESCAPE){
                e.consume();
                pane.requestFocus();
                return;
            }

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
                else TextTreeItem.lastKeyPressTime = System.currentTimeMillis();
                pane.requestFocus();
                if(!treeView.selectNextInSelection()){
                    txtArea.requestFocus();
                }
            }else if(e.getCode() == KeyCode.UP && txtArea.getText().split("\n").length == 1){
                e.consume();
                if(TextTreeItem.lastKeyPressTime > System.currentTimeMillis() - 100) return;
                else TextTreeItem.lastKeyPressTime = System.currentTimeMillis();
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

    private void setupAutocompletePopup(){
        autocompleteList.setPrefHeight(250);
        autocompleteList.setMaxHeight(400);
        autocompleteList.setStyle("-fx-background-color: white; -fx-border-color: #999999; -fx-border-width: 1px;");

        // Custom cell factory to display TextTreeItems
        autocompleteList.setCellFactory(param -> new ListCell<TextTreeItem>(){
            private final Label label = new Label();

            {
                // Initialize label
                label.setMaxWidth(Double.MAX_VALUE);
                label.setPadding(new Insets(3, 5, 3, 5));

                // Update label style when selection state changes
                selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    if(getItem() != null){
                        if(isNowSelected){
                            label.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
                        }else{
                            label.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(TextTreeItem item, boolean empty){
                super.updateItem(item, empty);
                if(empty || item == null){
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                }else{
                    String displayText = TextElement.invertMathIfNeeded(item.getText());
                    // Truncate long text for display
                    if(displayText.length() > 80){
                        displayText = displayText.substring(0, 77) + "...";
                    }
                    label.setText(displayText);

                    // Update label color based on selection state
                    if(isSelected()){
                        label.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
                    }else{
                        label.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
                    }

                    setText(null);
                    setGraphic(label);
                    // Don't override background - let JavaFX handle selection highlighting
                    setStyle("-fx-padding: 2px;");
                }
            }
        });

        // Handle mouse clicks on list items
        autocompleteList.setOnMouseClicked(event -> {
            if(event.getClickCount() == 1 && autocompleteList.getSelectionModel().getSelectedItem() != null){
                selectAutocompleteItem();
            }
        });

        autocompletePopup.getContent().add(autocompleteList);
        autocompletePopup.setAutoHide(true);
        autocompletePopup.setAutoFix(true);
    }

    private void updateAutocompletePopup(){
        if(isSelectingFromPopup) return;

        String matchText = txtArea.getText();

        if(txtArea.isDisabled() || matchText.isBlank()){
            autocompletePopup.hide();
            return;
        }

        // Collect matching items from all sections
        List<TextTreeItem> matchingItems = new ArrayList<>();

        // Check favorites
        for(int i = 0; i < treeView.favoritesSection.getChildren().size(); i++){
            if(treeView.favoritesSection.getChildren().get(i) instanceof TextTreeItem item){
                if(item.getCore() != MainWindow.mainScreen.getSelected()
                        && TextElement.invertMathIfNeeded(item.getText()).toLowerCase().contains(matchText.toLowerCase())){
                    matchingItems.add(item);
                }
            }
        }

        // Check lasts
        for(int i = 0; i < treeView.lastsSection.getChildren().size(); i++){
            if(treeView.lastsSection.getChildren().get(i) instanceof TextTreeItem item){
                if(item.getCore() != MainWindow.mainScreen.getSelected()
                        && TextElement.invertMathIfNeeded(item.getText()).toLowerCase().contains(matchText.toLowerCase())){
                    matchingItems.add(item);
                }
            }
        }

        // Check onFile
        for(int i = 0; i < treeView.onFileSection.getChildren().size(); i++){
            if(treeView.onFileSection.getChildren().get(i) instanceof TextTreeItem item){
                if(item.getCore() != MainWindow.mainScreen.getSelected()
                        && TextElement.invertMathIfNeeded(item.getText()).toLowerCase().contains(matchText.toLowerCase())){
                    matchingItems.add(item);
                }
            }
        }

        // Update popup
        if(matchingItems.isEmpty()){
            autocompletePopup.hide();
        }else{
            autocompleteList.getItems().setAll(matchingItems);
            autocompleteList.getSelectionModel().selectFirst();

            // Position popup below txtArea
            if(!autocompletePopup.isShowing()){
                var bounds = txtArea.localToScreen(txtArea.getBoundsInLocal());
                if(bounds != null){
                    autocompletePopup.show(txtArea, bounds.getMinX(), bounds.getMaxY());
                }
            }

            // Adjust width to match txtArea
            autocompleteList.setPrefWidth(txtArea.getWidth() - 10);
        }
    }

    private void selectAutocompleteItem(){
        System.out.println("DEBUG: selectAutocompleteItem() called");
        TextTreeItem selectedItem = autocompleteList.getSelectionModel().getSelectedItem();
        System.out.println("DEBUG: Selected item: " + selectedItem);
        System.out.println("DEBUG: Selection index: " + autocompleteList.getSelectionModel().getSelectedIndex());
        System.out.println("DEBUG: Items count: " + autocompleteList.getItems().size());

        if(selectedItem != null){
            System.out.println("DEBUG: Selected item text: " + selectedItem.getText());
            isSelectingFromPopup = true;

            // Hide popup first to prevent visual glitches
            autocompletePopup.hide();

            // Set the text to the selected item's text
            String selectedText = TextElement.invertMathIfNeeded(selectedItem.getText());
            System.out.println("DEBUG: Setting text to: " + selectedText);
            txtArea.setText(selectedText);
            txtArea.positionCaret(selectedText.length());

            // Update formatting from the selected item
            fontCombo.getSelectionModel().select(selectedItem.getFont().getFamily());
            sizeSpinner.getValueFactory().setValue(selectedItem.getFont().getSize());
            colorPicker.setValue(selectedItem.getColor());
            boldBtn.setSelected(FontUtils.getFontWeight(selectedItem.getFont()) == FontWeight.BOLD);
            itBtn.setSelected(FontUtils.getFontPosture(selectedItem.getFont()) == FontPosture.ITALIC);

            txtArea.requestFocus();

            // Clear flag after all events have been processed
            PlatformUtils.runLaterOnUIThread(50, () -> {
                isSelectingFromPopup = false;
            });
        }
    }
}
