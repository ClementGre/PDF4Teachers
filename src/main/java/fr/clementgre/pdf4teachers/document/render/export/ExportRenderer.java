package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Pattern;

public class ExportRenderer {

    public void exportFile(File pdfFile, File toFile, boolean textElements, boolean gradesElements, boolean drawElements) throws Exception {

        File editFile = Edition.getEditFile(pdfFile);

        PDDocument doc = PDDocument.load(pdfFile);
        new PDStream(doc, new FileInputStream(pdfFile), COSName.FLATE_DECODE);
        doc.getDocumentInformation().setModificationDate(Calendar.getInstance());

        TextElementRenderer textElementRenderer = new TextElementRenderer(doc);
        GradeElementRenderer gradeElementRenderer = new GradeElementRenderer(doc);

        Element[] elements = Edition.simpleLoad(editFile);
        for(int pageNumber = 0 ; pageNumber < doc.getNumberOfPages() ; pageNumber++){

            PDPage page = doc.getPage(pageNumber);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
            page.setBleedBox(page.getCropBox());

            float startX = page.getBleedBox().getLowerLeftX();
            float startY = page.getBleedBox().getLowerLeftY();
            float pageRealHeight = page.getBleedBox().getHeight();
            float pageRealWidth = page.getBleedBox().getWidth();
            float pageHeight = page.getCropBox().getHeight();
            float pageWidth = page.getCropBox().getWidth();
            // ROTATE PAGES ADAPT
            if(page.getRotation() == 90 || page.getRotation() == 270){
                startY = page.getBleedBox().getLowerLeftX();
                startX = page.getBleedBox().getLowerLeftY();

                pageRealHeight = page.getBleedBox().getWidth();
                pageRealWidth = page.getBleedBox().getHeight();

                pageHeight = page.getCropBox().getWidth();
                pageWidth = page.getCropBox().getHeight();
            }

            // ROTATE PAGES ADAPT
            switch(page.getRotation()){
                case 90: contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealHeight, 0));
                    break;
                case 180: contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealWidth, pageRealHeight));
                    break;
                case 270: contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), 0, pageRealWidth));
                    break;
            }

            for(Element element : elements){

                if(element.getPageNumber() != pageNumber) continue;

                if(element instanceof TextElement){
                    if(textElements) textElementRenderer.renderElement((TextElement) element, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }else if(element instanceof GradeElement){
                    if(gradesElements) gradeElementRenderer.renderElement((GradeElement) element, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }/*else if(element instanceof DrawElement){
                    if(drawElements)
                }*/
            }

            contentStream.close();
        }

        doc.save(toFile);
        doc.close();
    }
}
