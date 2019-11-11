package fr.themsou.document.render;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import fr.themsou.main.Main;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PDFPagesRender {
	
	public Image[] render(File file){
		
		try{
			
			PDDocument doc = PDDocument.load(file);
				
			System.out.println("Start render");
			long time = System.currentTimeMillis();
			
			PDFRenderer pdfRenderer = new PDFRenderer(doc);

			int pageCounter = 0;
			Image[] images = new Image[doc.getNumberOfPages()];
			
			for(@SuppressWarnings("unused") PDPage page : doc.getPages()){

			    BufferedImage bimg = pdfRenderer.renderImageWithDPI(pageCounter, 150, ImageType.RGB);

			    images[pageCounter] = bimg;
			    pageCounter ++;
			}
			doc.close();
			
			System.out.println("-> Finished in " + (System.currentTimeMillis() - time) + "ms");
			
			return images;
			
		}catch(Exception | NoSuchMethodError e){
			System.out.println(e.getMessage());
			Main.mainScreen.setStatus(2);
			return null;
		}
		
	}

}
