package fr.themsou.panel;

import fr.themsou.main.Main;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@SuppressWarnings("serial")
public class MenuBar extends javafx.scene.control.MenuBar{
	
	
	public void setup(){
		
		Menu menu1 = new Menu("Fichier");
		MenuItem menu1arg1 = new MenuItem("Ouvrir     ");
		//menu1arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/ouvrir.png")));
		/*menu1arg1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK, true));
		MenuItem menu1arg2 = new MenuItem("Fermer le fichier     ");
		menu1arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/fermer.png")));
		menu1arg2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK, true));
		MenuItem menu1arg3 = new MenuItem("Vider la liste     ");
		menu1arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/vider.png")));
		menu1arg3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK | Event.SHIFT_MASK, true));
		
		MenuItem menu1arg4 = new MenuItem("Sauvegarder l'édition     ");
		menu1arg4.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/sauvegarder.png")));
		menu1arg4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK, true));
		Menu menu1arg5 = new Menu("Éditions du même nom     ");
		menu1arg5.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/memeNom.png")));
		MenuItem menu1arg6 = new MenuItem("Supprimer l'édition     ");
		menu1arg6.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/supprimer.png")));
		menu1arg6.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.CTRL_MASK, true));
		
		MenuItem menu1arg7 = new MenuItem("Exporter     ");
		menu1arg7.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/exporter.png")));
		menu1arg7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK, true));
		MenuItem menu1arg8 = new MenuItem("Tout exporter     ");
		menu1arg8.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/exporter.png")));
		menu1arg8.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK | Event.SHIFT_MASK, true));
		*/
		getMenus().add(menu1);
		menu1.getItems().add(menu1arg1);
		/*menu1.add(menu1arg2);
		menu1.add(menu1arg3);
		menu1.addSeparator();
		menu1.add(menu1arg4);
		menu1.add(menu1arg5);
		menu1.add(menu1arg6);
		menu1.addSeparator();
		menu1.add(menu1arg7);
		menu1.add(menu1arg8);
		
		
		
		Menu menu2 = new Menu("Édition");
		MenuItem menu2arg1 = new MenuItem(" Annuler     ");
		menu2arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/annuler.png")));
		menu2arg1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK, true));
		MenuItem menu2arg2 = new MenuItem(" Rétablir     ");
		menu2arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/retablir.png")));
		menu2arg2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK, true));
		
		MenuItem menu2arg3 = new MenuItem(" Couper     ");
		menu2arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/couper.png")));
		menu2arg3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK, true));
		MenuItem menu2arg4 = new MenuItem(" Copier     ");
		menu2arg4.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/copier.png")));
		menu2arg4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK, true));
		MenuItem menu2arg5 = new MenuItem(" Coller     ");
		menu2arg5.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/coller.png")));
		menu2arg5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK, true));
		
		Main.menuBar.add(menu2);
		menu2.add(menu2arg1);
		menu2.add(menu2arg2);
		menu2.addSeparator();
		menu2.add(menu2arg3);
		menu2.add(menu2arg4);
		menu2.add(menu2arg5);
		
		
		
		Menu menu3 = new Menu("Préférences");
		
		Menu menu3arg1 = new Menu("Zoom par défaut     ");
		menu3arg1.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/zoom.png")));
		MenuItem menu3arg1arg1 = new MenuItem("100%     ");
		MenuItem menu3arg1arg2 = new MenuItem("125%     ");
		MenuItem menu3arg1arg3 = new MenuItem("150%     ");
		MenuItem menu3arg1arg4 = new MenuItem("175%     ");
		menu3arg1.add(menu3arg1arg1);
		menu3arg1.add(menu3arg1arg2);
		menu3arg1.add(menu3arg1arg3);
		menu3arg1.add(menu3arg1arg4);
		
		Menu menu3arg2 = new Menu("Pages maximum     ");
		menu3arg2.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/maxPages.png")));
		MenuItem menu3arg2arg1 = new MenuItem("5     ");
		MenuItem menu3arg2arg2 = new MenuItem("15     ");
		MenuItem menu3arg2arg3 = new MenuItem("30     ");
		MenuItem menu3arg2arg4 = new MenuItem("500     ");
		menu3arg2.add(menu3arg2arg1);
		menu3arg2.add(menu3arg2arg2);
		menu3arg2.add(menu3arg2arg3);
		menu3arg2.add(menu3arg2arg4);
		
		Menu menu3arg3 = new Menu("Sauvegarde auto     ");
		menu3arg3.setIcon(new ImageIcon(Main.devices.getClass().getResource("/img/MenuBar/sauvegarder.png")));
		MenuItem menu3arg3arg1 = new MenuItem("Activer     ");
		MenuItem menu3arg3arg2 = new MenuItem("Désactiver     ");
		menu3arg3.add(menu3arg3arg1);
		menu3arg3.add(menu3arg3arg2);
		
		Main.menuBar.add(menu3);
		menu3.add(menu3arg1);
		menu3.add(menu3arg2);
		menu3.add(menu3arg3);
		
		
		Menu menu4 = new Menu("À propos");
		Main.menuBar.add(menu4);
		
		Menu menu5 = new Menu("Aide");
		MenuItem menu5arg1 = new MenuItem("Charger le document d'aide     ");
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
				Main.lbFiles.openFiles(chooser.getSelectedFiles());
	  		}
			
		}});*/
		
	}
	
	

}
