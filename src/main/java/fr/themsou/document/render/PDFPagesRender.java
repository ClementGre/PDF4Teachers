package fr.themsou.document.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import fr.themsou.utils.CallBack;
import javafx.application.Platform;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PDFPagesRender {

	private PDDocument document;
	private PDFRenderer pdfRenderer;

	private boolean render = false;
	private boolean close = false;

	public PDFPagesRender(File file) throws IOException {
		document = PDDocument.load(file);
		pdfRenderer = new PDFRenderer(document);
	}

	public void renderPage(int page, CallBack<BufferedImage> callBack){

		new Thread(() -> {
			while(render){
				try{
					Thread.sleep(500);
				}catch(InterruptedException e){
					e.printStackTrace();
					callBack.call(null);
					return;
				}
			}

			render = true;
			BufferedImage image = null;
			try{
				if(close) return;
				image = pdfRenderer.renderImage(page, 3, ImageType.RGB);
				if(close) return;
			}catch(Exception e){
				e.printStackTrace();
			}
			render = false;

			final BufferedImage finalImage = image;
			Platform.runLater(() -> {
				callBack.call(finalImage);
			});

		}, "Render page " + page).start();

	}

	public void close(){
		close = true;
		new Thread(() -> {
			try{
				Thread.sleep(5000);
				document.close();
			}catch(IOException | InterruptedException e){ e.printStackTrace(); }
		}, "Document closer").start();
	}

	public int getNumberOfPages(){
		return document.getNumberOfPages();
	}

	public PDRectangle getPageSize(int page){
		if(document.getPage(page).getRotation() == 90 || document.getPage(page).getRotation() == 270){
			return new PDRectangle(document.getPage(page).getBleedBox().getHeight(), document.getPage(page).getBleedBox().getWidth());
		}
		return document.getPage(page).getBleedBox();
	}



}
