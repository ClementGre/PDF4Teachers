package fr.themsou.document.editions.elements;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.panel.leftBar.texts.TextTreeView;
import fr.themsou.panel.leftBar.texts.TreeViewSections.TextTreeSection;
import fr.themsou.utils.*;
import fr.themsou.utils.components.NodeMenuItem;
import fr.themsou.utils.components.ScratchText;
import fr.themsou.windows.MainWindow;
import fr.themsou.yaml.Config;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class TextElement extends Element {

	private ScratchText text = new ScratchText();
	private ImageView image = new ImageView();

	public static final float imageFactor = 2.5f;

	public TextElement(int x, int y, int pageNumber, Font font, String text, Color color, boolean hasPage){
		super(x, y, pageNumber);

		this.text.setFont(font);
		this.text.setFill(color);
		this.text.setText(text);

		this.text.setBoundsType(TextBoundsType.LOGICAL);
		this.text.setTextOrigin(VPos.TOP);

		if(hasPage && getPage() != null){
			setupGeneral(isLaTeX() ? this.image : this.text);
			updateLaTeX();
		}

	}

	// SETUP / EVENT CALL BACK

	@Override
	protected void setupBindings(){
		this.text.textProperty().addListener((observable, oldValue, newValue) -> {
			updateLaTeX();
			checkLocation(getLayoutX(), getLayoutY(), false);
		});
		this.text.fillProperty().addListener((observable, oldValue, newValue) -> {
			updateLaTeX();
		});
		this.text.fontProperty().addListener((observable, oldValue, newValue) -> {
			updateLaTeX();
		});
	}
	@Override
	protected void onMouseRelease(){
		MainWindow.lbTextTab.treeView.onFileSection.sortManager.simulateCall();
	}
	@Override
	protected void setupMenu(){

		NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Supprimer"), false);
		item1.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
		item1.setToolTip(TR.tr("Supprime cet élément. Il sera donc retiré de l'édition."));
		NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Dupliquer"), false);
		item2.setToolTip(TR.tr("Crée un second élément identique à celui-ci."));
		NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.tr("Ajouter aux éléments précédents"), false);
		item3.setToolTip(TR.tr("Ajoute cet élément à la liste des éléments précédents."));
		NodeMenuItem item4 = new NodeMenuItem(new HBox(), TR.tr("Ajouter aux éléments Favoris"), false);
		item4.setToolTip(TR.tr("Ajoute cet élément à la liste des éléments favoris."));
		menu.getItems().addAll(item1, item2, item4, item3);
		NodeMenuItem.setupMenu(menu);

		item1.setOnAction(e -> delete());
		item2.setOnAction(e -> cloneOnDocument());
		item3.setOnAction(e -> TextTreeView.addSavedElement(this.toNoDisplayTextElement(TextTreeSection.LAST_TYPE, true)));
		item4.setOnAction(e -> TextTreeView.addSavedElement(this.toNoDisplayTextElement(TextTreeSection.FAVORITE_TYPE, true)));
	}

	// ACTIONS

	@Override
	public void select(){
		super.selectPartial();
		MainWindow.leftBar.getSelectionModel().select(1);
		MainWindow.lbTextTab.selectItem();
	}

	// READER AND WRITERS

	@Override
	public LinkedHashMap<Object, Object> getYAMLData(){
		LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
		data.put("color", text.getFill().toString());
		data.put("font", text.getFont().getFamily());
		data.put("size", text.getFont().getSize());
		data.put("bold", FontUtils.getFontWeight(text.getFont()) == FontWeight.BOLD);
		data.put("italic", FontUtils.getFontPosture(text.getFont()) == FontPosture.ITALIC);
		data.put("text", text.getText());

		return data;
	}
	public static void readYAMLDataAndCreate(HashMap<String, Object> data, int page){
		TextElement element = readYAMLDataAndGive(data, true, page);
		if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
			MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElementSimple(element);
	}
	public static TextElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage, int page){

		int x = (int) Config.getLong(data, "x");
		int y = (int) Config.getLong(data, "y");
		double fontSize = Config.getDouble(data, "size");
		boolean isBold = Config.getBoolean(data, "bold");
		boolean isItalic = Config.getBoolean(data, "italic");
		String fontName = Config.getString(data, "font");
		Color color = Color.valueOf(Config.getString(data, "color"));
		String text = Config.getString(data, "text");

		Font font = FontUtils.getFont(fontName, isBold, isItalic, (int) fontSize);
		return new TextElement(x, y, page, font, text, color, hasPage);
	}

	public static TextElement readDataAndGive(DataInputStream reader, boolean hasPage) throws IOException {

		byte page = reader.readByte();
		short x = reader.readShort();
		short y = reader.readShort();
		double fontSize = reader.readFloat();
		boolean isBold = reader.readBoolean();
		boolean isItalic = reader.readBoolean();
		String fontName = reader.readUTF();
		short colorRed = (short) (reader.readByte() + 128);
		short colorGreen = (short) (reader.readByte() + 128);
		short colorBlue = (short) (reader.readByte() + 128);
		String text = reader.readUTF();

		Font font = FontUtils.getFont(fontName, isItalic, isBold, (int) fontSize);
		return new TextElement(x, y, page, font, text, Color.rgb(colorRed, colorGreen, colorBlue), hasPage);
	}
	public static void readDataAndCreate(DataInputStream reader) throws IOException {
		TextElement element = readDataAndGive(reader, true);
		element.setRealY((int) (element.getRealY() - element.getBaseLineY()/element.getPage().getHeight()*Element.GRID_HEIGHT));
		if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
			MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElementSimple(element);
	}

	// SPECIFIC METHODS

	public float getBaseLineY(){
		return (float) (text.getBaselineOffset());
	}
	@Override
	public float getAlwaysHeight(){
		return (float) text.getLayoutBounds().getHeight();
	}

	public boolean isLaTeX(){
		return text.getText().startsWith("$");
	}
	public String getLaTeXText(){
		return text.getText().replaceFirst(Pattern.quote("$"), "");
	}
	public java.awt.Color getAwtColor(){
		return new java.awt.Color((float) getColor().getRed(),
				(float) getColor().getGreen(),
				(float) getColor().getBlue(),
				(float) getColor().getOpacity());
	}
	public void updateLaTeX(){
		if(isLaTeX()){ // LaTeX

			if(getChildren().contains(text)){
				getChildren().remove(text);
				getChildren().add(image);
			}
			Image render = renderLatex();
			image.setImage(render);
			image.setVisible(true);
			image.setFitWidth(render.getWidth()/imageFactor);
			image.setFitHeight(render.getHeight()/imageFactor);

		}else{ // Lambda Text

			text.setVisible(true);

			if(getChildren().contains(image)){
				getChildren().remove(image);
				getChildren().add(text);
				image.setImage(null);
			}
		}
	}

	public WritableImage renderLatex(){

		BufferedImage render = renderAwtLatex();
		return SwingFXUtils.toFXImage(render, new WritableImage(render.getWidth(null), render.getHeight(null)));
	}
	public BufferedImage renderAwtLatex(){
		return renderLatex(getLaTeXText(), getAwtColor(), (int) getFont().getSize());
	}

	public static BufferedImage renderLatex(String text, java.awt.Color color, int size){
		try {

			TeXFormula formula = new TeXFormula(text);
			formula.setColor(color);

			TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, size*imageFactor);
			icon.setInsets(new Insets((int) (-size*imageFactor/7), (int) (-size*imageFactor/7), (int) (-size*imageFactor/7), (int) (-size*imageFactor/7)));

			BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.setBackground(new java.awt.Color(0f, 0f, 0f, 1f));
			icon.paintIcon(null, g, 0, 0);

			return image;

		}catch(ParseException ex){
			if(ex.getMessage().contains("Unknown symbol or command or predefined TeXFormula: ")){
				return renderLatex("$" + TR.tr("Commande/Symbole~inconnu~:") + "\\\\" +
						ex.getMessage().replaceAll(Pattern.quote("Unknown symbol or command or predefined TeXFormula:"), ""), color, size);
			}else if(text.startsWith(TR.tr("Erreur~:") + "\\\\")){
				return renderLatex(TR.tr("Impossible~de~lire~la~formule"), color, size);
			}else{
				return renderLatex(TR.tr("Erreur~:") + "\\\\" + ex.getMessage(), color, size);
			}
		}
	}

	// ELEMENT DATA GETTERS AND SETTERS

	public String getText(){
		return text.getText();
	}
	public StringProperty textProperty(){
		return text.textProperty();
	}
	public void setText(String text){
		this.text.setText(text);
	}
	public void setColor(Color color){
		this.text.setFill(color);
	}
	public ObjectProperty<Paint> fillProperty(){
		return text.fillProperty();
	}
	public Color getColor(){
		return (Color) text.getFill();
	}
	public void setFont(Font font){
		text.setFont(font);
	}
	public ObjectProperty<Font> fontProperty(){
		return text.fontProperty();
	}
	public Font getFont(){
		return text.getFont();
	}

	// TRANSFORMATIONS

	@Override
	public Element clone(){
		return new TextElement(getRealX(), getRealY(), pageNumber, text.getFont(), text.getText(), (Color) text.getFill(), true);
	}
	public TextTreeItem toNoDisplayTextElement(int type, boolean hasCore){
		if(hasCore) return new TextTreeItem(text.getFont(), text.getText(), (Color) text.getFill(), type, 0, System.currentTimeMillis()/1000, this);
		else return new TextTreeItem(text.getFont(), text.getText(), (Color) text.getFill(), type, 0, System.currentTimeMillis()/1000);
	}

}