/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SkillTableElement extends GraphicElement{
    
    private final LongProperty assessmentId = new SimpleLongProperty();
    private final LongProperty studentId = new SimpleLongProperty();
    private final ListProperty<EditionSkill> editionSkills = new SimpleListProperty<>();
    
    
    private final GridPane gridPane = new GridPane();
    
    public SkillTableElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, long assessmentId, long studentId, ArrayList<EditionSkill> editionSkills){
        super(x, y, pageNumber, width, height, RepeatMode.KEEP_RATIO, ResizeMode.CORNERS);
        this.assessmentId.set(assessmentId);
        this.studentId.set(studentId);
        this.editionSkills.set(FXCollections.observableList(editionSkills));
    
        //super.allowShiftToInvertResizeMode = false;
        
        if(hasPage && getPage() != null) setupPage();
    }
    
    @Override
    public void initializePage(int pageNumber, double x, double y){
        this.pageNumber = pageNumber;
        setupPage();
        checkLocation(x, y, false);
    }
    
    private void setupPage(){
        
        visibleProperty().bind(editionSkillsProperty().emptyProperty().not());
        updateLayout();
        editionSkills.addListener((observable, oldValue, newValue) -> updateLayout());
        
        gridPane.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        gridPane.setMouseTransparent(true);
        gridPane.setGridLinesVisible(true);
        
        
        if(getRealWidth() == 0 && getRealHeight() == 0){
            System.out.println("Defining default size");
            setRealWidth(30000);
        }
        
        
    
        // Make sure the ratio is always respected & update the gridPane scale
        heightProperty().addListener((observable) -> updateDimensionsToMatchRatio());
        widthProperty().addListener((observable) -> updateDimensionsToMatchRatio());
    
        // Update element height when gridPane size changes.
        gridPane.widthProperty().addListener((observable) -> updateDimensionsExtendingHeight());
        gridPane.heightProperty().addListener((observable) -> updateDimensionsExtendingHeight());
        
        
        setupGeneral(gridPane);
    
        Platform.runLater(this::updateDimensionsToMatchRatio);
    }
    private boolean isEditingDimensions = false;
    private void updateDimensionsToMatchRatio(){
        if(isEditingDimensions) return;
        if(getWidth() == 0 || getHeight() == 0 || Double.isNaN(getRatio()) || Double.isInfinite(getRatio())) return;
        isEditingDimensions = true;
        
        
        if((int) ((getWidth()/getHeight()) * 1000) != (int) (getRatio() * 1000)){ // 3 decimal precision equality
            System.out.println("updateDimensionsToMatchRatio(): " + getWidth()/getHeight() + " != " + getRatio());
            
            if(getWidth()/getHeight() >= getRatio()){
                checkLocation(getLayoutX(), getLayoutY(),  getHeightFromRealHeight() * getRatio(),  getHeightFromRealHeight(), false);
            }else{
                checkLocation(getLayoutX(), getLayoutY(), getWidthFromRealWidth(), getWidthFromRealWidth() / getRatio(), false);
            }
        }
        
        updateGridPaneScale();
        
        isEditingDimensions = false;
    }
    private void updateDimensionsExtendingHeight(){
        if(isEditingDimensions) return;
        if(getWidth() == 0 || getHeight() == 0 || Double.isNaN(getRatio()) || Double.isInfinite(getRatio())) return;
        System.out.println("updateDimensionsExtendingHeight(), ratio = " + getRatio() + " (" + gridPane.getWidth() + " * " + gridPane.getHeight() + ")");
    
        isEditingDimensions = true;
        checkLocation(getLayoutX(), getLayoutY(), getWidthFromRealWidth(), getWidthFromRealWidth() / getRatio(), false);
        getPage().layout(); // Required to update the visual bounds of the element
        isEditingDimensions = false;
        
        // Update in case the dimensions are higher than the page.
        updateDimensionsToMatchRatio();
    }
    public double getWidthFromRealWidth(){
        return getPage().fromGridX(getRealWidth());
    }
    public double getHeightFromRealHeight(){
        return getPage().fromGridY(getRealHeight());
    }
    
    private void updateGridPaneScale(){
        double scale = getWidth() / gridPane.getWidth();
        gridPane.setScaleX(scale);
        gridPane.setScaleY(scale);
        gridPane.setTranslateX(- (gridPane.getWidth()  - gridPane.getWidth() * scale) / 2);
        gridPane.setTranslateY(- (gridPane.getHeight() - gridPane.getHeight() * scale) / 2);
    }
    
    public void updateLayout(){
        
        gridPane.getChildren().clear();
        SkillsAssessment assessment = MainWindow.skillsTab.getCurrentAssessment();
        
        
        gridPane.addRow(0, new Label("Compétences"), new Label("Évaluation"));
        if(assessment == null) return;
        AtomicInteger i = new AtomicInteger();
        editionSkills.forEach(editionSkill -> {
            Skill skill = editionSkill.getMatchingSkill(assessment);
            Notation notation = editionSkill.getMatchingNotation(assessment);
            if(skill != null && notation != null){
                gridPane.addRow(i.incrementAndGet(), new Label(skill.getAcronym() + "\n" + skill.getName()), new Label(notation.getName()));
            }
        });
    }
    
    @Override
    public void addedToDocument(boolean markAsUnsave){
        MainWindow.skillsTab.registerSkillTableElement(this);
    }
    @Override
    public void removedFromDocument(boolean markAsUnsave){
        super.removedFromDocument(markAsUnsave);
    }
    @Override
    public void restoredToDocument(){
        super.restoredToDocument();
    }
    
    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("page", pageNumber);
        data.put("assessmentId", assessmentId.get());
        data.put("studentId", studentId.get());
        // Useless to save editionSkills that have no matching Notation (id == 0)
        data.put("list", editionSkills.get().stream().filter(s -> s.getNotationId() != 0).map(EditionSkill::toYAML).toList());
        return data;
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data){
        SkillTableElement element = readYAMLDataAndGive(data, true);
        if(MainWindow.mainScreen.document.getPagesNumber() > element.getPageNumber())
            MainWindow.mainScreen.document.getPage(element.getPageNumber()).addElement(element, false, UType.NO_UNDO);
    }
    
    public static SkillTableElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage){
        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        
        int page = (int)  Config.getLong(data, "page");
        
        long assessmentId = Config.getLong(data, "assessmentId");
        long studentId = Config.getLong(data, "studentId");
    
        ArrayList<EditionSkill> skills = new ArrayList<>();
        for(Object skillData : Config.getList(data, "list")){
            if(skillData instanceof Map) skills.add(EditionSkill.getFromYAML((HashMap<String, Object>) skillData));
        }
        
        return new SkillTableElement(x, y, page, hasPage, width, height, assessmentId, studentId, skills);
    }
    
    
    
    @Override
    public String getElementName(boolean plural){
        return null;
    }
    
    @Override
    public Element clone(){
        throw new RuntimeException("SkillTableElement can't be cloned.");
    }
    
    @Override
    protected void onMouseRelease(){
        super.onMouseRelease();
    }
    @Override
    public void onDoubleClick(){
    
    }
    
    @Override
    public void defineSizeAuto(){
    }
    
    @Override
    public void incrementUsesAndLastUse(){
    }
    
    @Override
    public double getRatio(){
        return gridPane.getWidth() / gridPane.getHeight();
    }
    
    public long getAssessmentId(){
        return assessmentId.get();
    }
    public LongProperty assessmentIdProperty(){
        return assessmentId;
    }
    public void setAssessmentId(long assessmentId){
        this.assessmentId.set(assessmentId);
    }
    public long getStudentId(){
        return studentId.get();
    }
    public LongProperty studentIdProperty(){
        return studentId;
    }
    public void setStudentId(long studentId){
        this.studentId.set(studentId);
    }
    public ObservableList<EditionSkill> getEditionSkills(){
        return editionSkills.get();
    }
    public ListProperty<EditionSkill> editionSkillsProperty(){
        return editionSkills;
    }
    public void setEditionSkills(ObservableList<EditionSkill> editionSkills){
        this.editionSkills.set(editionSkills);
    }
}
