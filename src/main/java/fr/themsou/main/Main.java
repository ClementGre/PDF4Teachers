package fr.themsou.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
		window.addKeyListener(devices);
		window.setContentPane(panel);
		
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
		leftBar.add(new LeftbarText(), new ImageIcon(Main.devices.getClass().getResource("/img/Text.png")));
		leftBar.add(new LeftbarNote(), new ImageIcon(Main.devices.getClass().getResource("/img/Note.png")));
        leftBar.add(new LeftbarPaint(), new ImageIcon(Main.devices.getClass().getResource("/img/Paint.png")));
		leftBar.setPreferredSize(new Dimension(230, leftBar.getHeight()));
		leftBarFilesDrop.setDefaultActions(DnDConstants.ACTION_COPY);
		leftBarFilesScroll.setBorder(null);
		leftBarFilesScroll.getVerticalScrollBar().setUnitIncrement(30);
		
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
				mainScreen.repaint();
			}
			
		}
		
		
	}
	
	public static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }


}
