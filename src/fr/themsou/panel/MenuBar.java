package fr.themsou.panel;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import fr.themsou.main.Main;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar{
	
	
	public void setup(){
		
		JMenu menu1 = new JMenu("Fichier");
		JMenuItem menu1arg1 = new JMenuItem("Ouvrir     ");
		menu1arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/ouvrir.png")));
		menu1arg1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK, true));
		JMenuItem menu1arg2 = new JMenuItem("Fermer le fichier     ");
		menu1arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/fermer.png")));
		menu1arg2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK, true));
		JMenuItem menu1arg3 = new JMenuItem("Vider la liste     ");
		menu1arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/vider.png")));
		menu1arg3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK | Event.SHIFT_MASK, true));
		
		JMenuItem menu1arg4 = new JMenuItem("Sauvegarder l'édition     ");
		menu1arg4.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/sauvegarder.png")));
		menu1arg4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK, true));
		JMenu menu1arg5 = new JMenu("Éditions du même nom     ");
		menu1arg5.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/memeNom.png")));
		JMenuItem menu1arg6 = new JMenuItem("Supprimer l'édition     ");
		menu1arg6.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/supprimer.png")));
		menu1arg6.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.CTRL_MASK, true));
		
		JMenuItem menu1arg7 = new JMenuItem("Exporter     ");
		menu1arg7.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/exporter.png")));
		menu1arg7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK, true));
		JMenuItem menu1arg8 = new JMenuItem("Tout exporter     ");
		menu1arg8.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/exporter.png")));
		menu1arg8.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK | Event.SHIFT_MASK, true));
		
		Main.menuBar.add(menu1);
		menu1.add(menu1arg1);
		menu1.add(menu1arg2);
		menu1.add(menu1arg3);
		menu1.addSeparator();
		menu1.add(menu1arg4);
		menu1.add(menu1arg5);
		menu1.add(menu1arg6);
		menu1.addSeparator();
		menu1.add(menu1arg7);
		menu1.add(menu1arg8);
		
		
		
		JMenu menu2 = new JMenu("Édition");
		JMenuItem menu2arg1 = new JMenuItem(" Annuler     ");
		menu2arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/annuler.png")));
		menu2arg1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK, true));
		JMenuItem menu2arg2 = new JMenuItem(" Rétablir     ");
		menu2arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/retablir.png")));
		menu2arg2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK, true));
		
		JMenuItem menu2arg3 = new JMenuItem(" Couper     ");
		menu2arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/couper.png")));
		menu2arg3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK, true));
		JMenuItem menu2arg4 = new JMenuItem(" Copier     ");
		menu2arg4.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/copier.png")));
		menu2arg4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK, true));
		JMenuItem menu2arg5 = new JMenuItem(" Coller     ");
		menu2arg5.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/coller.png")));
		menu2arg5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK, true));
		
		Main.menuBar.add(menu2);
		menu2.add(menu2arg1);
		menu2.add(menu2arg2);
		menu2.addSeparator();
		menu2.add(menu2arg3);
		menu2.add(menu2arg4);
		menu2.add(menu2arg5);
		
		
		
		JMenu menu3 = new JMenu("Préférences");
		
		JMenu menu3arg1 = new JMenu("Zoom par défaut     ");
		menu3arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/zoom.png")));
		JMenuItem menu3arg1arg1 = new JMenuItem("100%     ");
		JMenuItem menu3arg1arg2 = new JMenuItem("125%     ");
		JMenuItem menu3arg1arg3 = new JMenuItem("150%     ");
		JMenuItem menu3arg1arg4 = new JMenuItem("175%     ");
		menu3arg1.add(menu3arg1arg1);
		menu3arg1.add(menu3arg1arg2);
		menu3arg1.add(menu3arg1arg3);
		menu3arg1.add(menu3arg1arg4);
		
		JMenu menu3arg2 = new JMenu("Pages maximum     ");
		menu3arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/maxPages.png")));
		JMenuItem menu3arg2arg1 = new JMenuItem("5     ");
		JMenuItem menu3arg2arg2 = new JMenuItem("15     ");
		JMenuItem menu3arg2arg3 = new JMenuItem("30     ");
		JMenuItem menu3arg2arg4 = new JMenuItem("500     ");
		menu3arg2.add(menu3arg2arg1);
		menu3arg2.add(menu3arg2arg2);
		menu3arg2.add(menu3arg2arg3);
		menu3arg2.add(menu3arg2arg4);
		
		JMenu menu3arg3 = new JMenu("Sauvegarde auto     ");
		menu3arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/sauvegarder.png")));
		JMenuItem menu3arg3arg1 = new JMenuItem("Activer     ");
		JMenuItem menu3arg3arg2 = new JMenuItem("Désactiver     ");
		menu3arg3.add(menu3arg3arg1);
		menu3arg3.add(menu3arg3arg2);
		
		Main.menuBar.add(menu3);
		menu3.add(menu3arg1);
		menu3.add(menu3arg2);
		menu3.add(menu3arg3);
		
		
		JMenu menu4 = new JMenu("À propos");
		Main.menuBar.add(menu4);
		
		JMenu menu5 = new JMenu("Aide");
		JMenuItem menu5arg1 = new JMenuItem("Charger le document d'aide     ");
		menu5arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/info.png")));
		
		Main.menuBar.add(menu5);
		menu5.add(menu5arg1);
		
		menu1arg1.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { // Ouvrir un fichier
			
			JFileChooser chooser = new JFileChooser(new File(""));
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setMultiSelectionEnabled(true);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	  		chooser.setFileFilter(new FileFilter() {
				@Override
				public String getDescription(){ return "Document PDF"; }
				@Override
				public boolean accept(File file) {
					if((!file.isFile()) || Main.getFileExtension(file).equals("pdf")) return true;
	                else return false;
				}
			});
	  		int result = chooser.showOpenDialog(chooser);
	  		if(result == JFileChooser.APPROVE_OPTION){
				Main.leftBarFiles.openFiles(chooser.getSelectedFiles());
	  		}
			
		}});
		
	}
	
	

}
