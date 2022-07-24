/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.SkillTableElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;

public class SkillTableElementRenderer{
    
    private final PDDocument doc;
    private final TextRenderer textRenderer;
    private PageSpecs pageSpecs;
    private SkillTableElement element;
    private float bottomMargin;
    
    public SkillTableElementRenderer(PDDocument doc, TextRenderer textRenderer){
        this.doc = doc;
        this.textRenderer = textRenderer;
    }
    // Returns false if the user cancelled the export process.
    public boolean renderElement(SkillTableElement element, PDPageContentStream contentStream, PDPage page, PageSpecs ps) throws IOException{
        this.pageSpecs = ps;
        this.element = element;
        this.bottomMargin = ps.realHeight() - ps.height() - ps.startY();
        
        // Find assessment
        SkillsAssessment assessment = MainWindow.skillsTab.getAssessments().stream().filter(a -> a.getId() == element.getAssessmentId()).findAny().orElse(null);
        if(assessment == null) return true;
    
        // Map skills with their matching notation.
        LinkedHashMap<Skill, Notation> skills = new LinkedHashMap<>();
        assessment.getSkills().forEach(skill -> {
            EditionSkill editionSkill = element.getEditionSkills().stream().filter(es -> es.getSkillId() == skill.getId()).findFirst().orElse(null);
            Notation notation = editionSkill == null ? null : editionSkill.getMatchingNotation(assessment);
            skills.put(skill, notation);
        });
        if(skills.size() == 0) return true;
    
        
        // Draw the two first borders.
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth((float) element.getScale());
        
        contentStream.moveTo(ps.layoutXToPDCoo(0), ps.realXToPDCoo(element.getRealHeight())); // + 2 +
        contentStream.lineTo(ps.layoutXToPDCoo(0), ps.layoutYToPDCoo(0));                  // 1
        contentStream.lineTo(ps.realXToPDCoo(element.getRealHeight()), ps.layoutYToPDCoo(0)); // +
        contentStream.stroke();
        System.out.println("Line drawn");
    
    
        
        /*TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs(element.getBoundsHeight(), element.getBoundsWidth(), bottomMargin,
                element.getBaseLineY(), element.getRealX(), element.getRealY(), element.getText(), Color.BLACK, false);
    
    
        // Header
        Map.Entry<String, String> boldFontEntry = textRenderer.setContentStreamFont(contentStream, FontUtils.getDefaultFont(false, true, 11), pageSpecs.width());
        
        
        
        
        // Content
        Map.Entry<String, String> regularFontEntry = textRenderer.setContentStreamFont(contentStream, FontUtils.getDefaultFont(false, false, 11), pageSpecs.width());
        
        if(!textRenderer.drawText(page, contentStream, regularFontEntry, textSpecs, pageSpecs)) return false;*/
        
        
        
        return true;
    }
    
}
