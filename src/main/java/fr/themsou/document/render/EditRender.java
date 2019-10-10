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
}
