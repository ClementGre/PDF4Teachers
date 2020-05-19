package fr.themsou.document.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.themsou.utils.CallBack;
import javafx.application.Platform;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;

import javax.imageio.ImageIO;

public class PDFPagesRender {

	private File file;

	private PDDocument document;
	ArrayList<Thread> rendersPage = new ArrayList<>();

	private boolean render = false;

	public PDFPagesRender(File file) throws IOException{
		this.file = file;

		render = true;
		document = PDDocument.load(file);
		render = false;
	}

	public void renderPage(int pageNumber, CallBack<BufferedImage> callBack){

		Thread renderPage = new Thread(() -> {
			/*while(render){
				try{
					Thread.sleep(500);
				}catch(InterruptedException e){
					e.printStackTrace();
					callBack.call(null);
					return;
				}
			}
			render = true;*/

			PDRectangle pageSize = getPageSize(pageNumber);

			int destWidth = 1190; // *1=595 | *1.5=892 |*2=1190
			int destHeight = (int) (pageSize.getHeight() / pageSize.getWidth() * ((double)destWidth));

			BufferedImage renderImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = renderImage.createGraphics();
			graphics.setBackground(Color.WHITE);

			try{
				PDDocument document = PDDocument.load(file);
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				pdfRenderer.renderPageToGraphics(pageNumber, graphics, destWidth/pageSize.getWidth(), destWidth/pageSize.getWidth(), RenderDestination.VIEW);
				//scale(pdfRenderer.renderImage(page, 3, ImageType.RGB), 1800);
				document.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			graphics.dispose();
			Platform.runLater(() -> callBack.call(renderImage));

			render = false;

		}, "Render page " + pageNumber);
		renderPage.start();
		rendersPage.add(renderPage);

	}

	public BufferedImage scale(BufferedImage img, double width) {

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
		try {
			document.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public int getNumberOfPages(){
		return document.getNumberOfPages();
	}
	public PDRectangle getPageSize(int pageNumber){

		return getPageCropBox(pageNumber);
		/*PDPage page = document.getPage(pageNumber);
		PDRectangle pageSize;
		if(page.getRotation() == 90 || page.getRotation() == 270) pageSize = new PDRectangle(page.getBleedBox().getHeight(), page.getBleedBox().getWidth());
		else pageSize = page.getBleedBox();

		return pageSize;*/
	}
	public PDRectangle getPageCropBox(int pageNumber){
		PDPage page = document.getPage(pageNumber);
		PDRectangle pageSize;
		if(page.getRotation() == 90 || page.getRotation() == 270) pageSize = new PDRectangle(page.getCropBox().getHeight(), page.getCropBox().getWidth());
		else pageSize = page.getCropBox();

		return pageSize;
	}
}
