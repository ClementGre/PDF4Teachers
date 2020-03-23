package fr.themsou.document.editions.elements;

import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.LeftBar.LBTextTreeView;
import fr.themsou.utils.TextWrapper;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NoDisplayTextElement extends TreeItem{

	private ObjectProperty<Font> font = new SimpleObjectProperty<>();
	private String text;
	private ObjectProperty<Color> color = new SimpleObjectProperty<>();

	private int type;
	private long uses;
	private long creationDate;

	private TextElement core = null;

	public static final int FAVORITE_TYPE = 1;
	public static final int LAST_TYPE = 2;
	public static final int ONFILE_TYPE = 3;

	private NoDisplayTextElement thisObject = this;

	public NoDisplayTextElement(Font font, String text, Color color, int type, long uses, long creationDate) {
		this.font.set(font);
		this.text = text;
		this.color.set(color);
		this.type = type;
		this.uses = uses;
		this.creationDate = creationDate;
	}
	public NoDisplayTextElement(Font font, String text, Color color, int type, long uses, long creationDate, TextElement core) {
		this.font.set(font);
		this.text = text;
		this.color.set(color);
		this.type = type;
		this.uses = uses;
		this.creationDate = creationDate;
		this.core = core;

		// update le graphic quand le text est modifié
		core.textProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				setText(newValue);
				updateGraphic(findCellByItem(thisObject, Main.lbTextTab.treeView));
			}
		});
		// BIND la couleur et le font
		fontProperty().bind(core.fontProperty());
		core.fillProperty().addListener(new ChangeListener<Paint>() {
			@Override public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
				setColor((Color) newValue);
			}
		});

		if(type == NoDisplayTextElement.ONFILE_TYPE){
			core.setOnMouseReleased(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent event) {
					if(event.getButton() == MouseButton.PRIMARY){
						if(Main.lbTextTab.onFileTextSortManager.getSelectedButton().getText().equals("Position")){
							Main.lbTextTab.onFileTextSortManager.simulateCall();
						}
					}
				}
			});
		}
	}

	public void updateGraphic(TreeCell<String> cell){

		if(cell == null){
			System.out.println("Error : Cell is null");
			return;
		}

		cell.setStyle(null);
		cell.setStyle("-fx-padding: 3 -10;");

		int maxWidth = (int) (Main.lbTextTab.treeView.getWidth() + 20);
		if(maxWidth < 0) return;

		Text name = new Text(new TextWrapper(getText(), getFont(), maxWidth).wrap());

		name.fillProperty().bind(colorProperty());
		name.fontProperty().bind(Bindings.createObjectBinding(() -> TextElement.getFont(getFont().getFamily(), false, false, 14), fontProperty()));

		cell.setGraphic(name);

		cell.setContextMenu(LBTextTreeView.getNewMenu(this));

		cell.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent mouseEvent){
				if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
					addToDocument();
					if(getType() == NoDisplayTextElement.FAVORITE_TYPE){
						Main.lbTextTab.favoritesTextSortManager.simulateCall();
					}else if(getType() == NoDisplayTextElement.LAST_TYPE){
						Main.lbTextTab.lastsTextSortManager.simulateCall();
					}
				}
			}
		});

	}

	public static TreeCell<String> findCellByItem(TreeItem treeItem, TreeView treeView) {
		return (TreeCell<String>) treeView.lookupAll(".tree-cell").stream()
				.filter(n -> ((TreeCell) n).getTreeItem() == treeItem)
				.findFirst()
				.orElse(null);
	}

	@Override
	public boolean equals(Object v){

		if(v instanceof NoDisplayTextElement){
			NoDisplayTextElement element = (NoDisplayTextElement) v;
			if(element.type == type && element.text.equals(text) && element.color.hashCode() == color.hashCode()){
				if(element.font.get().getStyle().equals(font.get().getStyle()) && element.font.get().getSize() == font.get().getSize() && element.getFont().getFamily().equals(font.get().getFamily())){
					return true;
				}
			}
		}
		return false;
	}

	public void writeData(DataOutputStream writer) throws IOException {

		writer.writeFloat((float) font.get().getSize());
		writer.writeBoolean(TextElement.getFontWeight(font.get()) == FontWeight.BOLD);
		writer.writeBoolean(TextElement.getFontPosture(font.get()) == FontPosture.ITALIC);
		writer.writeUTF(font.get().getFamily());
		writer.writeByte((int) (color.get().getRed() * 255.0 - 128));
		writer.writeByte((int) (color.get().getGreen() * 255.0 - 128));
		writer.writeByte((int) (color.get().getBlue() * 255.0 - 128));
		writer.writeLong(uses);
		writer.writeLong(creationDate);
		writer.writeUTF(text);
	}
	public TextElement toRealTextElement(int x, int y, int page){
		return new TextElement(x, y, font.get(), text, color.get(), page, Main.mainScreen.document.pages.get(page));
	}
	public static NoDisplayTextElement readDataAndGive(DataInputStream reader, int type) throws IOException {

		double fontSize = reader.readFloat();
		boolean isBold = reader.readBoolean();
		boolean isItalic = reader.readBoolean();
		String fontName = reader.readUTF();
		short colorRed = (short) (reader.readByte() + 128);
		short colorGreen = (short) (reader.readByte() + 128);
		short colorBlue = (short) (reader.readByte() + 128);
		long uses = reader.readLong();
		long creationDate = reader.readLong();
		String text = reader.readUTF();

		Font font = TextElement.getFont(fontName, isBold, isItalic, (int) fontSize);

		return new NoDisplayTextElement(font, text, Color.rgb(colorRed, colorGreen, colorBlue), type, uses, creationDate);

	}

	public void addToDocument(){

		if(Main.mainScreen.hasDocument(false)){
			uses++;
			PageRenderer page = Main.mainScreen.document.pages.get(0);
			if (Main.mainScreen.document.getCurrentPage() != -1)
				page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

			TextElement realElement = toRealTextElement(30, (int) (page.mouseY * 800 / page.getHeight()), page.getPage());
			page.addElement(realElement, true);
			Main.mainScreen.selectedProperty().setValue(realElement);
		}

	}

	public Font getFont() {
		return font.get();
	}
	public ObjectProperty<Font> fontProperty() {
		return font;
	}
	public void setFont(Font font) {
		this.font.set(font);
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Color getColor() {
		return color.get();
	}
	public ObjectProperty<Color> colorProperty() {
		return color;
	}
	public void setColor(Color color) {
		this.color.set(color);
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getUses() {
		return uses;
	}
	public void setUses(long uses) {
		this.uses = uses;
	}
	public long getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}
	public TextElement getCore() {
		return core;
	}
	public void setCore(TextElement core) {
		this.core = core;
	}
}