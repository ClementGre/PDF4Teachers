package fr.themsou.panel;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.main.Main;
import fr.themsou.utils.Hand;
import fr.themsou.utils.Location;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class LeftbarText extends JPanel {


	private ArrayList<TextElement> favorites = new ArrayList<>();
	private ArrayList<TextElement> lasts = new ArrayList<>();
	private int width = 0;
	private int height = 0;
	private int maxWidth = 0;

	private int currentTime = 0;
	private int current = -1;

	public TextElement elementToEdit;

	// Swing elements

	private JComboBox<String> fontsNames; String[] fontNames;
	private JComboBox<Integer> fontsSizes = new JComboBox<>(new Integer[]{6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30, 34, 38, 42});

	private JTextField colorHex = new JTextField("#000000", 7);
	private JButton colorBtn = new JButton(new ImageIcon(getClass().getResource("/img/MenuBar/settings.png")));

	private JTextField comment = new JTextField(256);
	private JButton delete = new JButton("Supprimer");
	private JButton makeNew = new JButton("Nouveau");



	public void paintComponent(Graphics go){

		Main.footerBar.repaint();

		boolean hasCurrent = false;

		setBorder(null);
		int mouseX = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;
		Graphics2D g = (Graphics2D) go;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setColor(new Color(189, 195, 199));
		g.fillRect(0, 0, getWidth(), getHeight());



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
			colorHex.hide();
			colorBtn.hide();
			comment.hide();
			delete.hide();
			makeNew.setBounds(5, 5, 215, 30);

		}else{

			fontsNames.show();
			fontsSizes.show();
			colorHex.show();
			colorBtn.show();
			comment.show();
			delete.show();
			makeNew.setBounds(120, 95, 105, 30);

			Color color = Color.BLACK;
			try{
				color = Color.decode(colorHex.getText());
			}catch(Exception ignored){}
			g.setColor(color);
			g.fillRect(90, 35, 80, 25);

			comment.setFont(new Font(fontNames[fontsNames.getSelectedIndex()], Font.PLAIN, 14));
			elementToEdit.setFont(new Font(fontNames[fontsNames.getSelectedIndex()], Font.PLAIN, (int) fontsSizes.getSelectedItem()));
			elementToEdit.setColor(color);
			elementToEdit.setContent(comment.getText());
		}

	}

	public void setup(){
		setLayout(null);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fontNames = ge.getAvailableFontFamilyNames();
		fontsNames = new JComboBox<>(fontNames);
		fontsNames.setBounds(5, 5, 165, 25);
		fontsNames.setRenderer(new ComboBoxRenderar());
		fontsNames.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		fontsNames.setSelectedItem("Arial");
		add(fontsNames);

		fontsSizes.setBounds(175, 5, 45, 25);
		fontsSizes.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		fontsSizes.setSelectedIndex(7);
		add(fontsSizes);

		colorHex.setBounds(5, 35, 80, 25);
		add(colorHex);

		colorBtn.setBounds(175, 35, 45, 25);
		colorBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		add(colorBtn);

		comment.setBounds(5, 65, 215, 25);
		add(comment);

		delete.setBounds(5, 95, 105, 30);
		delete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		add(delete);

		makeNew.setBounds(125, 95, 105, 30);
		makeNew.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		add(makeNew);

		colorBtn.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e){
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
		}});

	}


	public void selectTextElement(Element element) {
		elementToEdit = (TextElement) element;

		fontsNames.setSelectedItem(((TextElement) element).getFont().getName());
		fontsSizes.setSelectedItem(((TextElement) element).getFont().getSize());
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

		int mouseX = MouseInfo.getPointerInfo().getLocation().x - getLocationOnScreen().x;
		int mouseY = MouseInfo.getPointerInfo().getLocation().y - getLocationOnScreen().y;

		for(int i = 0; i < lasts.size(); i++){

			if(mouseY > i*30 && mouseY < i*30+30 && mouseX > 0 && mouseX < Main.leftBarFilesScroll.getWidth()){

				if(mouseX > 7 && mouseX < 23){ // Add favorites

				}else if(mouseX > 28 && mouseX < 44){ // Remove

				}else if(mouseY > i*30 && mouseY < i*30+30){ // Add

				}

			}



		}
	}

	private String toHexString(Color colour) throws NullPointerException {
		String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
		if (hexColour.length() < 6) {
			hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
		}
		return "#" + hexColour;
	}

	public class ComboBoxRenderar extends JLabel implements ListCellRenderer{

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
			setText((String) value);
			setFont(new Font((String) value, Font.PLAIN, 15));
			return this;
		}
	}

	
}
