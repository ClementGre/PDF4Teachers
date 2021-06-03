package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.ScaledComboBox;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.components.FontComboBox;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToDoubleConverter;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class TextTab extends SideTab{
    
    public VBox pane = new VBox();
    public VBox optionPane = new VBox();
    
    // OPTIONS DE MISE EN PAGE + INPUTS + BOUTONS
    // Séparés par ligne
    
    private final HBox combosBox = new HBox();
    public FontComboBox fontCombo = new FontComboBox(true);
    private final Spinner<Double> sizeCombo = new Spinner<>(2d, 999d, 14d, 2d);
    
    private final HBox colorAndParamsBox = new HBox();
    private final SyncColorPicker colorPicker = new SyncColorPicker();
    private final ToggleButton boldBtn = new ToggleButton("");
    private final ToggleButton itBtn = new ToggleButton("");
    
    public TextArea txtArea = new TextArea();
    
    private final HBox btnBox = new HBox();
    private final Button deleteBtn = new Button(TR.tr("actions.delete"));
    public Button newBtn = new Button(TR.tr("actions.new"));
    
    // FIELDS
    
    public boolean isNew = false;
    
    // TREEVIEW
    public TextTreeView treeView;
    
    // OTHER
    
    private boolean txtAreaScrollBarListenerIsSetup = false;
    
    public TextTab(){
        super("text", SVGPathIcons.TEXT_LETTER, 0, 28, new int[]{460, 500});
        
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
        
        PaneUtils.setHBoxPosition(sizeCombo, 95, 30, 2.5);
        sizeCombo.setStyle("-fx-font-size: 13");
        sizeCombo.setEditable(true);
        sizeCombo.getValueFactory().setConverter(new StringToDoubleConverter(14));
        sizeCombo.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        sizeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
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
        boldBtn.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/TextTab/bold.png") + "", 0, 0, ImageUtils.defaultFullDarkColorAdjust));
        boldBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        boldBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(isNew) MainWindow.userData.textLastFontBold = newValue;
        });
        
        PaneUtils.setHBoxPosition(itBtn, 45, 29, 2.5);
        itBtn.setCursor(Cursor.HAND);
        itBtn.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/TextTab/italic.png") + "", 0, 0, ImageUtils.defaultFullDarkColorAdjust));
        itBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        itBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(isNew) MainWindow.userData.textLastFontItalic = newValue;
        });
        
        PaneUtils.setHBoxPosition(txtArea, -1, 30, 0);
        if(Main.settings.textSmall.getValue()) txtArea.setStyle("-fx-font-size: 12");
        else txtArea.setStyle("-fx-font-size: 13");
        txtArea.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.getSelected() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        txtArea.setPromptText(TR.tr("textTab.Latex.help"));
        txtArea.setId("no-vertical-scroll-bar");
        txtArea.setFocusTraversable(false);
        
        PaneUtils.setHBoxPosition(deleteBtn, -1, 30, 2.5);
        deleteBtn.disableProperty().bind(Bindings.createBooleanBinding(() -> MainWindow.mainScreen.selectedProperty().get() == null || !(MainWindow.mainScreen.getSelected() instanceof TextElement), MainWindow.mainScreen.selectedProperty()));
        
        PaneUtils.setHBoxPosition(newBtn, -1, 30, 2.5);
        newBtn.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        
        combosBox.getChildren().addAll(fontCombo, sizeCombo);
        colorAndParamsBox.getChildren().addAll(colorPicker, boldBtn, itBtn);
        btnBox.getChildren().addAll(deleteBtn, newBtn);
        
        VBox.setMargin(combosBox, new Insets(2.5, 2.5, 0, 2.5));
        VBox.setMargin(colorAndParamsBox, new Insets(0, 2.5, 0, 2.5));
        VBox.setMargin(txtArea, new Insets(2.5, 5, 2.5, 5));
        VBox.setMargin(btnBox, new Insets(0, 2.5, 7.5, 2.5));
        optionPane.getChildren().addAll(combosBox, colorAndParamsBox, txtArea, btnBox);
        
        
        MainWindow.mainScreen.selectedProperty().addListener((ObservableValue<? extends Element> observable, Element oldElement, Element newElement) -> {
            isNew = false;
            if(oldElement != null){
                if(oldElement instanceof TextElement current){
                    current.textProperty().unbind();
                    current.fontProperty().unbind();
                    
                    if(((TextElement) oldElement).getText().isBlank()){
                        oldElement.delete(true);
                    }
                }
            }
            if(newElement != null){
                if(newElement instanceof TextElement current){
    
                    txtArea.setText(current.getText());
                    boldBtn.setSelected(FontUtils.getFontWeight(current.getFont()) == FontWeight.BOLD);
                    itBtn.setSelected(FontUtils.getFontPosture(current.getFont()) == FontPosture.ITALIC);
                    colorPicker.setValue(current.getColor());
                    fontCombo.getSelectionModel().select(current.getFont().getFamily());
                    sizeCombo.getValueFactory().setValue(current.getFont().getSize());
                    
                    current.fontProperty().bind(Bindings.createObjectBinding(() -> {
                        Edition.setUnsave("TextElement FontChanged");
                        return getFont();
                    }, fontCombo.getSelectionModel().selectedItemProperty(), sizeCombo.valueProperty(), itBtn.selectedProperty(), boldBtn.selectedProperty()));
                }
            }
        });
        
        ContextMenu menu = new ContextMenu();
        MenuItem deleteReturn = new NodeMenuItem(TR.tr("textTab.fieldActions.deleteUselessLineBreak"), true);
        deleteReturn.setOnAction(event -> {
            String wrapped = new TextWrapper(txtArea.getText().replaceAll(Pattern.quote("\n"), " "), ((TextElement) MainWindow.mainScreen.getSelected()).getFont(), (int) MainWindow.mainScreen.getSelected().getPage().getWidth()).wrap();
            if(txtArea.getText().endsWith(" ")) wrapped += " ";
            
            if(!wrapped.equals(txtArea.getText())){
                int positionCaret = txtArea.getCaretPosition();
                txtArea.setText(wrapped);
                txtArea.positionCaret(positionCaret);
            }
            Platform.runLater(() -> MainWindow.mainScreen.getSelected().checkLocation(false));
        });
        menu.getItems().add(deleteReturn);
        txtArea.setContextMenu(menu);
        
        txtArea.disableProperty().addListener((observable, oldValue, newValue) -> treeView.updateAutoComplete());
        MainWindow.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> treeView.updateAutoComplete());
        
        txtArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            
            if(newValue.contains("\u0009")){ // TAB
                txtArea.setText(newValue.replaceAll(Pattern.quote("\u0009"), ""));
                return;
            }
            
            if(!TextElement.isLatex(newValue)){
                // WRAP TEXT
                if(MainWindow.mainScreen.getSelected() == null) return;
                Platform.runLater(() -> {
                    if(MainWindow.mainScreen.getSelected() == null) return;
                    String wrapped = new TextWrapper(newValue, ((TextElement) MainWindow.mainScreen.getSelected()).getFont(), (int) MainWindow.mainScreen.getSelected().getPage().getWidth()).wrap();
                    if(newValue.endsWith(" ")) wrapped += " ";
                    
                    if(!wrapped.equals(newValue)){
                        int positionCaret = txtArea.getCaretPosition();
                        txtArea.setText(wrapped);
                        txtArea.positionCaret(positionCaret);
                    }
                    Platform.runLater(() -> MainWindow.mainScreen.getSelected().checkLocation(false));
                });
            }
            
            treeView.updateAutoComplete();
            
            updateHeightAndYLocations(getHorizontalSB(txtArea).isVisible());
            if(!txtAreaScrollBarListenerIsSetup){
                getHorizontalSB(txtArea).visibleProperty().addListener((ObservableValue<? extends Boolean> observableTxt, Boolean oldTxtValue, Boolean newTxtValue) -> updateHeightAndYLocations(newTxtValue));
                txtAreaScrollBarListenerIsSetup = true;
            }
            ((TextElement) MainWindow.mainScreen.getSelected()).setText(newValue);
            if(new Random().nextInt(10) == 0) AutoTipsManager.showByAction("textedit");
        });
        sizeCombo.valueProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            if(!((TextElement) MainWindow.mainScreen.getSelected()).isLatex()){
                String wrapped = new TextWrapper(txtArea.getText(), ((TextElement) MainWindow.mainScreen.getSelected()).getFont(), (int) MainWindow.mainScreen.getSelected().getPage().getWidth()).wrap();
                if(txtArea.getText().endsWith(" ")) wrapped += " ";
                
                if(!wrapped.equals(txtArea.getText())){
                    int positionCaret = txtArea.getCaretPosition();
                    txtArea.setText(wrapped);
                    txtArea.positionCaret(positionCaret);
                }
            }
        }));
        txtArea.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE || (e.getCode() == KeyCode.BACK_SPACE && e.isShortcutDown())){
                e.consume();
                if(txtArea.getCaretPosition() == txtArea.getText().length()){
                    Element element = MainWindow.mainScreen.getSelected();
                    if(element != null){
                        MainWindow.mainScreen.setSelected(null);
                        element.delete(true);
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
                    MainWindow.keyboardShortcuts.reportKeyPressedForMultipleUsesKeys(e);
                }
            }else if(e.getCode() == KeyCode.UP && txtArea.getText().split("\n").length == 1){
                e.consume();
                if(TextTreeItem.lastKeyPressTime > System.currentTimeMillis() - 100) return;
                else TextTreeItem.lastKeyPressTime = System.currentTimeMillis();
                pane.requestFocus();
                if(!treeView.selectPreviousInSelection()){
                    MainWindow.keyboardShortcuts.reportKeyPressedForMultipleUsesKeys(e);
                }
            }
        });
        colorPicker.setOnAction((ActionEvent e) -> {
            if(MainWindow.mainScreen.getSelected() != null){
                if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                    ((TextElement) MainWindow.mainScreen.getSelected()).setColor(colorPicker.getValue());
                    Edition.setUnsave("TextElement color changed");
                }
                
            }
        });
        newBtn.setOnAction(e -> {
            
            PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
            
            MainWindow.mainScreen.setSelected(null);
            
            fontCombo.getSelectionModel().select(MainWindow.userData.textLastFontName.isEmpty() ? "Open Sans" : MainWindow.userData.textLastFontName);
            sizeCombo.getValueFactory().setValue(MainWindow.userData.textLastFontSize);
            colorPicker.setValue(Color.valueOf(MainWindow.userData.textLastFontColor.isEmpty() ? "#000000" : MainWindow.userData.textLastFontColor));
            boldBtn.setSelected(MainWindow.userData.textLastFontBold);
            itBtn.setSelected(MainWindow.userData.textLastFontItalic);
            
            TextElement current = new TextElement((int) (60 * Element.GRID_WIDTH / page.getWidth()), (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight()), page.getPage(),
                    true, txtArea.getText(), colorPicker.getValue(), getFont());
            
            page.addElement(current, true);
            current.centerOnCoordinatesY();
            MainWindow.mainScreen.setSelected(current);
            isNew = true;
            
            txtArea.setText("");
            TextTreeView.addSavedElement(current.toNoDisplayTextElement(TextTreeSection.LAST_TYPE, true));
            txtArea.requestFocus();
            
            AutoTipsManager.showByAction("newtextelement");
        });
        deleteBtn.setOnAction(e -> {
            MainWindow.mainScreen.getSelected().delete(true);
            MainWindow.mainScreen.setSelected(null);
        });
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
        PlatformUtils.runLaterOnUIThread(50, () ->{
            String text = txtArea.getText();
            txtArea.setText(text);
            txtArea.positionCaret(txtArea.getText().length());
            txtArea.requestFocus();
        });
    }
    
    private Font getFont(){
        return FontUtils.getFont(fontCombo.getSelectionModel().getSelectedItem(), itBtn.isSelected(), boldBtn.isSelected(), sizeCombo.getValueFactory().getValue());
    }
    
    private ScrollBar getHorizontalSB(final TextArea scrollPane){
        Set<Node> nodes = scrollPane.lookupAll(".scroll-bar");
        for(final Node node : nodes){
            if(node instanceof ScrollBar sb){
                if(sb.getOrientation() == Orientation.HORIZONTAL){
                    return sb;
                }
            }
        }
        return null;
    }
}