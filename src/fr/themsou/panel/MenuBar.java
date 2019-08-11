package fr.themsou.panel;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import fr.themsou.main.Main;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar{
	
	
	public void setup(){
		
		JMenu menu1 = new JMenu("Fichier");
		JMenuItem menu1arg1 = new JMenuItem("Ouvrir un fichier     ");
		menu1arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Ouvrir-doc.png")));
		menu1arg1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK, true));
		JMenuItem menu1arg2 = new JMenuItem("Ouvrir un dossier     ");
		menu1arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Ouvrir-dossier.png")));
		menu1arg2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK | Event.SHIFT_MASK, true));
		
		JMenuItem menu1arg3 = new JMenuItem("Sauvegarder     ");
		menu1arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Sauvegarder.png")));
		menu1arg3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK, true));
		
		JMenuItem menu1arg4 = new JMenuItem("Fermer le fichier     ");
		menu1arg4.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Fermer.png")));
		menu1arg4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK, true));
		JMenuItem menu1arg5 = new JMenuItem("Quitter     ");
		menu1arg5.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Quitter.png")));
		menu1arg5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK, true));
		
		Main.menuBar.add(menu1);
		menu1.add(menu1arg1);
		menu1.add(menu1arg2);
		menu1.addSeparator();
		menu1.add(menu1arg3);
		menu1.addSeparator();
		menu1.add(menu1arg4);
		menu1.add(menu1arg5);
		
		
		
		JMenu menu2 = new JMenu("Édition");
		JMenuItem menu2arg1 = new JMenuItem(" Annuler     ");
		menu2arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Annuler.png")));
		menu2arg1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK, true));
		JMenuItem menu2arg2 = new JMenuItem(" Rétablir     ");
		menu2arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Retablir.png")));
		menu2arg2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK, true));
		
		JMenuItem menu2arg3 = new JMenuItem(" Couper     ");
		menu2arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Couper.png")));
		menu2arg3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK, true));
		JMenuItem menu2arg4 = new JMenuItem(" Copier     ");
		menu2arg4.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Copier.png")));
		menu2arg4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK, true));
		JMenuItem menu2arg5 = new JMenuItem(" Coller     ");
		menu2arg5.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Coller.png")));
		menu2arg5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK, true));
		
		Main.menuBar.add(menu2);
		menu2.add(menu2arg1);
		menu2.add(menu2arg2);
		menu2.addSeparator();
		menu2.add(menu2arg3);
		menu2.add(menu2arg4);
		menu2.add(menu2arg5);
		
		
		
		JMenu menu3 = new JMenu("Préférences");
		JMenuItem menu3arg1 = new JMenuItem("Zoom par défaut     ");
		menu3arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Zoom.png")));
		JMenuItem menu3arg2 = new JMenuItem("Qualité de rendu     ");
		menu3arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Rendu.png")));
		JMenuItem menu3arg3 = new JMenuItem("Pages maximum     ");
		menu3arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/Pages.png")));
		
		Main.menuBar.add(menu3);
		menu3.add(menu3arg1);
		menu3.add(menu3arg2);
		menu3.add(menu3arg3);
		
		
		menu1arg1.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { // Supprimer
			
			if(MainScreen.current != null){
				int i = JOptionPane.showConfirmDialog(null, "Êtes vous sur de vouloir supprimer le document " + MainScreen.current.getName() + " ?");
				if(i == 0){
					
				}
			}else{
				JOptionPane.showMessageDialog(null, "Vous devez sélexionner une Fiche");
			}
		}});
		
	}
	
	

}
