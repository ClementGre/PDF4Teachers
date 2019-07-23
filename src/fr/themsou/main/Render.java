package fr.themsou.main;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class Render {
	
	@SuppressWarnings("unused")
	public Image[] render(File file){
		
		try{
			
			System.out.println("Start render");
			long time = System.currentTimeMillis();
			
			PDDocument doc = PDDocument.load(file);
			PDFRenderer pdfRenderer = new PDFRenderer(doc);
			
			int pageCounter = 0;
			Image[] images = new Image[doc.getNumberOfPages()];
			
			for(PDPage page : doc.getPages()){
			    BufferedImage bimg = pdfRenderer.renderImageWithDPI(pageCounter, 200, ImageType.RGB);
			    images[pageCounter] = (Image) bimg;
			    pageCounter ++;
			}
			doc.close();
			
			System.out.println("-> Finished in " + (System.currentTimeMillis() - time) + "ms");
			
			return images;
			
		}catch(Exception e){
			System.out.println(e.getMessage());
			return null;
		}
		
		
		
	}

}
