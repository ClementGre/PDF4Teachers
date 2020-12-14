package fr.clementgre.pdf4teachers.document.render.display;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;

public class PDFPagesRender {

	private File file;

	public PDFPagesEditor editor;
	private PDDocument document;
	ArrayList<Thread> rendersPage = new ArrayList<>();

	public boolean advertisement = false;
	private boolean render = false;

	public PDFPagesRender(File file) throws IOException{
		this.file = file;

		render = true;
		document = PDDocument.load(file);
		editor = new PDFPagesEditor(document, file);
		render = false;
	}

	public void renderPage(int pageNumber, double size, double width, double height, CallBackArg<Background> callBack){

		Thread renderPage = new Thread(() -> {

			PDRectangle pageSize = getPageSize(pageNumber);

			int destWidth = (int) (595*1.4*size); // *1=595 | *1.5=892 |*2=1190
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

				graphics.dispose();
				Background background = new Background(
						Collections.singletonList(new BackgroundFill(
								javafx.scene.paint.Color.WHITE,
								CornerRadii.EMPTY,
								Insets.EMPTY)),
						Collections.singletonList(new BackgroundImage(
								SwingFXUtils.toFXImage(renderImage, null),
								BackgroundRepeat.NO_REPEAT,
								BackgroundRepeat.NO_REPEAT,
								BackgroundPosition.CENTER,
								new BackgroundSize(width, height, false, false, false, true))));

				Platform.runLater(() -> callBack.call(background));
			}catch(Exception e){
				e.printStackTrace();
				Platform.runLater(() -> callBack.call(null));
			}

			System.gc(); // clear unused element in RAM
			render = false;

		}, "Render page " + pageNumber);
		renderPage.start();
		rendersPage.add(renderPage);
	}
	public BufferedImage renderPageBasic(int pageNumber, int width, int height){

		PDRectangle pageSize = getPageSize(pageNumber);

		BufferedImage renderImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = renderImage.createGraphics();
		graphics.setBackground(Color.WHITE);

		try{
			PDDocument document = PDDocument.load(file);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			pdfRenderer.renderPageToGraphics(pageNumber, graphics, width/pageSize.getWidth(), width/pageSize.getWidth(), RenderDestination.VIEW);
			//scale(pdfRenderer.renderImage(page, 3, ImageType.RGB), 1800);
			document.close();
			graphics.dispose();

			return renderImage;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static void renderAdvertisement(){
		if(MainWindow.mainScreen.hasDocument(false)){
			if(!MainWindow.mainScreen.document.pdfPagesRender.advertisement){ // not already sended
				MainWindow.mainScreen.document.pdfPagesRender.advertisement = true;

				Alert alert = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("Erreur de rendu"));
				alert.setHeaderText(TR.tr("Des erreurs sont apparues lors du rendu du document PDF."));
				alert.setContentText(TR.tr("Certains caractères spéciaux (espaces insécables, signes spéciaux ou tabulations) risquent de ne pas s'afficher correctement."));
				alert.show();
			}
		}
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
	public PDDocument getDocument(){
		return document;
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
