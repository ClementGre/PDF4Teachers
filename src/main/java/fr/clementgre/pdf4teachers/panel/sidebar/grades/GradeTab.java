/*
 * Copyright (c) 2020-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.components.IconButton;
import fr.clementgre.pdf4teachers.components.IconToggleButton;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.export.GradeExportWindow;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;

@SuppressWarnings("serial")
public class GradeTab extends SideTab {
    
    public VBox pane = new VBox();
    public HBox optionPane = new HBox();
    
    public GradeTreeView treeView;
    
    public static HashMap<Integer, TiersFont> fontTiers = new HashMap<>();
    
    public ToggleButton lockGradeScale = new IconToggleButton(SVGPathIcons.LOCK, SVGPathIcons.LOCK_OPEN, TR.tr("gradeTab.lockGradeScale.tooltip"), null, true);
    private final Button settings = new IconButton(SVGPathIcons.GEAR, TR.tr("gradeTab.gradeFormatWindow.accessButton.tooltip"), e -> new GradeSettingsWindow(), true);
    private final Button link = new IconButton(SVGPathIcons.LINK, TR.tr("gradeTab.copyGradeScaleDialog.accessButton.tooltip"), e -> new GradeCopyGradeScaleDialog().show(), true);
    private final Button export = new IconButton(SVGPathIcons.EXPORT, TR.tr("gradeTab.gradeExportWindow.accessButton"), e -> {
        if(MainWindow.mainScreen.hasDocument(false) && MainWindow.mainScreen.document.save(true)) new GradeExportWindow();
    }, true);
    
    public GradeTab(){
        super("grades", SVGPathIcons.ON_TWENTY, 29, 500/440d);
        
        setContent(pane);
        setup();
    }
    
    public void setup(){
        
        fontTiers.put(0, new TiersFont(FontUtils.getFont("Open Sans", false, false, 28), Color.valueOf("#990000"), true, false, false));
        fontTiers.put(1, new TiersFont(FontUtils.getFont("Open Sans", false, false, 24), Color.valueOf("#b31a1a"), false, false, false));
        fontTiers.put(2, new TiersFont(FontUtils.getFont("Open Sans", false, false, 18), Color.valueOf("#cc3333"), false, false, false));
        fontTiers.put(3, new TiersFont(FontUtils.getFont("Open Sans", false, false, 18), Color.valueOf("#e64d4d"), false, false, false));
        fontTiers.put(4, new TiersFont(FontUtils.getFont("Open Sans", false, false, 18), Color.valueOf("#ff6666"), false, false, false));
        
        lockGradeScale.setSelected(false);
        lockGradeScale.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if(newValue) AutoTipsManager.showByAction("gradescalelock");
            // Update the selected cell
            if(treeView.getSelectionModel().getSelectedItem() != null){
                int selected = treeView.getSelectionModel().getSelectedIndex();
                treeView.getSelectionModel().select(null);
                treeView.getSelectionModel().select(selected);
            }
        });
        
        optionPane.setPadding(new Insets(5, 0, 5, 0));
        optionPane.getChildren().addAll(new HBoxSpacer(), lockGradeScale, settings, link, export);
        
        treeView = new GradeTreeView(this);
        pane.getChildren().addAll(optionPane, treeView);
        
    }
    
    public GradeElement newGradeElementAuto(GradeTreeItem parent){
        
        PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
        
        MainWindow.mainScreen.setSelected(null);
        
        String name = TR.tr("gradeTab.gradeDefaultName");
        if(!parent.getChildren().isEmpty()){
            String lastName = ((GradeTreeItem) parent.getChildren().get(parent.getChildren().size() - 1)).getCore().getName();
            String newName = StringUtils.incrementName(lastName);
            if(!lastName.equals(newName)) name = newName;
        }
        
        GradeElement current = new GradeElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(),
                true, -1, 0, -1, parent.getChildren().size(), GradeTreeView.getElementPath(parent), name, false);
        
        page.addElement(current, true, UType.ELEMENT);
        current.centerOnCoordinatesY();
        MainWindow.mainScreen.setSelected(current);
        
        return current;
    }
    
    public void newGradeElement(String name, double value, double total, int index, String parentPath, boolean update){
        PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
        if(page == null) return;
        
        if(update) MainWindow.mainScreen.setSelected(null);
        
        GradeElement current = new GradeElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(),
                true, value, total, -1, index, parentPath, name, false);
        
        page.addElement(current, update, UType.NO_UNDO);
        current.centerOnCoordinatesY();
        if(update) MainWindow.mainScreen.setSelected(current);
        
    }
    
    public void updateElementsFont(){
        if(treeView.getRoot() != null){
            GradeTreeItem root = ((GradeTreeItem) treeView.getRoot());
            if(root.hasSubGrade()) updateElementFont(root);
            root.getCore().updateFont();
        }
    }
    
    private void updateElementFont(GradeTreeItem parent){
        
        for(int i = 0; i < parent.getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) parent.getChildren().get(i);
            
            children.getCore().updateFont();
            if(children.hasSubGrade()) updateElementFont(children);
        }
    }
    
    public static Font getTierFont(int index){
        return fontTiers.get(index).getFont();
    }
    
    public static Color getTierColor(int index){
        return fontTiers.get(index).getColor();
    }
    
    public static boolean getTierShowName(int index){
        return fontTiers.get(index).isShowName();
    }
    
    public static boolean getTierHide(int index){
        return fontTiers.get(index).isHide();
    }
    
    public static boolean getTierHideWhenAllPoints(int index){
        return fontTiers.get(index).isHideWhenAllPoints();
    }
    
    public BooleanProperty isLockGradeScaleProperty(){
        return lockGradeScale.selectedProperty();
    }
}
