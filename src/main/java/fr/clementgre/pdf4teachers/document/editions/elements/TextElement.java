/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.datasaving.settings.Settings;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.ObservableChangedUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextBoundsType;
import org.jetbrains.annotations.NotNull;
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import writer2latex.api.ConverterFactory;
import writer2latex.api.StarMathConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class TextElement extends Element {
    
    private final ScratchText textNode = new ScratchText();
    private final ImageView image = new ImageView();
    
    private final StringProperty text = new SimpleStringProperty();
    // Must be between 0 and 100 in percents.
    private final DoubleProperty maxWidth = new SimpleDoubleProperty();
    
    private final BooleanProperty isTextWrapped = new SimpleBooleanProperty(false);
    
    public static final float SIZE_FACTOR = 1f;
    public static final float RENDER_FACTOR = 3f;
    public static final String STARMATH_CHAR = "&&";
    public static final String STARMATH_CHAR_IN_LATEX = "\\&\\&";
    
    public TextElement(int x, int y, int pageNumber, boolean hasPage, String text, Color color, Font font, double maxWidth){
        super(x, y, pageNumber);
        
        this.textNode.setFont(font);
        this.textNode.setFill(color);
        this.text.set(text);
        this.maxWidth.set(maxWidth == 0 ? Main.settings.defaultMaxWidth.getValue() : maxWidth);
        
        this.textNode.setBoundsType(TextBoundsType.LOGICAL);
        this.textNode.setTextOrigin(VPos.TOP);
        
        if(hasPage && getPage() != null){
            setupGeneral(true, isMath() ? this.image : this.textNode);
            updateText();
            this.textNode.setUnderline(isURL());
        }
        
    }
    
    // SETUP / EVENT CALL BACK
    
    @Override
    protected void setupBindings(){
        
        textProperty().addListener((observable, oldValue, newValue) -> {
            updateText();
            this.textNode.setUnderline(isURL());
            
            if(isSelected() && !MainWindow.textTab.txtArea.getText().equals(newValue)){ // Edit textArea from Element
                StringUtils.editTextArea(MainWindow.textTab.txtArea, invertMathIfNeeded(newValue));
                return;
            }
            
            // New word added OR this is the first registration of this action/property.
            if(StringUtils.countSpaces(oldValue) != StringUtils.countSpaces(newValue)
                    || !MainWindow.mainScreen.isNextUndoActionProperty(textProperty())){
                MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, textProperty(), oldValue.trim(), UType.UNDO));
            }
            
        });
        this.textNode.fillProperty().addListener((observable, oldValue, newValue) -> {
            updateText();
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, this.textNode.fillProperty(), oldValue, UType.UNDO));
        });
        this.textNode.fontProperty().addListener((observable, oldValue, newValue) -> {
            updateText();
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, this.textNode.fontProperty(), oldValue, UType.UNDO));
        });
        widthProperty().addListener((observable, oldValue, newValue) -> {
            checkLocation(getLayoutX(), getLayoutY(), false);
        });
        
        isTextWrappedProperty().addListener((observable, oldValue, newValue) -> {
            setGrabLineMaxed(newValue);
        });
        textMaxWidthProperty().addListener((observable, oldValue, newValue) -> {
            updateText();
        });
    }
    
    @Override
    protected void onMouseRelease(){
        MainWindow.textTab.treeView.onFileSection.sortManager.simulateCall();
    }
    
    @Override
    protected void setupMenu(){
        
        NodeMenuItem item1 = new NodeMenuItem(TR.tr("actions.delete"), false);
        item1.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        item1.setToolTip(TR.tr("elements.delete.tooltip"));
        NodeMenuItem item2 = new NodeMenuItem(TR.tr("actions.duplicate"), false);
        item2.setToolTip(TR.tr("elements.duplicate.tooltip"));
        NodeMenuItem item3 = new NodeMenuItem(TR.tr("elementMenu.addToPreviousList"), false);
        item3.setToolTip(TR.tr("elementMenu.addToPreviousList.tooltip"));
        NodeMenuItem item4 = new NodeMenuItem(TR.tr("elementMenu.addToFavouriteList"), false);
        item4.setToolTip(TR.tr("elementMenu.addToFavouritesList.tooltip"));
        menu.getItems().addAll(item1, item2, item4, item3);
        NodeMenuItem.setupMenu(menu);
        
        item1.setOnAction(e -> delete(true, UType.UNDO));
        item2.setOnAction(e -> cloneOnDocument());
        item3.setOnAction(e -> TextTreeView.addSavedElement(this.toNoDisplayTextElement(TextTreeSection.LAST_TYPE, true)));
        item4.setOnAction(e -> TextTreeView.addSavedElement(this.toNoDisplayTextElement(TextTreeSection.FAVORITE_TYPE, true)));
    }
    
    private void updateGrabIndicator(boolean selected){
        if(selected && !isMath()){
            if(getChildren().stream().noneMatch((n) -> n instanceof GrabLine))
                getChildren().add(new GrabLine(this, isIsTextWrapped()));
        }else{
            getChildren().removeIf((n) -> n instanceof GrabLine);
        }
        
    }
    
    // ACTIONS
    
    @Override
    public void select(){
        super.selectPartial();
        SideBar.selectTab(MainWindow.textTab);
        MainWindow.textTab.selectItem();
        AutoTipsManager.showByAction("textselect");
    }
    @Override
    public void onDoubleClickAfterSelected(){
        cloneOnDocument();
    }
    @Override
    public void onDoubleClick(){
    
    }
    @Override
    protected void onSelected(){
        super.onSelected();
        updateGrabIndicator(true);
        
        // Return value is ignored, this is only used to check if text is wrapped (update GrabLine color)
        getWrappedTextFromMaxWidth();
    }
    @Override
    protected void onDeSelected(){
        super.onDeSelected();
        updateGrabIndicator(false);
    }
    @Override
    public void addedToDocument(boolean markAsUnsave){
        if(markAsUnsave) MainWindow.textTab.treeView.onFileSection.addElement(this);
    }
    
    @Override
    public void removedFromDocument(boolean markAsUnsave){
        super.removedFromDocument(markAsUnsave);
        if(markAsUnsave) MainWindow.textTab.treeView.onFileSection.removeElement(this);
    }
    
    // READER AND WRITERS
    
    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("color", textNode.getFill().toString());
        data.put("font", textNode.getFont().getFamily());
        data.put("size", textNode.getFont().getSize());
        data.put("bold", FontUtils.getFontWeight(textNode.getFont()) == FontWeight.BOLD);
        data.put("italic", FontUtils.getFontPosture(textNode.getFont()) == FontPosture.ITALIC);
        data.put("text", getText());
        data.put("maxWidth", maxWidth.get());
        
        return data;
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data, int page, boolean upscaleGrid){
        TextElement element = readYAMLDataAndGive(data, true, page, upscaleGrid);
        
        if(MainWindow.mainScreen.document.getPagesNumber() > element.getPageNumber())
            MainWindow.mainScreen.document.getPage(element.getPageNumber()).addElement(element, false, UType.NO_UNDO);
    }
    
    public static TextElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage, int page, boolean upscaleGrid){
        
        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        double fontSize = Config.getDouble(data, "size");
        boolean isBold = Config.getBoolean(data, "bold");
        boolean isItalic = Config.getBoolean(data, "italic");
        String fontName = Config.getString(data, "font");
        Color color = Color.valueOf(Config.getString(data, "color"));
        String text = Config.getString(data, "text");
        double maxWidth = Config.getDouble(data, "maxWidth");
        
        Font font = FontUtils.getFont(fontName, isItalic, isBold, (int) fontSize);
        
        if(upscaleGrid){ // Between 1.2.1 and 1.3.0, the grid size was multiplied by 100
            x *= 100;
            y *= 100;
        }
        
        return new TextElement(x, y, page, hasPage, text, color, font, maxWidth);
    }
    
    // SPECIFIC METHODS
    
    public float getBaseLineY(){
        return (float) (textNode.getBaselineOffset());
    }
    
    @Override
    public float getBoundsHeight(){
        return (float) textNode.getLayoutBounds().getHeight();
    }
    
    public float getBoundsWidth(){
        return (float) textNode.getLayoutBounds().getWidth();
    }
    
    public boolean isURL(){
        return getText().startsWith("http://") || getText().startsWith("https://") || getText().startsWith("www.");
    }
    
    public boolean isMath(){
        return isMath(getText());
    }
    public static boolean isMath(@NotNull String text){
        return text.startsWith("$$") || text.startsWith(STARMATH_CHAR) ||
                text.split(Pattern.quote("$$")).length > 1 || text.split(Pattern.quote(STARMATH_CHAR)).length > 1;
    }
    
    
    // Invert only if settings' defaultLatex is true
    public static @NotNull String invertMathIfNeeded(@NotNull String value){
        if(Main.settings.defaultTextMode.getValue() == Settings.TEXT_MODE_LATEX){
            return invertLatex(value);
        }else if(Main.settings.defaultTextMode.getValue() == Settings.TEXT_MODE_STARMATH){
            return invertStarMath(value);
        }
        return value;
    }
    public static @NotNull String invertLatex(@NotNull String value){
        if(value.startsWith("$$")) return value.substring(2);
        return "$$" + value;
    }
    public static @NotNull String invertStarMath(@NotNull String value){
        if(value.startsWith(STARMATH_CHAR)) return value.substring(2);
        return STARMATH_CHAR + value;
    }
    public static String invertBySettings(String text, int defaultTextMode){
        if(defaultTextMode == Settings.TEXT_MODE_LATEX){
            return invertLatex(text);
        }else if(defaultTextMode == Settings.TEXT_MODE_STARMATH){
            return invertStarMath(text);
        }else{
            return text;
        }
    }
    
    public String getLaTeXText(){
        
        String text = getText();
        StringBuilder output = new StringBuilder();
        
        boolean isLatex = text.startsWith("$$");
        boolean isStarmath = text.startsWith(STARMATH_CHAR);
        if(isLatex || isStarmath) text = text.substring(2);
        
        for(String latexPart : text.split(Pattern.quote("$$"))){
            if(isLatex) latexPart = latexPart.replace(STARMATH_CHAR, STARMATH_CHAR_IN_LATEX);
            
            String[] latexPartSplit = latexPart.split(Pattern.quote(STARMATH_CHAR));
            for(int i = 0; i < latexPartSplit.length; i++){
                String part = latexPartSplit[i];
                
                if(isLatex){
                    output.append(part.replace("\n", " \\\\ "));
                }else if(isStarmath){
                    output.append(translateStarMathToLatex(part));
                }else{
                    output.append(formatLatexText(part));
                }
                
                // Invert only if this is not the end of the for loop,
                // if there is a change of starMath and not of LaTeX
                if(i != latexPartSplit.length - 1){
                    isStarmath = !isStarmath;
                }
                
            }
            // If in starmath, do not invert
            if(isStarmath && !isLatex) output.append("$$");
            else isLatex = !isLatex;
        }
        
        return output.toString();
    }
    
    private static String formatLatexText(String text){
        return "\\textsf{" + text.replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("$", "\\$")
                .replace("%", "\\%")
                .replace("^", "\\^")
                .replace("&", "\\&")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("\n", "} \\\\ \\textsf{") + "}";
    }
    
    public java.awt.Color getAwtColor(){
        return new java.awt.Color((float) getColor().getRed(),
                (float) getColor().getGreen(),
                (float) getColor().getBlue(),
                (float) getColor().getOpacity());
    }
    
    public void updateText(){
        if(isMath()){ // LaTeX
            
            updateGrabIndicator(false);
            if(getChildren().contains(textNode)){ // Remove plain text
                getChildren().remove(textNode);
                getChildren().add(image);
            }
            renderLatex((render) -> {
                Platform.runLater(() -> {
                    image.setImage(render);
                    image.setVisible(true);
                    image.setFitWidth(render.getWidth() / RENDER_FACTOR);
                    image.setFitHeight(render.getHeight() / RENDER_FACTOR);
                });
            });
            
        }else{ // Lambda Text
            
            updateGrabIndicator(isSelected());
            textNode.setText(getWrappedTextFromMaxWidth());
            textNode.setVisible(true);
            
            if(getChildren().contains(image)){ // Remove image
                getChildren().remove(image);
                getChildren().add(textNode);
                image.setImage(null);
            }
        }
    }
    
    public String getWrappedTextFromMaxWidth(){
        int maxWidth = (int) (PageRenderer.PAGE_WIDTH * (getTextMaxWidth() / 100d));
        
        if(getText() == null){
            setGrabLineMaxed(false);
            isTextWrapped.set(false);
            return "";
        }
        
        TextWrapper wrapper = new TextWrapper(getText(), getFont(), maxWidth);
        
        String wrapped = wrapper.wrap();
        isTextWrapped.set(wrapper.doHasWrapped());
        
        return wrapped;
    }
    
    private void setGrabLineMaxed(boolean maxed){
        getChildren().forEach((n) -> {
            if(n instanceof GrabLine grabLine){
                grabLine.setMaxed(maxed);
            }
        });
    }
    
    public void renderLatex(CallBackArg<Image> callback){
        new Thread(() -> {
            BufferedImage render = renderAwtLatex();
            callback.call(SwingFXUtils.toFXImage(render, new WritableImage(render.getWidth(null), render.getHeight(null))));
        }, "LaTeX rendered").start();
    }
    
    public BufferedImage renderAwtLatex(){
        int style = 0;
        
        if(FontUtils.getFontWeight(getFont()) == FontWeight.BOLD) style = style | TeXFormula.BOLD;
        if(FontUtils.getFontPosture(getFont()) == FontPosture.ITALIC) style = style | TeXFormula.ITALIC;
        
        return renderLatex(getLaTeXText(), getAwtColor(), getFont().getFamily(), style, (int) getFont().getSize(), 0);
    }
    
    public static BufferedImage renderLatex(String text, java.awt.Color color, String family, int type, int size, int calls){
        
        try{
            
            //TeXFormula.registerExternalFont(Character.UnicodeBlock.BASIC_LATIN, family);
            TeXFormula formula = new TeXFormula(text);
            
            formula.setColor(color);
            
            int finalSize = (int) (size * RENDER_FACTOR * SIZE_FACTOR);
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, finalSize, type);
            
            icon.setInsets(new Insets(-finalSize / 7, -finalSize / 7, -finalSize / 7, -finalSize / 7));
            
            BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setBackground(new java.awt.Color(0f, 0f, 0f, 1f));
            icon.paintIcon(null, g, 0, 0);
            
            return image;
            
        }catch(ParseException ex){
            if(Main.DEBUG) System.out.println("error rendering Latex");
            if(calls >= 3){
                ex.printStackTrace();
                return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            }
            if(ex.getMessage().contains("Unknown symbol or command or predefined TeXFormula: ")){
                return renderLatex(formatLatexText(TR.tr("textTab.Latex.unknownError") + "\\" +
                        ex.getMessage().replaceAll(Pattern.quote("Unknown symbol or command or predefined TeXFormula:"), "")), color, family, type, size, calls + 1);
            }else if(text.startsWith(TR.tr("dialog.error.presentative") + "\\")){
                return renderLatex(formatLatexText(TR.tr("textTab.Latex.unableToParse")), color, family, type, size, calls + 1);
            }else{
                return renderLatex(formatLatexText(TR.tr("dialog.error.presentative") + "\\" + ex.getMessage()), color, family, type, size, calls + 1);
            }
        }
    }
    
    private static String translateStarMathToLatex(String starMath){
        
        StarMathConverter converter = ConverterFactory.createStarMathConverter();
        StringBuilder output = new StringBuilder();
        
        int i = 0;
        for(String part : starMath.split(Pattern.quote("\n"))){
            if(i != 0) output.append(" \\\\ ");
            output.append(converter.convert(part));
            i++;
        }
        return output.toString();
    }
    
    // ELEMENT DATA GETTERS AND SETTERS
    
    @Override
    public String getElementName(boolean plural){
        return getElementNameStatic(plural);
    }
    public static String getElementNameStatic(boolean plural){
        if(plural) return TR.tr("elements.name.texts");
        else return TR.tr("elements.name.text");
    }
    
    public String getText(){
        return text.get();
    }
    public String getTextNodeText(){
        return textNode.getText();
    }
    public boolean hasEmptyText(){
        String text = invertMathIfNeeded(getText());
        return text.isBlank();
    }
    
    public StringProperty textProperty(){
        return text;
    }
    public void setText(String text){
        this.text.set(text);
    }
    public void setColor(Color color){
        this.textNode.setFill(color);
    }
    public ObjectProperty<Paint> fillProperty(){
        return textNode.fillProperty();
    }
    public Color getColor(){
        return (Color) textNode.getFill();
    }
    public void setFont(Font font){
        textNode.setFont(font);
    }
    public ObjectProperty<Font> fontProperty(){
        return textNode.fontProperty();
    }
    public Font getFont(){
        return textNode.getFont();
    }
    public double getTextMaxWidth(){
        return maxWidth.get();
    }
    public DoubleProperty textMaxWidthProperty(){
        return maxWidth;
    }
    public void setTextMaxWidth(double maxWidth){
        this.maxWidth.set(StringUtils.clamp(maxWidth, 1, 100));
    }
    public boolean isIsTextWrapped(){
        return isTextWrapped.get();
    }
    public BooleanProperty isTextWrappedProperty(){
        return isTextWrapped;
    }
    public void setIsTextWrapped(boolean isTextWrapped){
        this.isTextWrapped.set(isTextWrapped);
    }
    // TRANSFORMATIONS
    
    @Override
    public Element clone(){
        AutoTipsManager.showByAction("textclone");
        return new TextElement(getRealX(), getRealY(), pageNumber, true, getText(), (Color) textNode.getFill(), textNode.getFont(), getTextMaxWidth());
    }
    
    public TextTreeItem toNoDisplayTextElement(int type, boolean hasCore){
        if(hasCore)
            return new TextTreeItem(textNode.getFont(), getText(), (Color) textNode.getFill(), getTextMaxWidth(), type, 0, System.currentTimeMillis() / 1000, this);
        else
            return new TextTreeItem(textNode.getFont(), getText(), (Color) textNode.getFill(), getTextMaxWidth(), type, 0, System.currentTimeMillis() / 1000);
    }
    
}