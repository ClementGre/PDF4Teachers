/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.SkillTableElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static fr.clementgre.pdf4teachers.document.editions.elements.SkillTableGridPane.H_PADDING;
import static fr.clementgre.pdf4teachers.document.editions.elements.SkillTableGridPane.V_PADDING;

public class SkillTableElementRenderer{
    
    private final PDDocument doc;
    private final TextRenderer textRenderer;
    private SkillTableElement element;
    private PDPageContentStream cs;
    private PDPage page;
    private PageSpecs ps;
    
    public SkillTableElementRenderer(PDDocument doc, TextRenderer textRenderer){
        this.doc = doc;
        this.textRenderer = textRenderer;
    }
    // Returns false if the user cancelled the export process.
    public boolean renderElement(SkillTableElement element, PDPageContentStream cs, PDPage page, PageSpecs ps) throws IOException{
        this.element = element;
        this.cs = cs;
        this.page = page;
        this.ps = ps;
        
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
    
        
        // HEADER
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(getScale());
    
    
        fillRect(new Color(232, 232, 232), 0, getLayoutHeight());
        
        float newY1 = drawWrappedTextAt(H_PADDING, V_PADDING, getSkillsColWidth() - 2*H_PADDING,
                TR.tr("skillTableElement.header.skill"), true);
        if(newY1 == -1) return false;
    
        float newY2 = drawWrappedTextAt(getSkillsColWidth() + H_PADDING, V_PADDING, getNotationColWidth() - 2*H_PADDING,
                TR.tr("skillTableElement.header.grade"), true);
        if(newY2 == -1) return false;
        
        float newY = Math.max(newY1, newY2) + V_PADDING;
    
        fillRect(Color.WHITE, newY - 1, getLayoutHeight());
        drawHLine(0);
        drawHLine(newY - 1); // -1 because the border is on the top cell, and we pass the TOP y.
        drawHLine(getLayoutHeight() - 1); // -1 because the border is on the top cell, and we pass the TOP y.
        
        drawVLine(0);
        drawVLine(getLayoutWidth() - 80 - 1); // border are from the left text cell.
        drawVLine(getLayoutWidth() - 1);
        
        
        
        
        
        // SKILLS
        //Map.Entry<String, String> regularFontEntry = textRenderer.setContentStreamFont(cs, FontUtils.getDefaultFont(false, false, 11), ps.width());
        
        
        
        return true;
    }
    
    
    // Returns the new Y position (bottom y + text height) or -1 if the user cancelled the export process.
    // Font: 11pt
    private float drawWrappedTextAt(float x, float y, float maxWidth, String text, boolean bold) throws IOException{
        Font font = FontUtils.getDefaultFont(false, bold, 11);
        text = new TextWrapper(text, font, (int) maxWidth).wrap();
        
        Text textText = new Text(text);
        textText.setBoundsType(TextBoundsType.LOGICAL);
        textText.setTextOrigin(VPos.TOP);
        textText.setFont(font);
    
        TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs((float) textText.getLayoutBounds().getHeight() * getScale(), (float) textText.getLayoutBounds().getWidth() * getScale(), ps.bottomMargin(),
                (float) textText.getBaselineOffset() * getScale(), element.getRealX() + ps.layoutXToReal(x * getScale()), element.getRealY() + ps.layoutYToReal(y * getScale()), text, Color.BLACK, false, 11 * getScale());
        
        Map.Entry<String, String> fontEntry = textRenderer.setContentStreamFont(cs, FontUtils.getDefaultFont(false, bold, 11 * getScale()), ps.realWidth());
        
        if(!textRenderer.drawText(page, cs, fontEntry, textSpecs, ps)) return -1;
        return (float) (y + textText.getLayoutBounds().getHeight());
    }
    
    private void drawHLine(float topInnerY) throws IOException{
        // The border is 1px thick, then we need to remove 0.5px on each side.
        cs.moveTo(innerXToPD(0.5f),                                                    innerYToPD(topInnerY + 0.5f));
        cs.lineTo(innerXToPD(getLayoutWidth() - 0.5f),  innerYToPD(topInnerY + 0.5f));
        cs.stroke();
    }
    private void drawVLine(float leftInnerX) throws IOException{
        // The border is 1px thick, then we need to remove 0.5px on each side.
        cs.moveTo(innerXToPD(leftInnerX + 0.5f), innerYToPD(0.5f));
        cs.lineTo(innerXToPD(leftInnerX + 0.5f), innerYToPD(getLayoutHeight() - 0.5f));
        cs.stroke();
    }
    private void fillRect(Color fill, float topY, float bottomY) throws IOException{
        // The border is 1px thick, then we need to remove 0.5px on each side.
        cs.addRect(innerXToPD(0.5f), innerYToPD(bottomY - 0.5f), innerWToPD(getLayoutWidth() - 0.5f), innerHToPD(bottomY - topY - 1));
        cs.setNonStrokingColor(fill);
        cs.fill();
        cs.setNonStrokingColor(Color.BLACK); // Return to black for text printing.
    }
    
    // Not scaled widths
    private float getLayoutWidth(){
        // The element never really fits fully the width
        return ps.realXToLayout(element.getRealWidth()) / getScale() - 0.5f;
    }
    private float getLayoutHeight(){
        // The element never really fits fully the width
        return ps.realYToLayout(element.getRealHeight()) / getScale();
    }
    private float getSkillsColWidth(){
        return Math.abs(getLayoutWidth() - 80);
    }
    private float getNotationColWidth(){
        return 80;
    }
    
    private float getScale(){
        return (float) element.getScale();
    }
    // These methods makes the scaling
    private float innerXToPD(float x){
        return ps.layoutXToPDCoo(ps.realXToLayout(element.getRealX()) + x * getScale());
    }
    private float innerYToPD(float y){
        return ps.layoutYToPDCoo(ps.realYToLayout(element.getRealY()) + y * getScale());
    }
    private float innerWToPD(float w){
        return ps.layoutWToPDCoo(w * getScale());
    }
    private float innerHToPD(float h){
        return ps.layoutHToPDCoo(h * getScale());
    }
    
}
