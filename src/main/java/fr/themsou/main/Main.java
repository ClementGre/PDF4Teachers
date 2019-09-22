package fr.themsou.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.*;

import fr.themsou.devices.Devices;
import fr.themsou.devices.FileDrop;
import fr.themsou.panel.Footerbar;
import fr.themsou.panel.LeftbarFiles;
import fr.themsou.panel.LeftbarNote;
import fr.themsou.panel.LeftbarPaint;
import fr.themsou.panel.LeftbarText;
import fr.themsou.panel.MainScreen;
import fr.themsou.panel.MenuBar;

public class Main{
	
	public static JFrame window;
	public static Devices devices = new Devices();
	public static Settings settings = new Settings();

	public static boolean click = false;
	
	public static JPanel panel = new JPanel();
	
//		MAIN
	
	public static MainScreen mainScreen = new MainScreen();
	public static JScrollPane mainScreenScroll = new JScrollPane(mainScreen);
	public static DropTarget mainScreenDrop = new DropTarget(mainScreen, new FileDrop(1));
	
//		LEFT BAR
	
	public static JTabbedPane leftBar = new JTabbedPane();

	public static LeftbarFiles leftBarFiles = new LeftbarFiles();
	public static JScrollPane leftBarFilesScroll = new JScrollPane(leftBarFiles);
	public static DropTarget leftBarFilesDrop = new DropTarget(leftBarFiles, new FileDrop(2));

	public static LeftbarText leftBarText = new LeftbarText();
	public static JScrollPane leftBarTextScroll = new JScrollPane(leftBarText);

	
//		FOOTER-HEADER BAR
	
	public static Footerbar footerBar = new Footerbar();
	public static MenuBar menuBar = new MenuBar();
	
	public static void main(String[] args){
		
//		WINDOW
		
		window = new JFrame("PDF Teacher - Aucun document");
		window.setSize(1200, 675);
		window.setMinimumSize(new Dimension(700, 393));
		window.setResizable(true);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainScreen.addMouseListener(devices);
		leftBarFiles.addMouseListener(devices);
		window.addMouseMotionListener(devices);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addKeyListener(devices);
		window.setContentPane(panel);

		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			if(mainScreen.document != null){
				if(mainScreen.document.save()){
					System.exit(0);
				}
			}else{
				System.exit(0);
			}
			}
		});

		try {
			//UIManager.setLookAndFeel("de.javasoft.synthetica.dark.SyntheticaDarkLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

//		MAIN
		
		mainScreenScroll.getVerticalScrollBar().setUnitIncrement(30);
		mainScreenScroll.setBorder(null);
		mainScreenDrop.setDefaultActions(DnDConstants.ACTION_COPY);
		
//		LEFT BAR
		
		/*leftBar.setUI(new BasicTabbedPaneUI(){
	        private final Insets borderInsets = new Insets(0, 0, 0, 0);
	        @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex){}
	        @Override protected Insets getContentBorderInsets(int tabPlacement){ return borderInsets; }
	    });*/
		//leftBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		leftBar.add(leftBarFilesScroll, new ImageIcon(Main.devices.getClass().getResource("/img/PDF-Document.png")));
		leftBar.add(leftBarTextScroll, new ImageIcon(Main.devices.getClass().getResource("/img/Text.png")));
		leftBar.add(new LeftbarNote(), new ImageIcon(Main.devices.getClass().getResource("/img/Note.png")));
        leftBar.add(new LeftbarPaint(), new ImageIcon(Main.devices.getClass().getResource("/img/Paint.png")));
		leftBar.setPreferredSize(new Dimension(230, leftBar.getHeight()));

		leftBarFilesDrop.setDefaultActions(DnDConstants.ACTION_COPY);
		leftBarFilesScroll.setBorder(null);
		leftBarFilesScroll.getVerticalScrollBar().setUnitIncrement(30);

		leftBarTextScroll.setBorder(null);
		leftBarTextScroll.getVerticalScrollBar().setUnitIncrement(30);
		leftBarText.setup();

		
//		FOOTER-HEADER BAR
		
		menuBar.setup();
		footerBar.setPreferredSize(new Dimension(footerBar.getWidth(), 20));
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		
//		PANEL
		
		panel.setLayout(new BorderLayout());
		panel.add("Center", mainScreenScroll);
		panel.add("North", menuBar);
		panel.add("South", footerBar);
		panel.add("West", leftBar);
		
		window.setSize(1200, 674);

		
//		RUN
		
		while(true){
			
			try{
				Thread.sleep(30);
			}catch(InterruptedException e){ e.printStackTrace(); }
			
			if(leftBar.getSelectedIndex() == 0){
				leftBarFiles.repaint();
			}else if(leftBar.getSelectedIndex() == 1){
				leftBarText.repaint();
			}
			mainScreen.repaint();

			
		}
		
		
	}
	
	public static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
    /*
    		while(true){
    			String input = JOptionPane.showInputDialog(null, "Entrez le nom de x", <Panel>);
				if(input == null){
					break; // Annuler
				}
				if(input.isEmpty()){
					JOptionPane.showMessageDialog(null, "Vous devez saisir un nom");
					continue; // Erreur
				}
				if(input != null){ // GOOD

					break;
				}
    		}

    		String[] values = new String[]{"1  580px/861px", "2  1160px/1722px", "3  1740px/2583px", "4  2320px/3444px", "5  29000px/4305px"};
    		String input = (String) JOptionPane.showInputDialog(null, "Choisissez une valeur", "NOM de la fenêtre", JOptionPane.QUESTION_MESSAGE, null, values, values[2]);
			if(input != null){ // GOOD

			}

    		int i = JOptionPane.showConfirmDialog(null, "Etes vous sur de vouloir supprimer ???");
			if(i == 0){ // YES

			}


     */


}
