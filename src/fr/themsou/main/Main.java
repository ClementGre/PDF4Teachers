package fr.themsou.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fr.themsou.devices.Devices;
import fr.themsou.devices.FileDrop;
import fr.themsou.panel.Footerbar;
import fr.themsou.panel.Leftbar;
import fr.themsou.panel.Mainscreen;

public class Main{
	
	public static JFrame fenetre;
	public static Devices devices = new Devices();

	public static Color currentColor = Color.GREEN;
	public static boolean click = false;
	public static File addFile = null;
	
	public static JPanel panel = new JPanel();
	public static Mainscreen mainscreen = new Mainscreen();
	
	public static Footerbar footerBar = new Footerbar();
	public static JScrollPane sPaneMain = new JScrollPane(mainscreen);
	public static JScrollPane sPaneLeft = new JScrollPane(new Leftbar());

	public static DropTarget fileDrop = new DropTarget(panel, new FileDrop());

	public static void main(String[] args){
		
		fileDrop.setActive(false);
		fileDrop.setDefaultActions(DnDConstants.ACTION_COPY);
		
		fenetre = new JFrame("PDF Marker");
		fenetre.setSize(1200, 675);
		fenetre.setMinimumSize(new Dimension(700, 393));
		fenetre.setResizable(true);
		fenetre.setLocationRelativeTo(null);
		fenetre.setVisible(true);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fenetre.addMouseListener(devices);
		fenetre.addMouseWheelListener(devices);
		fenetre.addKeyListener(devices);
		fenetre.setContentPane(panel);
		
		panel.setLayout(new BorderLayout());
		panel.add("Center", sPaneMain);
		panel.add("South", footerBar);
		panel.add("West", sPaneLeft);
		
		sPaneMain.addMouseListener(devices);
		sPaneMain.addMouseWheelListener(devices);
		sPaneMain.addKeyListener(devices);
		
		sPaneLeft.setPreferredSize(new Dimension(200, sPaneLeft.getHeight()));
		footerBar.setPreferredSize(new Dimension(footerBar.getWidth(), 20));
		fenetre.setSize(1200, 674);
		
		fenetre.addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent componentEvent) {
		    }
		});
		
		mainscreen.openFile(new File("/home/clement/Téléchargements/test-1.pdf"));
		
		/*int reload = 10;
		while(true){
			
			try{
				Thread.sleep(20);
			}catch(InterruptedException e){ e.printStackTrace(); }
			
			
			if(reload > 0){
				reload --;
			}else if(reload == 0){
				reload --;
			}
			
		}*/
		
		
	}

}
