/*
 * Copyright (c) 2022-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.document.editions.elements.SkillTableElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.NotationGraph;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.DPIManager;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static fr.clementgre.pdf4teachers.document.editions.elements.SkillTableGridPane.*;

public class SkillTableElementRenderer{
    
    private final PDDocument doc;
    private final TextRenderer textRenderer;
    private final DPIManager dpiManager;
    private SkillTableElement element;
    private PDPageContentStream cs;
    private PDPage page;
    private PageSpecs ps;
    
    public SkillTableElementRenderer(PDDocument doc, TextRenderer textRenderer, int dpi){
        this.doc = doc;
        this.textRenderer = textRenderer;
        this.dpiManager = new DPIManager(dpi);
    }
    // Returns false if the user cancelled the export process.
    public boolean renderElement(SkillTableElement element, PDPageContentStream cs, PDPage page, PageSpecs ps) throws IOException{
        this.element = element;
        this.cs = cs;
        this.page = page;
        this.ps = ps;
        
        dpiManager.initOneCmWidthFromA4Width(ps.width());
        
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
        if(skills.isEmpty()) return true;
    
        
        // HEADER
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(getScale());
    
    
        fillRect(new Color(232, 232, 232), 0, getLayoutHeight());
        
        float y1 = drawWrappedTextAt(H_PADDING, V_PADDING, getSkillsColWidth() - 2*H_PADDING,
                TR.tr("skillTableElement.header.skill"), true);
        if(y1 == -1) return false;
    
        float y2 = drawWrappedTextAt(getSkillsColWidth() + H_PADDING, V_PADDING, getNotationColWidth() - 2*H_PADDING,
                TR.tr("skillTableElement.header.grade"), true);
        if(y2 == -1) return false;
        
        float y = Math.max(y1, y2) + V_PADDING;
    
        fillRect(Color.WHITE, y - 1, getLayoutHeight());
        drawHLine(0);
        drawHLine(y - 1); // -1 because the border is on the top cell, and we pass the TOP y.
        drawHLine(getLayoutHeight() - 1); // -1 because the border is on the top cell, and we pass the TOP y.
        
        drawVLine(0);
        drawVLine(getLayoutWidth() - 1);
        
        for(Map.Entry<Skill, Notation> entry : skills.entrySet()){
            Skill skill = entry.getKey();
            Notation notation = entry.getValue();
            
            float topY = y;
            y = drawWrappedTextAt(H_PADDING, y + V_PADDING, getSkillsColWidth() - 2*H_PADDING,
                    skill.getAcronym(), true);
            if(y == -1) return false;
            y = drawWrappedTextAt(H_PADDING, y, getSkillsColWidth() - 2*H_PADDING,
                    skill.getName(), false);
            if(y == -1) return false;
            
            y += V_PADDING;
            drawHLine(y - 1); // -1 because the border is on the top cell, and we pass the TOP y.
    
            if(notation == null) continue;
            float size = 20*1.3f;
            Image graph = new NotationGraph(dpiManager.getPixelsLength(1.3 * getScale()), assessment.getNotationType(), notation, true).isolatedSnapshot();
            
            drawImage(graph, getSkillsColWidth() + (getNotationColWidth() - size) / 2, topY + (y - topY - size) / 2, size, size);
            
        }
        // Center vertical line must not cross the legend zone.
        drawVLine(getLayoutWidth() - 80 - 1, y);
        
        // LEGEND
    
        ArrayList<Notation> notations = new ArrayList<>();
        notations.addAll(assessment.getNotations());
        notations.addAll(SkillsAssessment.getOtherNotations().stream().filter(notation ->
                element.getEditionSkills().stream()
                        .anyMatch(es -> es.getNotationId() == notation.getId() // Has a matching editionSkill
                                && assessment.getSkills().stream().anyMatch(skill ->
                                es.getSkillId() == skill.getId())) // And this edition skill is linked to a Skill of this specific assessment
        ).toList());
    
        float lx = H_PADDING;
        float ly = y + V_PADDING;
        for(Notation notation : notations){
            float result = addLegend(lx, ly, notation);
            if(result == Float.MIN_VALUE) return false;
            if(result >= 0) lx = result;
            else{ lx = -result; ly += TEXT_HEIGHT; }
        }
        
        return true;
    }
    
    // Returns Float.MIN_VALUE if action canceled by user.
    private float addLegend(float x, float y, Notation notation) throws IOException{
        float graphSize = NotationLegend.GRAPH_SIZE;
        float gTSpacing = NotationLegend.G_T_SPACING;
        Image graph = new NotationGraph(dpiManager.getPixelsLength(NotationLegend.GRAPH_SIZE/20f * getScale()), MainWindow.skillsTab.getCurrentAssessment().getNotationType(), notation, true).isolatedSnapshot();

        float newX = x + graphSize + gTSpacing + measureTextWidth(notation.getName());
        
        if(newX + H_PADDING > getLayoutWidth()){ // WRAP
            newX = drawTextAt(H_PADDING + graphSize + gTSpacing, y+TEXT_HEIGHT+2, notation.getName());
            if(newX == -1) return Float.MIN_VALUE;
            drawImage(graph, H_PADDING, y+TEXT_HEIGHT+1, graphSize, graphSize);
            
            return -newX - NotationLegend.BLOCK_SPACING; // New row => negative new x
            
        }
        newX = drawTextAt(x+graphSize+gTSpacing, y+2, notation.getName());
        if(newX == -1) return Float.MIN_VALUE;
        drawImage(graph, x, y+1, graphSize, graphSize);
        
        return newX + NotationLegend.BLOCK_SPACING;
    }
    
    private void drawImage(Image image, float x, float y, float width, float height) throws IOException{
        BufferedImage bImg = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bImg, "png", bos);
        byte[] data = bos.toByteArray();
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, data, "SkillTableElementRenderer image");
        
        cs.drawImage(pdImage, innerXToPD(x), innerYToPD(y+height), innerWToPD(width), innerHToPD(height));
    }
    
    // Returns the new Y position (bottom y + text height) or -1 if the user cancelled the export process.
    // Font: 11pt
    private float drawWrappedTextAt(float x, float y, float maxWidth, String text, boolean bold) throws IOException{
        Font font = FontUtils.getDefaultFont(false, bold, 11);
        text = new TextWrapper(text, font, (int) maxWidth).wrap();
        
        Text textText = new ScratchText(text);
        textText.setBoundsType(TextBoundsType.LOGICAL);
        textText.setTextOrigin(VPos.TOP);
        textText.setFont(font);
        
        TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs((float) textText.getLayoutBounds().getHeight() * getScale(), (float) textText.getLayoutBounds().getWidth() * getScale(), ps.height(),
                (float) textText.getBaselineOffset() * getScale(), element.getRealX() + ps.layoutXToReal(x * getScale()), element.getRealY() + ps.layoutYToReal(y * getScale()), text, Color.BLACK, false, 11 * getScale());
        
        Map.Entry<String, String> fontEntry = textRenderer.setContentStreamFont(cs, FontUtils.getDefaultFont(false, bold, 11 * getScale()), ps.width());
        
        if(!textRenderer.drawText(page, cs, fontEntry, textSpecs, ps)) return -1;
        return (float) (y + textText.getLayoutBounds().getHeight());
    }
    
    // Font: 9pt (For the legend)
    private float measureTextWidth(String text){
        return (float) getText(text, false, 9).getLayoutBounds().getWidth();
    }
    // Returns the new X position (right x + text width) or -1 if the user cancelled the export process.
    // Font: 9pt (For the legend)
    private float drawTextAt(float x, float y, String text) throws IOException{
        Text textText = getText(text, false, 9);
        TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs((float) textText.getLayoutBounds().getHeight() * getScale(), (float) textText.getLayoutBounds().getWidth() * getScale(), ps.height(),
                (float) textText.getBaselineOffset() * getScale(), element.getRealX() + ps.layoutXToReal(x * getScale()), element.getRealY() + ps.layoutYToReal(y * getScale()), text, Color.BLACK, false, 9 * getScale());
        
        Map.Entry<String, String> fontEntry = textRenderer.setContentStreamFont(cs, FontUtils.getDefaultFont(false, false, 9 * getScale()), ps.width());
        
        if(!textRenderer.drawText(page, cs, fontEntry, textSpecs, ps)) return -1;
        return (float) (x + textText.getLayoutBounds().getWidth());
    }
    private Text getText(String text, boolean bold, float fontSize){
        Font font = FontUtils.getDefaultFont(false, bold, fontSize);
        Text textText = new ScratchText(text);
        textText.setBoundsType(TextBoundsType.LOGICAL);
        textText.setTextOrigin(VPos.TOP);
        textText.setFont(font);
        return textText;
    }
    
    private void drawHLine(float topInnerY) throws IOException{
        // The border is 1px thick, then we need to remove 0.5px on each side.
        cs.moveTo(innerXToPD(0.5f),                                                    innerYToPD(topInnerY + 0.5f));
        cs.lineTo(innerXToPD(getLayoutWidth() - 0.5f),  innerYToPD(topInnerY + 0.5f));
        cs.stroke();
    }
    private void drawVLine(float leftInnerX) throws IOException{
        drawVLine(leftInnerX, getLayoutHeight());
    }
    private void drawVLine(float leftInnerX, float bottomInnerY) throws IOException{
        // The border is 1px thick, then we need to remove 0.5px on each side.
        cs.moveTo(innerXToPD(leftInnerX + 0.5f), innerYToPD(0.5f));
        cs.lineTo(innerXToPD(leftInnerX + 0.5f), innerYToPD(bottomInnerY - 0.5f));
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
        return Math.abs(getLayoutWidth() - getNotationColWidth());
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
