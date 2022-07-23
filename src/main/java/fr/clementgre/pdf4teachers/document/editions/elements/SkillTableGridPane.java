/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.NotationGraph;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
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
    
    private static final int H_PADDING = 5;
    private static final int V_PADDING = 2;
    private static final int TEXT_HEIGHT = 16;
    
    public boolean areDimensionsSetup = false;
    public Pane legend;
    private final SkillTableElement element;
    
    public SkillTableGridPane(SkillTableElement element) {
        this.element = element;
        
        setMouseTransparent(true);
        getColumnConstraints().setAll(new ColumnConstraints(), new ColumnConstraints(80));
        getColumnConstraints().get(0).setHgrow(Priority.ALWAYS);
        getColumnConstraints().get(0).setFillWidth(true);
        setMaxHeight(Double.MAX_VALUE);
        
    }
    
    // When data updated
    public void updateLayout(){
        areDimensionsSetup = false;
        System.out.println("----- LAYOUT GRID PANE -----");
        
        getChildren().clear();
        getRowConstraints().clear();
        SkillsAssessment assessment = MainWindow.skillsTab.getCurrentAssessment();
        if(assessment == null){
            setVisible(false);
            return;
        }
        setVisible(true);
        
        addGridLabel(0, 0, TR.tr("skillTableElement.header.skill"), null);
        addGridLabel(1, 0, TR.tr("skillTableElement.header.grade"), null);
        
        AtomicInteger i = new AtomicInteger();
        assessment.getSkills().forEach(skill -> {
            EditionSkill editionSkill = element.getEditionSkills().stream().filter(es -> es.getSkillId() == skill.getId()).findFirst().orElse(null);
            
            Notation notation = null;
            if(editionSkill != null) notation = editionSkill.getMatchingNotation(assessment);
            
            addGridLabel(0, i.incrementAndGet(), skill.getAcronym(), skill.getName());
            addGridNotationGraph(1, i.get(), notation);
        });
        if(i.get() == 0){
            setVisible(false);
            return;
        }
        
        legend = getGridCellPane(false, true);
    
        ArrayList<NotationLegend> notationsLegend = new ArrayList<>();
        assessment.getNotations().forEach(notation -> notationsLegend.add(new NotationLegend(notation, legend)));
        SkillsAssessment.getOtherNotations().forEach(notation -> {
            boolean match = element.getEditionSkills().stream()
                    .anyMatch(es -> es.getNotationId() == notation.getId() // Has a matching editionSkill
                            && assessment.getSkills().stream().anyMatch(skill ->
                            es.getSkillId() == skill.getId())); // And this edition skill is linked to a Skill of this specific assessment
            if(match)  notationsLegend.add(new NotationLegend(notation, legend));
        });
    
        legend.widthProperty().addListener((observable, oldValue, newValue) -> {
            notationsLegend.get(0).regen(); // We need to regen at least one child to update cell height visually while dragging.
    
            AtomicInteger x = new AtomicInteger(H_PADDING);
            AtomicInteger y = new AtomicInteger(V_PADDING);
            for(NotationLegend notationLeg : notationsLegend){
                int result = notationLeg.update(x.get(), y.get());
                if(result >= 0) x.set(result);
                else{ x.set(-result); y.addAndGet(TEXT_HEIGHT); }
            }
            legend.setPrefHeight(y.get() + TEXT_HEIGHT + V_PADDING + 1); // +1 for the border
        });
    
        add(legend, 0, i.incrementAndGet(), 2, 1);
        
    }
    
    private static class NotationLegend{
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
            this.text = getText(notation.getName(), false, 9, 0, 0);
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
    
    private void addGridLabel(int x, int y, String header, String text){
        Pane pane = getGridCellPane(y == 0, x == 0);
    
        Text acrText = getText("", true, H_PADDING, V_PADDING);
        pane.getChildren().add(acrText);
        if(text != null){
            Text descText = getText("", false, H_PADDING, TEXT_HEIGHT);
            
            pane.widthProperty().addListener((observable, o, n) -> {
                acrText.setText(wrap(header, acrText.getFont(), (int) pane.getWidth()));
                descText.setLayoutY(V_PADDING + acrText.getLayoutBounds().getHeight());
                descText.setText(wrap(text, descText.getFont(), (int) pane.getWidth()));
                pane.setPrefHeight(descText.getLayoutY() + descText.getLayoutBounds().getHeight() + V_PADDING);
            });
            pane.getChildren().add(descText);
        }else{
            pane.widthProperty().addListener((observable) -> {
                acrText.setText(wrap(header, acrText.getFont(), (int) pane.getWidth()));
                pane.setPrefHeight(acrText.getLayoutBounds().getHeight() + 2*V_PADDING);
            });
            pane.setStyle("-fx-background-color: black, #e8e8e8");
            
        }
        
        add(pane, x, y);
    }
    
    
    private String wrap(String text, Font font, int width){
        return new TextWrapper(text, font, width - 2*H_PADDING).wrap();
    }
    
    private Pane getGridCellPane(boolean firstRow, boolean firstCol){
        Pane pane = new Pane();
        pane.getStyleClass().add("bordered-grid-cell");
        if(firstRow) pane.getStyleClass().add("first-row");
        if(firstCol) pane.getStyleClass().add("first-column");
        GridPane.setFillWidth(pane, true);
        GridPane.setFillHeight(pane, true);
        return pane;
    }
    private Text getText(String text, boolean bold, int x, int y){
        return getText(text, bold,11, x, y);
    }
    private static Text getText(String text, boolean bold, int fontSize, int x, int y){
        Text textText = new Text(text);
        textText.setBoundsType(TextBoundsType.LOGICAL);
        textText.setTextOrigin(VPos.TOP);
        textText.setFont(FontUtils.getDefaultFont(false, bold, fontSize));
        textText.setStyle("-fx-font: normal " + (bold ? "bold" : "normal") + " " + fontSize + " 'Open Sans' !important");
        textText.setLayoutX(x);
        textText.setLayoutY(y);
        return textText;
    }
    
    private void addGridNotationGraph(int x, int y, Notation notation){
        Pane pane = getGridCellPane(y == 0, x == 0);
        if(notation != null){
            Region notationGraph = new NotationGraph(1.3, MainWindow.skillsTab.getCurrentAssessment().getNotationType(), notation, true);
            pane.getChildren().add(notationGraph);
            notationGraph.layoutXProperty().bind(pane.widthProperty().subtract(notationGraph.widthProperty()).divide(2));
            notationGraph.layoutYProperty().bind(pane.heightProperty().subtract(notationGraph.heightProperty()).divide(2));
        }
        add(pane, x, y);
    }
    
    
}
