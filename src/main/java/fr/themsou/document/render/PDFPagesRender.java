package fr.themsou.document.render;

import java.awt.*;
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
				image = scale(pdfRenderer.renderImage(page, 3, ImageType.RGB), 1800);
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

	public static BufferedImage scale(BufferedImage img, double width) {

		if(img.getWidth() < width){
			return img;
		}

		int destWidth = (int) (width);
		int destHeight = (int) (img.getHeight() / ((double) img.getWidth()) * width);

		//créer l'image de destination
		GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage newImg = configuration.createCompatibleImage(destWidth, destHeight);

		Graphics2D graphics = newImg.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		//dessiner l'image de destination
		graphics.drawImage(img, 0, 0, destWidth, destHeight, 0, 0, img.getWidth(), img.getHeight(), null);
		graphics.dispose();

		return newImg;
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
