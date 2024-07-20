/*
 * Copyright (c) 2022-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.FooterBar;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Student;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallsBuffer;
import fr.clementgre.pdf4teachers.utils.interfaces.CallsBufferMemory;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkillTableElement extends GraphicElement{
    
    private final LongProperty assessmentId = new SimpleLongProperty();
    private final LongProperty studentId = new SimpleLongProperty();
    
    public double defaultScale = .8;
    private static final double MAX_SCALE = 1.4;
    private static final double MIN_SCALE = 0.4;
    private double scale;
    
    
    private final ListProperty<EditionSkill> editionSkills = new SimpleListProperty<>();
    private final SkillTableGridPane gridPane = new SkillTableGridPane(this);
    
    public SkillTableElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, double scale, long assessmentId, long studentId, ArrayList<EditionSkill> editionSkills){
        super(x, y, pageNumber, width, height, RepeatMode.STRETCH, ResizeMode.CORNERS);
        this.scale = scale; // scale is just a save of the last scale used for this element. It is used in the export process.
        this.assessmentId.set(assessmentId);
        this.studentId.set(studentId);
        this.editionSkills.set(FXCollections.observableList(editionSkills));
        
        if(hasPage && getPage() != null) setupPage();
    }
    
    @Override
    public void initializePage(int pageNumber, double x, double y){
        this.pageNumber = pageNumber;
        setupPage();
        checkLocation(x, y, false);
    }
    
    private void setupPage(){
        
        editionSkills.addListener((observable, oldValue, newValue) -> updateSkillsNotation());
        assessmentId.addListener((observable, oldValue, newValue) -> updateLayout());
    
        setupGeneral(gridPane);
        
        if(getRealWidth() == 0 && getRealHeight() == 0){
            // Default size
            SkillsAssessment assessment = MainWindow.skillsTab.getAssessments().stream().filter(a -> a.getId() == assessmentId.get()).findFirst().orElse(null);
            
            if(assessment != null && getPageNumber() != assessment.getPrefPage()){
                int page = Math.min(MainWindow.mainScreen.document.numberOfPages - 1, assessment.getPrefPage());
                switchPage(page);
                Platform.runLater(() -> switchPage(page));
            }
            int realX = assessment == null || assessment.getPrefRealX() < 0 ? 22700 : assessment.getPrefRealX();
            int realY = assessment == null || assessment.getPrefRealY() < 0 ? 22700 : assessment.getPrefRealY();
            int realWidth = assessment == null || assessment.getPrefWidth() < 0 ? 120000 : assessment.getPrefWidth();
            
            checkLocation(getPage().fromGridX(realX), getPage().fromGridY(realY), getPage().fromGridX(Math.max(6000, realWidth)), 0, false);
            
            if(getPage() != MainWindow.mainScreen.document.getFirstTopVisiblePage()){
                MainWindow.mainScreen.zoomOperator.scrollToPage(getPage());
            }
            
            if(assessment != null && assessment.getPrefScale() > 0){
                defaultScale = assessment.getPrefScale();
                Platform.runLater(() -> updateGridPaneScale(defaultScale));
            }
            else Platform.runLater(this::updateGridPaneScale);
        }else{
            Platform.runLater(this::updateGridPaneScale);
        }
    
        // Update the gridPane scale
        widthProperty().addListener((observable) -> updateGridPaneScale());
        heightProperty().addListener((observable) -> updateGridPaneScale());
        
    
        // Update element height when gridPane height changes. (Adding new skill or new row)
        gridPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            
            double newHeight;
            double newLayoutY = getLayoutY();
            if(getRealHeight() == 0){ // Height undefined => Default height
                newHeight = newValue.doubleValue() * defaultScale;
                updateGridPaneScale(defaultScale);
                gridPane.areDimensionsSetup = true;
            }else{
                if(newValue.doubleValue() == 0){
                    // Grid is now empty => Set the element's height to 0 so it can then be auto defined.
                    newHeight = 0;
                }else if(getHeight() == 0 || oldValue.doubleValue() == 0 || Double.isNaN(getRatio()) || Double.isInfinite(getRatio())){
                    // Values are not defined => keep the current height.
                    return;
                }else{
                    // Values need one more step to fully setting up the element's height.
                    if(!gridPane.areDimensionsSetup){
                        gridPane.areDimensionsSetup = true;
                        return;
                    }
                    // Grow the height of the element as the gridPane height grows
                    newHeight = getHeight() * newValue.doubleValue() / oldValue.doubleValue();
                    
                    // If the user is currently dragging the element, update the origin height & originY.
                    if(Stream.of(Cursor.S_RESIZE, Cursor.SE_RESIZE, Cursor.SW_RESIZE).anyMatch(cursor -> getCursor() == cursor)){
                        originHeight += newHeight - getHeight();
                        newLayoutY = originY -= newHeight - getHeight();
                    }else if(Stream.of(Cursor.N_RESIZE, Cursor.NE_RESIZE, Cursor.NW_RESIZE, Cursor.E_RESIZE, Cursor.W_RESIZE).anyMatch(cursor -> getCursor() == cursor)){
                        originHeight += newHeight - getHeight();
                    }
                }
            }
            checkLocation(getLayoutX(), newLayoutY, getWidth(), newHeight, false);
            getPage().layout(); // Required to update the visual bounds of the element
            updateGridPaneScale();
        });
    }
    
    public void updateGridPaneScale(){
        // If gridPane dimensions are not fully setup yet, use the last saved scale.
        if(gridPane.areDimensionsSetup) scale = MathUtils.clamp(getHeight() / gridPane.getHeight(), MIN_SCALE, MAX_SCALE);
        updateGridPaneScale(scale);
    }
    private void updateGridPaneScale(double scale){
        // Using scale transform to make sure the pivot point is always (0, 0)
        gridPane.getTransforms().setAll(new Scale(scale, scale));
        // Grid pane width always match the element width, considering the scaling.
        gridPane.setPrefWidth(getWidth() / scale);
        //gridPane.setClip(new Rectangle(getWidth()/scale, getHeight()/scale));
        getPage().layout(); // Required to update the visual bounds of the element
    }
    
    @Override
    public void checkLocation(double itemX, double itemY, double width, double height, boolean allowSwitchPage){
        boolean itemYChanged = itemY != getLayoutY();
        // If the Y position changed, it means that the element is resized via N/W grab cursor so the origin needs to be updated as well.
        super.checkLocation(itemX, itemY, width, height, allowSwitchPage);
        
        // HEIGHT - min and max scale
        if(getWidth() == 0 || getHeight() == 0 || Double.isNaN(getRatio()) || Double.isInfinite(getRatio()) || !gridPane.areDimensionsSetup) return;
        double scale = getPage().fromGridY(getRealHeight()) / gridPane.getHeight(); // Not clamped
        double clampedScale = MathUtils.clamp(scale, MIN_SCALE, MAX_SCALE);
        if(scale != clampedScale){
            int newRealHeight = getPage().toGridY(gridPane.getHeight() * clampedScale);
            if(getRealHeight() != newRealHeight){
                if(itemYChanged) setRealY(getRealY() + getRealHeight() - newRealHeight); // When resized from north or west
                setRealHeight(newRealHeight);
            }
        }
    }
    
    public void updateLayout(){
        gridPane.updateLayout();
    }
    public void updateSkillsNotation(){
        gridPane.updateSkillsNotation();
        gridPane.updateLegend(false);
    }
    
    @Override
    public void addedToDocument(boolean markAsUnsave){
        MainWindow.skillsTab.registerSkillTableElement(this);
        updateLayout();
    }
    @Override
    protected void setupMenu(){
        // No menu
    }
    @Override
    public void removedFromDocument(boolean markAsUnsave){
        super.removedFromDocument(markAsUnsave);
        saveDefaultSize();
    }
    @Override
    public void delete(boolean markAsUnsave, UType undoType){
        // SkillTableElement can't be deleted and should always be kept.
    }
    
    public void saveDefaultSize(){
        MainWindow.skillsTab.getAssessments().stream().filter(a -> a.getId() == assessmentId.get()).findFirst().ifPresent(this::saveDefaultSize);
    }
    public void saveDefaultSize(SkillsAssessment assessment){
        assessment.setPrefPage(getPageNumber());
        assessment.setPrefRealX(getRealX());
        assessment.setPrefRealY(getRealY());
        assessment.setPrefWidth(getRealWidth());
        assessment.setPrefScale((float) getScale());
    }
    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("scale", scale);
        data.put("page", pageNumber);
        data.put("assessmentId", assessmentId.get());
        data.put("studentId", studentId.get());
        // Useless to save editionSkills that have no matching Notation (id == 0)
        data.put("list", editionSkills.get().stream().filter(s -> s.getNotationId() != 0).map(EditionSkill::toYAML).toList());
        return data;
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data){
        SkillTableElement element = readYAMLDataAndGive(data, true);
        if(MainWindow.mainScreen.document.getPagesNumber() > element.getPageNumber()){
            // There can't be more than one SkillTableElement element in the document
            MainWindow.mainScreen.document.getPages().forEach(page -> {
                int i = 0;
                while(i < page.getElements().size()){
                    if(page.getElements().get(i) instanceof SkillTableElement elem){
                        if(elem.equals(MainWindow.mainScreen.getSelected())) MainWindow.mainScreen.setSelected(null);
                        page.removeElement(elem, false, UType.NO_UNDO);
                        MainWindow.skillsTab.clearEditRelatedData();
                    }else i++;
                }
            });
            MainWindow.mainScreen.document.getPage(element.getPageNumber()).addElement(element, false, UType.NO_UNDO);
    
        }
    }
    
    public static SkillTableElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage){
        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        
        float scale = (float) Config.getDouble(data, "scale");
        
        int page = (int)  Config.getLong(data, "page");
        
        long assessmentId = Config.getLong(data, "assessmentId");
        long studentId = Config.getLong(data, "studentId");
    
        ArrayList<EditionSkill> skills = Config.getList(data, "list")
                .stream()
                .filter(skillData -> skillData instanceof Map)
                .map(skillData -> EditionSkill.getFromYAML((HashMap<String, Object>) skillData))
                .collect(Collectors.toCollection(ArrayList::new));

        return new SkillTableElement(x, y, page, hasPage, width, height, scale, assessmentId, studentId, skills);
    }
    
    // Copy editionSkill from student
    private final CallsBuffer<Student> callsBuffer = new CallsBufferMemory<>(300, oldStudent -> {
        if(getStudentId() != 0 && (oldStudent == null || getStudentId() != oldStudent.id())){
            tryLoadFromStudentInternal(MainWindow.skillsTab.getCurrentStudent());
        }
    });
    public void tryLoadFromStudent(Student oldStudent){
        callsBuffer.call(oldStudent);
    }
    private void tryLoadFromStudentInternal(Student student){
        if(student.editionSkills().isEmpty()) return;
        
        AtomicInteger loaded = new AtomicInteger();
        student.editionSkills().stream()
                .filter(s -> s.getNotationId() != 0 && editionSkills.get().stream().noneMatch(es ->
                        es.getNotationId() != 0 && es.getSkillId() == s.getSkillId() && // Same skill and notation not null
                                SkillsAssessment.getById(getAssessmentId()).getNotationsWithDefaults().stream() // Notation exists
                                        .anyMatch(n -> n.getId() == es.getNotationId())))
                .forEach(s -> {
                    editionSkills.get().removeIf(es -> es.getSkillId() == s.getSkillId()); // Remove old one
                    editionSkills.get().add(new EditionSkill(s.getSkillId(), s.getNotationId()));
                    loaded.incrementAndGet();
                });

        
        MainWindow.footerBar.showToast(Color.CORNFLOWERBLUE, Color.WHITE, FooterBar.ToastDuration.MEDIUM,
                TR.tr("skillsTab.student.copiedFromSACocheImport", String.valueOf(loaded), String.valueOf(student.editionSkills().size()), student.name()));
        
        updateSkillsNotation();
        MainWindow.skillsTab.refreshListView();
    }
    
    @Override
    public String getElementName(boolean plural){
        if(plural) return TR.tr("elements.name.skills");
        return TR.tr("elements.name.name.skill");
    }
    
    @Override
    public Element clone(){
        throw new RuntimeException("SkillTableElement can't be cloned.");
    }
    @Override
    public Element cloneHeadless(){
        return new SkillTableElement(getRealX(), getRealY(), getPageNumber(), false, getRealWidth(), getRealHeight(),
                getScale(), getAssessmentId(), getStudentId(), new ArrayList<>(getEditionSkills()));
    }
    
    @Override
    protected void onMouseRelease(){
        super.onMouseRelease();
        saveDefaultSize(); // Resized or moved
    }
    @Override
    public void simulateReleaseFromResize(){
        super.simulateReleaseFromResize();
    }
    
    @Override
    public void select(){
        super.select();
        SideBar.selectTab(MainWindow.skillsTab);
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
    public double getScale(){
        return scale;
    }
    
}
