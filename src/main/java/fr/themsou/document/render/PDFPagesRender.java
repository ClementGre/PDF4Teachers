package fr.themsou.document.render;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import fr.themsou.main.Main;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PDFPagesRender {

	private PDDocument document;
	private PDFRenderer pdfRenderer;

	public PDFPagesRender(File file) throws IOException {
		document = PDDocument.load(file);
		pdfRenderer = new PDFRenderer(document);
	}

	public Image renderPage(int page) throws IOException {

		return pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
		
	}

	public void close(){
		try{
			document.close();
		}catch(IOException e){ e.printStackTrace(); }
	}

	public int getNumberOfPages(){
		return document.getNumberOfPages();
	}

	public PDRectangle getPageSize(int page){
		return document.getPage(page).getBleedBox();
	}



}
