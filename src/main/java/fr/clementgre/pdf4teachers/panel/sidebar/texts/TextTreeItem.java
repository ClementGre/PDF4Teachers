package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.KeyableHBox;
import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class TextTreeItem extends TreeItem<String>{
    
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>();
    private String text;
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>();
    
    private int type;
    private long uses;
    private long creationDate;
    
    // Graphics items
    private final HBox spacer = new HBox();
    public KeyableHBox pane = new KeyableHBox();
    public Pane namePane = new Pane();
    public ImageView linkImage = ImageUtils.buildImage(getClass().getResource("/img/TextTab/link.png") + "", 0, 0, ImageUtils.defaultFullDarkColorAdjust);
    public ScratchText name = new ScratchText();
    public ContextMenu menu;
    public EventHandler<MouseEvent> onMouseCLick;
    
    // Link
    private TextElement core = null;
    private final ChangeListener<String> textChangeListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
        setText(newValue);
        updateGraphic(true);
    };
    private final ChangeListener<Paint> colorChangeListener = (ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) -> {
        setColor((Color) newValue);
    };
    
    
    public TextTreeItem(Font font, String text, Color color, int type, long uses, long creationDate){
        this.font.set(font);
        this.text = text;
        this.color.set(color);
        this.type = type;
        this.uses = uses;
        this.creationDate = creationDate;
        
        setup();
        updateGraphic(false);
    }
    
    public TextTreeItem(Font font, String text, Color color, int type, long uses, long creationDate, TextElement core){
        this.font.set(font);
        this.text = text;
        this.color.set(color);
        this.type = type;
        this.uses = uses;
        this.creationDate = creationDate;
        this.core = core;
        
        setup();
    }
    
    public void setup(){
        
        if(core != null){
            // bindings with the core
            fontProperty().bind(core.fontProperty());
            core.textProperty().addListener(textChangeListener);
            core.fillProperty().addListener(colorChangeListener);
        }
        
        // Setup les éléments graphiques
        menu = TextTreeView.getNewMenu(this);
        
        onMouseCLick = (MouseEvent e) -> {
            if(e.getButton().equals(MouseButton.PRIMARY)){
                
                if(MainWindow.mainScreen.getSelected() instanceof TextElement oldElement && e.isShortcutDown()){
                    oldElement.delete(true);
                    addToDocument(e.isShiftDown(), oldElement.getPage(), oldElement.getRealX(), oldElement.getRealY(), false);
                }else{
                    addToDocument(e.isShiftDown());
                }
                
                // Update the sorting if is sort by utils
                if(getType() == TextTreeSection.FAVORITE_TYPE){
                    if(MainWindow.textTab.treeView.favoritesSection.sortManager.getSelectedButton().getText().equals(TR.tr("sorting.sortType.use"))){
                        MainWindow.textTab.treeView.favoritesSection.sortManager.simulateCall();
                    }
                }else if(getType() == TextTreeSection.LAST_TYPE){
                    if(MainWindow.textTab.treeView.lastsSection.sortManager.getSelectedButton().getText().equals(TR.tr("sorting.sortType.use"))){
                        MainWindow.textTab.treeView.lastsSection.sortManager.simulateCall();
                    }
                }
                MainWindow.textTab.selectItem();
                e.consume();
            }
        };
        name.setFill(StyleManager.shiftColorWithTheme(color.get()));
        colorProperty().addListener((observable, oldValue, newValue) -> {
            name.setFill(StyleManager.shiftColorWithTheme(newValue));
        });
        
        name.fontProperty().bind(Bindings.createObjectBinding(this::getListFont, fontProperty(), Main.settings.textSmall.valueProperty()));
        
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.setFocusTraversable(false);
        
        pane.setBorder(new Border(new BorderStroke(Color.web("#0078d7", 0), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        pane.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue)
                pane.setBorder(new Border(new BorderStroke(Color.web("#0078d7", 1), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
            else
                pane.setBorder(new Border(new BorderStroke(Color.web("#0078d7", 0), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        });
        
        pane.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.RIGHT){
                e.consume();
                if(lastKeyPressTime > System.currentTimeMillis() - 100) return;
                else lastKeyPressTime = System.currentTimeMillis();
                MainWindow.textTab.treeView.selectNextInSelection();
            }else if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.LEFT){
                e.consume();
                if(lastKeyPressTime > System.currentTimeMillis() - 100) return;
                else lastKeyPressTime = System.currentTimeMillis();
                MainWindow.textTab.treeView.selectPreviousInSelection();
            }else if(e.getCode() == KeyCode.ENTER){
                e.consume();
                if(MainWindow.mainScreen.getSelected() instanceof TextElement oldElement){
                    oldElement.delete(true);
                    addToDocument(e.isShiftDown(), oldElement.getPage(), oldElement.getRealX(), oldElement.getRealY(), false);
                }else{
                    addToDocument(e.isShiftDown());
                    MainWindow.textTab.selectItem();
                }
                
                // Update the sorting if is sort by utils
                if(getType() == TextTreeSection.FAVORITE_TYPE){
                    if(MainWindow.textTab.treeView.favoritesSection.sortManager.getSelectedButton().getText().equals(TR.tr("sorting.sortType.use"))){
                        MainWindow.textTab.treeView.favoritesSection.sortManager.simulateCall();
                    }
                }else if(getType() == TextTreeSection.LAST_TYPE){
                    if(MainWindow.textTab.treeView.lastsSection.sortManager.getSelectedButton().getText().equals(TR.tr("sorting.sortType.use"))){
                        MainWindow.textTab.treeView.lastsSection.sortManager.simulateCall();
                    }
                }
                MainWindow.textTab.selectItem();
            }
        });
        pane.setOnKeyTyped((e) -> {
            if(e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.UP || e.getCode() == KeyCode.LEFT){
                e.consume();
            }
        });
        pane.setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.UP || e.getCode() == KeyCode.LEFT){
                e.consume();
            }
        });
        
        updateIcon();
    }
    public void onSelected(){
        pane.requestFocus();
    }
    
    static long lastKeyPressTime = 0;
    public void updateGraphic(boolean updateParentHeight){ // Re calcule le Text
        int maxWidth = (int) (MainWindow.textTab.treeView.getWidth() - 42);
        if(maxWidth < 0) return;
    
        Font font = getListFont();
        String wrappedText = "";
        final String[] splitText = getText().split(Pattern.quote("\n"));
        
        if(splitText.length != 0){
            if(Main.settings.textOnlyStart.getValue()){
                
                String text = splitText[0];
                wrappedText += new TextWrapper(text, font, maxWidth).wrapFirstLine();
                text = text.replaceFirst(Pattern.quote(wrappedText), "");
                
                // SECOND LINE
                if(!text.isBlank()){
                    String wrapped = new TextWrapper(text, font, maxWidth - 13).wrapFirstLine();
                    wrappedText += "\n" + wrapped;
                    if(!text.replaceFirst(Pattern.quote(wrapped), "").isBlank()) wrappedText += "...";
                }else if(splitText.length > 1){
                    String wrapped = new TextWrapper(splitText[1], font, maxWidth - 13).wrapFirstLine();
                    wrappedText += "\n" + wrapped;
                    if(!splitText[1].replaceFirst(Pattern.quote(wrapped), "").isBlank()) wrappedText += "...";
                }
            }else{
                for(String text : splitText){
                    wrappedText += wrappedText.isEmpty() ? new TextWrapper(text, font, maxWidth).wrap() : "\n" + new TextWrapper(text, font, maxWidth).wrap();
                }
            }
        }
        
        name.setText(wrappedText);
        name.setFill(StyleManager.shiftColorWithTheme(color.get()));
        
        // VARS DEFINED
        int negativePadding = (Main.settings.textSmall.getValue() ? 6 : 4);
        int cellHeight = (int) name.getLayoutBounds().getHeight() - negativePadding;
        int lineHeight = (int) name.getLayoutBounds().getHeight() / name.getText().split(Pattern.quote("\n")).length - negativePadding;
        
        linkImage.setFitWidth(12);
        linkImage.setFitHeight(12);
        spacer.setPrefWidth(linkImage.getFitWidth() + 3 + rect.getWidth() + 3);
        
        HBox.setMargin(rect, new Insets(((lineHeight - rect.getHeight()) / 2.0), 3, 0, 3));
        if(core != null){
            HBox.setMargin(linkImage, new Insets(((lineHeight - linkImage.getFitHeight()) / 2.0), 0, 0, 0));
        }
        HBox.setMargin(namePane, new Insets(-negativePadding / 2d, 0, -negativePadding / 2d, 0));
        
        if(updateParentHeight && pane.getParent() != null && pane.getParent() instanceof TreeCell){
            ((TreeCell<?>) pane.getParent()).setPrefHeight(cellHeight + 4);
        }
    }
    
    Rectangle rect = new Rectangle();
    
    public void updateIcon(){ // Re définis les children de la pane
        int negativePadding = (Main.settings.textSmall.getValue() ? 6 : 4);
        int cellHeight = (int) name.getLayoutBounds().getHeight() - negativePadding;
        
        rect.setWidth(4);
        rect.setHeight(4);
        rect.setFill(StyleManager.invertColorWithTheme(Color.WHITE));
        
        spacer.getChildren().clear();
        spacer.setAlignment(Pos.TOP_RIGHT);
        
        if(core != null){
            spacer.getChildren().add(linkImage);
        }
        
        namePane.getChildren().setAll(name);
        name.setTextOrigin(VPos.TOP);
        
        spacer.getChildren().add(rect);
        pane.getChildren().setAll(spacer, namePane);
        
    }
    
    public void updateCell(TreeCell<String> cell){ // Réattribue une cell à la pane
        if(cell == null) return;
        if(name.getText().isEmpty()) updateGraphic(false);
        
        name.setFill(StyleManager.shiftColorWithTheme(color.get()));
        rect.setFill(StyleManager.invertColorWithTheme(Color.WHITE));
        
        int negativePadding = (Main.settings.textSmall.getValue() ? 6 : 4);
        int cellHeight = (int) name.getLayoutBounds().getHeight() - negativePadding;
        
        cell.setPrefHeight(cellHeight + 4);
        cell.setGraphic(pane);
        cell.setStyle(null);
        cell.setStyle("-fx-padding: 0 0 0 -38;"); // top - right - bottom - left
        cell.setContextMenu(menu);
        cell.setOnMouseClicked(onMouseCLick);
    
        if(MainWindow.textTab.treeView.getSelectionModel().getSelectedItem() == this) pane.requestFocus();
    }
    
    private Font getListFont(){
        return FontUtils.getFont(getFont().getFamily(), false, false, Main.settings.textSmall.getValue() ? 12 : 14);
    }
    
    @Override
    public boolean equals(Object v){
        
        if(v instanceof TextTreeItem element){
            if(element.type == type && element.text.equals(text) && element.color.hashCode() == color.hashCode()){
                if(element.font.get().getStyle().equals(font.get().getStyle()) && element.font.get().getSize() == font.get().getSize() && element.getFont().getFamily().equals(font.get().getFamily())){
                    return true;
                }
            }
        }
        return false;
    }
    
    public TextTreeItem clone(){
        return new TextTreeItem(font.get(), text, color.get(), type, uses, creationDate);
    }
    
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
        data.put("color", color.get().toString());
        data.put("font", font.get().getFamily());
        data.put("size", font.get().getSize());
        data.put("bold", FontUtils.getFontWeight(font.get()) == FontWeight.BOLD);
        data.put("italic", FontUtils.getFontPosture(font.get()) == FontPosture.ITALIC);
        data.put("uses", uses);
        data.put("date", creationDate);
        data.put("text", text);
        
        return data;
    }
    
    public TextElement toRealTextElement(int x, int y, int page){
        return new TextElement(x, y, page, true, text, color.get(), font.get());
    }
    
    public static TextTreeItem readDataAndGive(DataInputStream reader, int type) throws IOException{
        
        double fontSize = reader.readFloat();
        boolean isBold = reader.readBoolean();
        boolean isItalic = reader.readBoolean();
        String fontName = reader.readUTF();
        short colorRed = (short) (reader.readByte() + 128);
        short colorGreen = (short) (reader.readByte() + 128);
        short colorBlue = (short) (reader.readByte() + 128);
        long uses = reader.readLong();
        long creationDate = reader.readLong();
        String text = reader.readUTF();
        
        Font font = FontUtils.getFont(fontName, isItalic, isBold, (int) fontSize);
        
        return new TextTreeItem(font, text, Color.rgb(colorRed, colorGreen, colorBlue), type, uses, creationDate);
    }
    
    public static TreeItem<String> readYAMLDataAndGive(HashMap<String, Object> data, int type){
        
        double fontSize = Config.getDouble(data, "size");
        boolean isBold = Config.getBoolean(data, "bold");
        boolean isItalic = Config.getBoolean(data, "italic");
        String fontName = Config.getString(data, "font");
        Color color = Color.valueOf(Config.getString(data, "color"));
        long uses = Config.getLong(data, "uses");
        long creationDate = Config.getLong(data, "date");
        String text = Config.getString(data, "text");
        
        Font font = FontUtils.getFont(fontName, isItalic, isBold, (int) fontSize);
        
        return new TextTreeItem(font, text, color, type, uses, creationDate);
    }
    
    public TextListItem toTextItem(){
        return new TextListItem(font.get(), text, color.get(), uses, creationDate);
    }
    
    public void addToDocument(boolean link){
        
        if(MainWindow.mainScreen.hasDocument(false)){
            PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
            
            int y = (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight());
            int x = (int) ((page.getMouseX() <= 0 ? 60 : page.getMouseX()) * Element.GRID_WIDTH / page.getWidth());
            addToDocument(link, page, x, y, true);
        }
        
    }
    
    public void addToDocument(boolean link, PageRenderer page, int x, int y, boolean centerOnY){
        
        if(MainWindow.mainScreen.hasDocument(false)){
            uses++;
            TextElement realElement = toRealTextElement(x, y, page.getPage());
            
            if(link){
                // UnBind with the core
                if(core != null){
                    fontProperty().unbind();
                    core.textProperty().removeListener(textChangeListener);
                    core.fillProperty().removeListener(colorChangeListener);
                }
                
                core = realElement;
                setup();
            }
            
            page.addElement(realElement, true);
            if(centerOnY) realElement.centerOnCoordinatesY();
            MainWindow.mainScreen.selectedProperty().setValue(realElement);
        }
        
    }
    
    public void unLink(boolean reSetupLayout){
        fontProperty().unbind();
        if(core != null){
            core.textProperty().removeListener(textChangeListener);
            core.fillProperty().removeListener(colorChangeListener);
            core = null;
        }
        if(reSetupLayout) setup();
    }
    
    public Font getFont(){
        return font.get();
    }
    
    public ObjectProperty<Font> fontProperty(){
        return font;
    }
    
    public void setFont(Font font){
        this.font.set(font);
    }
    
    public String getText(){
        return text;
    }
    
    public void setText(String text){
        this.text = text;
    }
    
    public Color getColor(){
        return color.get();
    }
    
    public ObjectProperty<Color> colorProperty(){
        return color;
    }
    
    public void setColor(Color color){
        this.color.set(color);
    }
    
    public int getType(){
        return type;
    }
    
    public void setType(int type){
        this.type = type;
    }
    
    public long getUses(){
        return uses;
    }
    
    public void setUses(long uses){
        this.uses = uses;
    }
    
    public long getCreationDate(){
        return creationDate;
    }
    
    public void setCreationDate(long creationDate){
        this.creationDate = creationDate;
    }
    
    public TextElement getCore(){
        return core;
    }
}