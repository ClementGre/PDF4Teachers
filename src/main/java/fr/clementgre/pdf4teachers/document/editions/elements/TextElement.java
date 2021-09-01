/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.ObservableChangedUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoEngine;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
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
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class TextElement extends Element {
    
    private final ScratchText text = new ScratchText();
    private final ImageView image = new ImageView();
    
    public static final float imageFactor = 3f;
    
    public TextElement(int x, int y, int pageNumber, boolean hasPage, String text, Color color, Font font){
        super(x, y, pageNumber);
        
        this.text.setFont(font);
        this.text.setFill(color);
        this.text.setText(text);
        
        this.text.setBoundsType(TextBoundsType.LOGICAL);
        this.text.setTextOrigin(VPos.TOP);
        
        if(hasPage && getPage() != null){
            setupGeneral(true, isLatex() ? this.image : this.text);
            updateLaTeX();
            this.text.setUnderline(isURL());
        }
        
    }
    
    // SETUP / EVENT CALL BACK
    
    @Override
    protected void setupBindings(){
        this.text.textProperty().addListener((observable, oldValue, newValue) -> {
            updateLaTeX();
            this.text.setUnderline(isURL());
            
            if(isSelected() && !MainWindow.textTab.txtArea.getText().equals(newValue)){ // Edit textArea from Element
                StringUtils.editTextArea(MainWindow.textTab.txtArea, newValue);
                return;
            }
            
            // New word added OR this is the first registration of this action/property.
            if(StringUtils.countSpaces(oldValue) != StringUtils.countSpaces(newValue)
                    || !UndoEngine.isNextUndoActionProperty(this.text.textProperty())){
                MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, this.text.textProperty(), oldValue.trim(), UType.UNDO));
            }
            
        });
        this.text.fillProperty().addListener((observable, oldValue, newValue) -> {
            updateLaTeX();
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, this.text.fillProperty(), oldValue, UType.UNDO));
        });
        this.text.fontProperty().addListener((observable, oldValue, newValue) -> {
            updateLaTeX();
            MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(this, this.text.fontProperty(), oldValue, UType.UNDO));
        });
        widthProperty().addListener((observable, oldValue, newValue) -> {
            checkLocation(getLayoutX(), getLayoutY(), false);
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
        data.put("color", text.getFill().toString());
        data.put("font", text.getFont().getFamily());
        data.put("size", text.getFont().getSize());
        data.put("bold", FontUtils.getFontWeight(text.getFont()) == FontWeight.BOLD);
        data.put("italic", FontUtils.getFontPosture(text.getFont()) == FontPosture.ITALIC);
        data.put("text", text.getText());
        
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
        
        Font font = FontUtils.getFont(fontName, isItalic, isBold, (int) fontSize);
        
        if(upscaleGrid){ // Between 1.2.1 and 1.3.0, the grid size was multiplied by 100
            x *= 100;
            y *= 100;
        }
        
        return new TextElement(x, y, page, hasPage, text, color, font);
    }
    
    // SPECIFIC METHODS
    
    public float getBaseLineY(){
        return (float) (text.getBaselineOffset());
    }
    
    @Override
    public float getBoundsHeight(){
        return (float) text.getLayoutBounds().getHeight();
    }
    
    public float getBoundsWidth(){
        return (float) text.getLayoutBounds().getWidth();
    }
    
    public boolean isURL(){
        return text.getText().startsWith("http://") || text.getText().startsWith("https://") || text.getText().startsWith("www.");
    }
    
    public boolean isLatex(){
        return isLatex(text.getText());
    }
    
    public static boolean isLatex(String text){
        return text.split(Pattern.quote("$$")).length > 1;
    }
    
    public String getLaTeXText(){
        
        String latexText = "";
        boolean isText = !text.getText().startsWith(Pattern.quote("$$"));
        for(String part : text.getText().split(Pattern.quote("$$"))){
            
            if(isText) latexText += formatLatexText(part);
            else latexText += part.replace("\n", " \\\\ ");
            
            isText = !isText;
        }
        return latexText;
    }
    
    public static String formatLatexText(String text){
        return "\\text{" + text.replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("$", "\\$")
                .replace("%", "\\%")
                .replace("^", "\\^")
                .replace("&", "\\&")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("\n", "} \\\\ \\text{") + "}";
    }
    
    public java.awt.Color getAwtColor(){
        return new java.awt.Color((float) getColor().getRed(),
                (float) getColor().getGreen(),
                (float) getColor().getBlue(),
                (float) getColor().getOpacity());
    }
    
    public void updateLaTeX(){
        if(isLatex()){ // LaTeX
            
            if(getChildren().contains(text)){
                getChildren().remove(text);
                getChildren().add(image);
            }
            renderLatex((render) -> {
                Platform.runLater(() -> {
                    image.setImage(render);
                    image.setVisible(true);
                    image.setFitWidth(render.getWidth() / imageFactor);
                    image.setFitHeight(render.getHeight() / imageFactor);
                });
            });
            
        }else{ // Lambda Text
            
            text.setVisible(true);
            if(getChildren().contains(image)){
                getChildren().remove(image);
                getChildren().add(text);
                image.setImage(null);
            }
        }
    }
    
    public void renderLatex(CallBackArg<Image> callback){
        new Thread(() -> {
            BufferedImage render = renderAwtLatex();
            callback.call(SwingFXUtils.toFXImage(render, new WritableImage(render.getWidth(null), render.getHeight(null))));
        }, "LaTeX rendered").start();
    }
    
    public BufferedImage renderAwtLatex(){
        return renderLatex(getLaTeXText(), getAwtColor(), (int) getFont().getSize(), 0);
    }
    
    public static BufferedImage renderLatex(String text, java.awt.Color color, int size, int calls){
        
        try{
            TeXFormula formula = new TeXFormula(text);
            formula.setColor(color);
            
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_TEXT, size * imageFactor);
            
            icon.setInsets(new Insets((int) (-size * imageFactor / 7), (int) (-size * imageFactor / 7), (int) (-size * imageFactor / 7), (int) (-size * imageFactor / 7)));
            
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
                        ex.getMessage().replaceAll(Pattern.quote("Unknown symbol or command or predefined TeXFormula:"), "")), color, size, calls + 1);
            }else if(text.startsWith(TR.tr("dialog.error.presentative") + "\\")){
                return renderLatex(formatLatexText(TR.tr("textTab.Latex.unableToParse")), color, size, calls + 1);
            }else{
                return renderLatex(formatLatexText(TR.tr("dialog.error.presentative") + "\\" + ex.getMessage()), color, size, calls + 1);
            }
        }
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
        return text.getText();
    }
    
    public StringProperty textProperty(){
        return text.textProperty();
    }
    
    public void setText(String text){
        this.text.setText(text);
    }
    
    public void setColor(Color color){
        this.text.setFill(color);
    }
    
    public ObjectProperty<Paint> fillProperty(){
        return text.fillProperty();
    }
    
    public Color getColor(){
        return (Color) text.getFill();
    }
    
    public void setFont(Font font){
        text.setFont(font);
    }
    
    public ObjectProperty<Font> fontProperty(){
        return text.fontProperty();
    }
    
    public Font getFont(){
        return text.getFont();
    }
    
    // TRANSFORMATIONS
    
    @Override
    public Element clone(){
        AutoTipsManager.showByAction("textclone");
        return new TextElement(getRealX(), getRealY(), pageNumber, true, text.getText(), (Color) text.getFill(), text.getFont());
    }
    
    public TextTreeItem toNoDisplayTextElement(int type, boolean hasCore){
        if(hasCore)
            return new TextTreeItem(text.getFont(), text.getText(), (Color) text.getFill(), type, 0, System.currentTimeMillis() / 1000, this);
        else
            return new TextTreeItem(text.getFont(), text.getText(), (Color) text.getFill(), type, 0, System.currentTimeMillis() / 1000);
    }
    
}