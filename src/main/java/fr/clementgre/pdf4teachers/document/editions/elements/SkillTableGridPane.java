/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.NotationGraph;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.geometry.VPos;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SkillTableGridPane extends GridPane {
    
    public static final int H_PADDING = 5;
    public static final int V_PADDING = 2;
    public static final int TEXT_HEIGHT = 16;
    
    public boolean areDimensionsSetup;
    public Pane legend;
    public final SkillTableElement element;
    
    private final ArrayList<SkillLine> skillLines = new ArrayList<>();
    private ArrayList<Notation> notations = new ArrayList<>();
    
    public SkillTableGridPane(SkillTableElement element) {
        this.element = element;
        
        setMouseTransparent(true);
        getColumnConstraints().setAll(new ColumnConstraints(), new ColumnConstraints(80));
        getColumnConstraints().getFirst().setHgrow(Priority.ALWAYS);
        getColumnConstraints().getFirst().setFillWidth(true);
        setMaxHeight(Double.MAX_VALUE);
        
    }
    
    // When data updated
    public void updateLayout(){
        
        getChildren().clear();
        getRowConstraints().clear();
        skillLines.clear();
        SkillsAssessment assessment = MainWindow.skillsTab.getCurrentAssessment();
        if(assessment == null){
            setVisible(false);
            if(element.isSelected()) MainWindow.mainScreen.setSelected(null);
            return;
        }
        setVisible(true);
    
        addHeaderLabel(0, TR.tr("skillTableElement.header.skill"));
        addHeaderLabel(1, TR.tr("skillTableElement.header.grade"));
        
        AtomicInteger i = new AtomicInteger();
        assessment.getSkills().forEach(skill -> skillLines.add(new SkillLine(skill, i.incrementAndGet(), this, assessment)));
        if(i.get() == 0){
            setVisible(false);
            if(element.isSelected()) MainWindow.mainScreen.setSelected(null);
            return;
        }
    
        updateSkillsNotation();
        
        legend = getGridCellPane(false, true);
        updateLegend(true);
        add(legend, 0, i.incrementAndGet(), 2, 1);
    }
    
    private ArrayList<NotationLegend> notationsLegend;
    void updateLegend(boolean forceUpdate){
        if(legend == null) return;
        SkillsAssessment assessment = MainWindow.skillsTab.getCurrentAssessment();
    
        ArrayList<Notation> notations = new ArrayList<>();
        notations.addAll(assessment.getNotations());
        notations.addAll(SkillsAssessment.getOtherNotations().stream().filter(notation ->
            element.getEditionSkills().stream()
                    .anyMatch(es -> es.getNotationId() == notation.getId() // Has a matching editionSkill
                            && assessment.getSkills().stream().anyMatch(skill ->
                            es.getSkillId() == skill.getId())) // And this edition skill is linked to a Skill of this specific assessment
        ).toList());
    
        if(!forceUpdate && notations.equals(this.notations)) return; // Same notations -> no need to update
        
        legend.getChildren().clear();
        notationsLegend = new ArrayList<>(notations.stream().map(notation -> new NotationLegend(notation, legend)).toList());
    
        if(!this.notations.isEmpty()) layoutLegend(); // No need to layout legend the first time
        legend.widthProperty().addListener((observable, oldValue, newValue) -> {
            layoutLegend();
        });
        this.notations = notations;
    }
    private void layoutLegend(){
        notationsLegend.getFirst().regen(); // We need to regen at least one child to update cell height visually while dragging.
    
        AtomicInteger x = new AtomicInteger(H_PADDING);
        AtomicInteger y = new AtomicInteger(V_PADDING);
        for(NotationLegend notationLeg : notationsLegend){
            int result = notationLeg.update(x.get(), y.get());
            if(result >= 0) x.set(result);
            else{ x.set(-result); y.addAndGet(TEXT_HEIGHT); }
        }
        legend.setPrefHeight(y.get() + TEXT_HEIGHT + V_PADDING + 1); // +1 for the border
    }
    
    public void updateSkillsNotation(){
        for(SkillLine skillLine : skillLines){
            skillLine.updateNotation();
        }
    }
    
    private static class SkillLine{
    
        private final Skill skill;
        private final SkillsAssessment assessment;
        private final int y;
        private final SkillTableGridPane gridPane;
        private Pane graphPane;
        public SkillLine(Skill skill, int y, SkillTableGridPane gridPane, SkillsAssessment assessment){
            this.skill = skill;
            this.assessment = assessment;
            this.y = y;
            this.gridPane = gridPane;
            Pane pane = getGridCellPane(false, true);
    
            Text acrText = getText("", true, H_PADDING, V_PADDING);
            Text descText = getText("", false, H_PADDING, TEXT_HEIGHT);
    
            pane.widthProperty().addListener((observable, o, n) -> {
                acrText.setText(wrap(skill.getAcronym(), acrText.getFont(), (int) pane.getWidth()));
                descText.setLayoutY(V_PADDING + acrText.getLayoutBounds().getHeight());
                descText.setText(wrap(skill.getName(), descText.getFont(), (int) pane.getWidth()));
                pane.setPrefHeight(descText.getLayoutY() + descText.getLayoutBounds().getHeight() + V_PADDING);
            });
            pane.getChildren().addAll(acrText, descText);
    
            gridPane.add(pane, 0, y);
        }
        
        public void updateNotation(){
            gridPane.getChildren().remove(graphPane);
            graphPane = getGridCellPane(false, false);
            
            EditionSkill editionSkill = MainWindow.skillsTab.getSkillTableElement().getEditionSkills().stream().filter(es -> es.getSkillId() == skill.getId()).findFirst().orElse(null);
            Notation notation = editionSkill == null ? null : editionSkill.getMatchingNotation(assessment);
            if(notation != null){
                Region notationGraph = new NotationGraph(1.3, MainWindow.skillsTab.getCurrentAssessment().getNotationType(), notation, true);
                graphPane.getChildren().setAll(notationGraph);
                notationGraph.layoutXProperty().bind(graphPane.widthProperty().subtract(notationGraph.widthProperty()).divide(2));
                notationGraph.layoutYProperty().bind(graphPane.heightProperty().subtract(notationGraph.heightProperty()).divide(2));
            }
            gridPane.add(graphPane, 1, y);
        }
    }
    
    public static class NotationLegend{
        public static final int BLOCK_SPACING = 8;
        public static final int GRAPH_SIZE = 14;
        public static final int G_T_SPACING = 3;
    
        private final Pane legendPane;
        private final Notation notation;
        public NotationGraph graph;
        public Text text;
        public NotationLegend(Notation notation, Pane legendPane){
            this.notation = notation;
            this.legendPane = legendPane;
            this.graph = new NotationGraph(GRAPH_SIZE/20d, MainWindow.skillsTab.getCurrentAssessment().getNotationType(), notation, true);
            this.text = getText(notation.getName(), false, 9, 0, 0);
            legendPane.getChildren().addAll(graph, text);
        }
        public void regen(){
            legendPane.getChildren().remove(text);
            text = getText(notation.getName(), false, 9, 0, 0);
            legendPane.getChildren().addAll(text);
        }
        /* @return the new x position or minus the opposite value (negative) if a new row has been started. */
        public int update(int x, int y){
            graph.setLayoutX(x);
            graph.setLayoutY(y+1);
            text.setLayoutX(x + GRAPH_SIZE + G_T_SPACING);
            text.setLayoutY(y+2);
            
            if(getNewX() + H_PADDING > legendPane.getWidth()){ // WRAP
                graph.setLayoutX(H_PADDING);
                text.setLayoutX(H_PADDING + GRAPH_SIZE + G_T_SPACING);
                graph.setLayoutY(graph.getLayoutY() + TEXT_HEIGHT);
                text.setLayoutY(text.getLayoutY() + TEXT_HEIGHT);
                return -getNewX() - BLOCK_SPACING; // New row => negative new x position
            }
            return getNewX() + BLOCK_SPACING;
        }
        private int getNewX(){
            return (int) (text.getLayoutX() + text.getLayoutBounds().getWidth());
        }
    }
    
    private void addHeaderLabel(int x, String header){
        Pane pane = getGridCellPane(true, x == 0);
        Text acrText = getText("", true, H_PADDING, V_PADDING);
        
        pane.widthProperty().addListener((observable) -> {
            acrText.setText(wrap(header, acrText.getFont(), (int) pane.getWidth()));
            pane.setPrefHeight(acrText.getLayoutBounds().getHeight() + 2*V_PADDING);
        });
        pane.getChildren().add(acrText);
        add(pane, x, 0);
    }
    private static String wrap(String text, Font font, int width){
        return new TextWrapper(text, font, width - 2*H_PADDING).wrap();
    }
    private static Pane getGridCellPane(boolean firstRow, boolean firstCol){
        Pane pane = new Pane();
        pane.getStyleClass().add("bordered-grid-cell");
        if(firstRow) pane.getStyleClass().add("first-row");
        if(firstCol) pane.getStyleClass().add("first-column");
        GridPane.setFillWidth(pane, true);
        GridPane.setFillHeight(pane, true);
        return pane;
    }
    private static Text getText(String text, boolean bold, int x, int y){
        return getText(text, bold,11, x, y);
    }
    private static Text getText(String text, boolean bold, int fontSize, int x, int y){
        Text textText = new ScratchText(text);
        textText.setBoundsType(TextBoundsType.LOGICAL);
        textText.setTextOrigin(VPos.TOP);
        textText.setFont(FontUtils.getDefaultFont(false, bold, fontSize));
        textText.setStyle("-fx-font: normal " + (bold ? "bold" : "normal") + " " + fontSize + " 'Open Sans' !important");
        textText.setLayoutX(x);
        textText.setLayoutY(y);
        return textText;
    }
    
    
}
