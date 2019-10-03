package fr.themsou.panel;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import javafx.collections.FXCollections;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class LeftbarText extends StackPane {


	private ArrayList<TextElement> favorites = new ArrayList<>();
	private ArrayList<TextElement> lasts = new ArrayList<>();
	private int width = 0;
	private int height = 0;
	private int maxWidth = 0;

	private int currentTime = 0;
	private int current = -1;

	public TextElement elementToEdit;

	// Swing elements

	private ComboBox<String> fontsNames; String[] fontNames;
	private ComboBox<Integer> fontsSizes = new ComboBox<>(FXCollections.observableArrayList(6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30, 34, 38, 42));

	private TextField colorHex = new TextField("#000000");
	private Button colorBtn = new Button(""); // new ImageIcon(getClass().getResource("/img/MenuBar/settings.png"))

	private TextField comment = new TextField();
	private Button delete = new Button("Supprimer");
	private Button makeNew = new Button("Nouveau");

	private Canvas canvas;
	private GraphicsContext g;

	public LeftbarText(){
		canvas = new Canvas(300, 250);
		g = canvas.getGraphicsContext2D();
		getChildren().add(canvas);
		setup();
		drawShapes();
	}

	private void drawShapes() {
		g.setFill(Color.GREEN);
		g.setStroke(Color.BLUE);
		g.strokeLine(40, 10, 10, 40);
		g.fillOval(10, 60, 30, 30);

		boolean hasCurrent = false;

		g.setFill(Color.rgb(189, 195, 199));
		g.fillRect(0, 0, (int)getWidth(), (int)getHeight());

		/*int i;
		for(i = 0; i < files.size(); i++){

			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < Main.leftBarFilesScroll.getWidth()){

				if(current != i){
					current = i;
					currentTime = 0;
				}
				if(currentTime < 10) currentTime++;
				hasCurrent = true;

				g.setColor(new Color(127, 140, 141, currentTime*25));
				g.fillRect(0, i*30, getWidth(), 30);

				g.setColor(new Color(44, 62, 80));
				maxWidth = StringDrawing.centerString(g, 8 + (currentTime * 4), i*30, i*30+30, files.get(i).getName(), new Font("FreeSans", Font.PLAIN, 15))[0] + (currentTime * 4) + 8;

				g.drawImage(new ImageIcon(Main.devices.getClass().getResource("/img/FilesBar/supprimer.png")).getImage(), 5 - 40+(currentTime * 4), i*30+5, 20, 20, null);
				g.drawImage(new ImageIcon(Main.devices.getClass().getResource("/img/FilesBar/fermer.png")).getImage(), 28 - 40+(currentTime * 4), i*30+7, 16, 16, null);

			}else{
				g.setColor(new Color(44, 62, 80));
				maxWidth = StringDrawing.centerString(g, 8, i*30, i*30+30, files.get(i).getName(), new Font("FreeSans", Font.PLAIN, 15))[0] + 8;
			}


		}

		if(!hasCurrent) current = -1;

		if((maxWidth != width && (currentTime == 10 || !hasCurrent)) || i*30 != height){
			width = maxWidth;
			height = i*30;
			setPreferredSize(new Dimension(width + 10, height));
			Main.leftBarFilesScroll.updateUI();
		}*/

		if(elementToEdit == null){

			fontsNames.hide();
			fontsSizes.hide();
			/*colorHex.hide();
			colorBtn.hide();
			comment.hide();
			delete.hide();
			makeNew.setBounds(5, 5, 215, 30);*/

		}else{

			fontsNames.show();
			fontsSizes.show();
			/*colorHex.show();
			colorBtn.show();
			comment.show();
			delete.show();
			makeNew.setBounds(120, 95, 105, 30);*/

			Color color = Color.BLACK;
			try{
				color = Color.web(colorHex.getText());
			}catch(Exception ignored){}
			g.setFill(color);
			g.fillRect(90, 35, 80, 25);

			comment.setFont(new Font(fontNames[fontsNames.getSelectionModel().getSelectedIndex()], 14));
			elementToEdit.setFont(new Font(fontNames[fontsNames.getSelectionModel().getSelectedIndex()], (int) fontsSizes.getSelectionModel().getSelectedIndex()));
			elementToEdit.setColor(color);
			elementToEdit.setContent(comment.getText());
		}

	}

	public void setup(){


		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fontNames = ge.getAvailableFontFamilyNames();
		fontsNames = new ComboBox<>(FXCollections.observableArrayList(fontNames));
		//fontsNames.setBounds(5, 5, 165, 25);
		//fontsNames.setRenderer(new ComboBoxRenderar());
		//fontsNames.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		fontsNames.getSelectionModel().select("Arial");
		fontsNames.setButtonCell(new ListCell(){
			@Override
			protected void updateItem(Object item, boolean empty) {
				super.updateItem(item, empty);
				if(empty || item == null){
				}else{
					setStyle("-fx-font: " + item.toString() + ";");
				}
			}

		});

		//fontsSizes.setBounds(175, 5, 45, 25);
		//fontsSizes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		fontsSizes.getSelectionModel().select(7);
		getChildren().add(fontsSizes);

		//colorHex.setBounds(5, 35, 80, 25);
		getChildren().add(colorHex);

		//colorBtn.setBounds(175, 35, 45, 25);
		//colorBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getChildren().add(colorBtn);

		//comment.setBounds(5, 65, 215, 25);
		getChildren().add(comment);

		//delete.setBounds(5, 95, 105, 30);
		//delete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getChildren().add(delete);

		//makeNew.setBounds(125, 95, 105, 30);
		//makeNew.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		getChildren().add(makeNew);

		/*colorBtn.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){
			Color lstColor = Color.BLACK;
			try{
				lstColor = Color.decode(colorHex.getText());
			}catch(Exception ignored){}
			Color newColor = JColorChooser.showDialog(null, "Choissez une couleur", lstColor);
			if(newColor != null)
				colorHex.setText(toHexString(newColor));
		}});

		makeNew.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){

			if(Main.mainScreen.document != null){

				if(elementToEdit != null){
					if(comment.getText().isEmpty()) Main.mainScreen.document.edition.removeElement(elementToEdit);
				}

				elementToEdit = new TextElement(new Location(0, 0), 0, new Font("Arial", Font.PLAIN, 14), "", Color.BLACK);
				Main.mainScreen.document.edition.editRender.hand = new Hand(elementToEdit, new Location(0, 0), Main.mainScreen.document.currentPage);
				comment.setText("");
				comment.requestFocus();
			}else{
				JOptionPane.showMessageDialog(null, "Vous devez ouvrir un document");
			}

		}});

		delete.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){
			Main.mainScreen.document.edition.removeElement(elementToEdit);
			elementToEdit = null;
		}});*/

	}


	public void selectTextElement(Element element) {
		elementToEdit = (TextElement) element;

		fontsNames.getSelectionModel().select(((TextElement) element).getFont().getName());
		fontsSizes.getSelectionModel().select((int) ((TextElement) element).getFont().getSize());
		colorHex.setText(toHexString(((TextElement) element).getColor()));
		comment.setText(((TextElement) element).getContent());

	}



	public void openFile(File file){


	}
	public void openFiles(File[] files){


	}
	public void clearFiles(){

	}
	public void removeFile(int file){

	}


	public void mouseReleased(){

		int mouseX = MouseInfo.getPointerInfo().getLocation().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y;

		for(int i = 0; i < lasts.size(); i++){

			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < 0){

				if(mouseX > 7 && mouseX < 23){ // Add favorites

				}else if(mouseX > 28 && mouseX < 44){ // Remove

				}else if(mouseY > i*30 && mouseY < i*30+30){ // Add

				}

			}



		}
	}

	private String toHexString(Color colour) throws NullPointerException {
		String hexColour = String.format("#%02x%02x%02x", (int)colour.getRed(), (int)colour.getGreen(), (int)colour.getBlue());
		if (hexColour.length() < 6) {
			hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
		}
		return "#" + hexColour;
	}

	
}
