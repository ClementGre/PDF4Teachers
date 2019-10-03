package fr.themsou.document.render;

import java.awt.*;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.main.Main;
import fr.themsou.utils.Hand;
import fr.themsou.utils.Location;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

public class EditRender {
	
	private int width;
	private int height;
	public Element current = null;
	public Hand hand = null;

	private Edition edition;




	public EditRender(Edition edition, int width, int height) {
		this.edition = edition;
		this.width = width;
		this.height = height;
	}

	public Image render(Image img, int page, int mouseX, int mouseY){

		javafx.scene.canvas.Canvas canvas = new Canvas(300, 250);
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.drawImage(javafx.scene.image.Image.impl_fromPlatformImage(img), 0, 0, img.getWidth(null), img.getHeight(null));

		if(Main.lbTextTab.elementToEdit != null && Main.lbTextTab.elementToEdit.getPage() == page){
			if(Main.lbTextTab.elementToEdit.getMargin() == null){
				Main.lbTextTab.elementToEdit.paint(g, mouseX, mouseY);
			}
			verifyLoc(this, Main.lbTextTab.elementToEdit);
		}

		for(int i = 0; i < edition.elements.size(); i++){
			if(edition.elements.get(i).getPage() == page){
				
				boolean on = edition.elements.get(i).paint(g, mouseX, mouseY);
				if(on){
					current = edition.elements.get(i);
				}
			}
		}
		if(!((mouseY > img.getHeight(null) && (page+1) != edition.document.rendered.length) || (mouseY < 0 && page != 0))){ // mouse on

			mouse(this, current, g, page, mouseX, mouseY);
			if(hand != null)
				hand.getElement().paint(g, mouseX, mouseY);
			if(edition.document.currentPage != page){
				edition.document.currentPage = page;
				//Main.footerBar.repaint();
			}
		}

		WritableImage image = canvas.snapshot(null, null);
		return SwingFXUtils.fromFXImage(image, null);
		
	}


	public void mouse(EditRender page, Element element, GraphicsContext g, int pageNumber, int mouseX, int mouseY){

		if(hand == null && Main.click && element != null){ // Ajouter

			hand = new Hand(element, element.getLocation().substractValues(new Location(mouseX, mouseY)), element.getPage());
			edition.removeElement(element);
			if(element instanceof TextElement)
				Main.lbTextTab.selectTextElement(element);

		}else if(hand != null){ // Déposer - Déplacer

			if(hand.getElement().getMargin() == null){
				hand.getElement().paint(g, mouseX, mouseY);
			}

			hand.setPage(pageNumber);
			hand.setLoc(new Location(mouseX, mouseY).additionValues(hand.getShift()));
			verifyLoc(page, hand.getElement());

			if(!Main.click){ // Déposer

				edition.addElement(hand.getElement());
				hand.getElement().paint(g, mouseX, mouseY);
				hand = null;
			}
		}
	}
	public void verifyLoc(EditRender page, Element element){

		Location minLoc = element.getLocation().substractValues(element.getMargin());
		Location maxLoc = element.getLocation().additionValues(element.getMargin());

		if(minLoc.getX() < 0) element.setX(element.getMargin().getX());
		if(maxLoc.getX() > page.getWidth()) element.setX(page.getWidth() - element.getMargin().getX());

		if(minLoc.getY() < 0) element.setY(element.getMargin().getY());
		if(maxLoc.getY() > page.getHeight()) element.setY(page.getHeight() - element.getMargin().getY());

	}


	public int getHeight(){
		return height;
	}
	public int getWidth(){
		return width;
	}

}
