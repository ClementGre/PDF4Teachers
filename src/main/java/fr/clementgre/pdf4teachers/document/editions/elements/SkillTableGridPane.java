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
import javafx.geometry.VPos;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.concurrent.atomic.AtomicInteger;

public class SkillTableGridPane extends GridPane {
    
    
    private static final int H_PADDING = 5;
    private static final int V_PADDING = 2;
    private static final int TEXT_HEIGHT = 16;
    
    private final SkillTableElement element;
    public SkillTableGridPane(SkillTableElement element) {
        this.element = element;
        
        setMouseTransparent(true);
        getColumnConstraints().setAll(new ColumnConstraints(), new ColumnConstraints(80));
        getColumnConstraints().get(0).setHgrow(Priority.ALWAYS);
        setMaxHeight(Double.MAX_VALUE);
    }
    
    // When data updated
    public void updateLayout(){
        
        getChildren().clear();
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
            addGridNotationGraph(1, i.get(), assessment, notation);
        });
        if(i.get() == 0) setVisible(false);
        
    }
    
    private void addGridLabel(int x, int y, String header, String text){
        Pane pane = getGridCellPane(y == 0, x == 0);
        
        Text acrText = getText(header, true, H_PADDING, V_PADDING);
        pane.getChildren().add(acrText);
        
        
        if(text != null){
            Text descText = getText(text + "\n", false, H_PADDING, TEXT_HEIGHT); // Make sure the text is always at least two lines long.
            descText.maxHeight(TEXT_HEIGHT*2);
            pane.widthProperty().addListener((observable) -> {
                acrText.setText(wrapHeader(header, acrText.getFont(), (int) pane.getWidth()));
                descText.setText(wrapDescription(text, descText.getFont(), (int) pane.getWidth()));
                //System.out.println(acrText.getLayoutBounds().getHeight() + " / " + descText.getLayoutBounds().getHeight());
            });
            pane.getChildren().add(descText);
            pane.setPrefHeight(50); // 2 (padding) + 16 (line height) * 32 (2x line height)
        }else{
            pane.setStyle("-fx-background-color: black, #e8e8e8");
            pane.setPrefHeight(20); // 2 (padding) + 16 (line height) + 2 (padding)
        }
        
        
        add(pane, x, y);
    }
    private String wrapHeader(String text, Font font, int width){
        return TextWrapper.wrapFirstLineWithEllipsis(text, font, width - 2*H_PADDING);
    }
    private String wrapDescription(String text, Font font, int width){
        return TextWrapper.wrapTwoFirstLinesWithEllipsis(text + "\n", font, width - 2*H_PADDING);
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
        Text textText = new Text(text);
        textText.setBoundsType(TextBoundsType.LOGICAL);
        textText.setTextOrigin(VPos.TOP);
        textText.setStyle("-fx-font: normal " + (bold ? "bold" : "normal") + " 11 'Open Sans' !important");
        textText.setLayoutX(x);
        textText.setLayoutY(y);
        return textText;
    }
    
    private void addGridNotationGraph(int x, int y, SkillsAssessment assessment, Notation notation){
        Pane pane = getGridCellPane(y == 0, x == 0);
        if(notation != null){
            Region notationGraph = new NotationGraph(1.3, assessment.getNotationType(), notation, true);
            pane.getChildren().add(notationGraph);
            notationGraph.layoutXProperty().bind(pane.widthProperty().subtract(notationGraph.widthProperty()).divide(2));
            notationGraph.layoutYProperty().bind(pane.heightProperty().subtract(notationGraph.heightProperty()).divide(2));
        }
        add(pane, x, y);
    }
    
    
}
