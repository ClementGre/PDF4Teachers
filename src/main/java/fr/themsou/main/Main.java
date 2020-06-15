package fr.themsou.main;

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;

import fr.themsou.utils.TR;
import fr.themsou.utils.style.StyleManager;
import fr.themsou.windows.LanguageWindow;
import fr.themsou.windows.LicenseWindow;
import fr.themsou.windows.LogWindow;
import fr.themsou.windows.MainWindow;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

	public static MainWindow window;

	public static Settings settings;

	public static DecimalFormat format = new DecimalFormat("0.#");
	public static HostServices hostServices;

	public static String dataFolder = System.getProperty("user.home") + File.separator + ".PDF4Teachers" + File.separator;
	public static final String VERSION = "1.2.0";
	public static final boolean DEBUG = false;
	public static final boolean COPY_CONSOLE = true;

	public static boolean firstLaunch;
	public static final Rectangle2D SCREEN_BOUNDS = Screen.getPrimary().getBounds();
	public static String systemShortcut = "Ctrl";

	static {
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)){
			Desktop.getDesktop().setOpenFileHandler(e -> {
				System.out.println(e.getFiles().get(0).getAbsolutePath());
				if(window.isShowing()){
					MainWindow.lbFilesTab.openFiles((File[]) e.getFiles().toArray());
					if(e.getFiles().size() == 1) MainWindow.mainScreen.openFile(e.getFiles().get(0));

				}
			});
		}
	}

	public static void main(String[] args){
		if(COPY_CONSOLE) LogWindow.copyLogs();
		System.out.println("Starting PDF4Teachers...");

		///// START APP /////
		launch(args);

		///// OPEN FILES WITH PDF4TEACHERS /////
		/*Desktop.getDesktop().setOpenURIHandler(e -> {
			File file = new File(e.getURI());
			if(window.isShowing() && file.exists()){
				MainWindow.lbFilesTab.openFiles(new File[]{file});
				MainWindow.mainScreen.openFile(file);
			}

		});*/
	}
	@Override
	public void start(Stage window){

		if(System.getProperty("os.name").toLowerCase().contains("win")) dataFolder = System.getenv("APPDATA") + File.separator + "PDF4Teachers" + File.separator;
		else if(System.getProperty("os.name").toLowerCase().contains("os x")) systemShortcut = "Cmd";

		firstLaunch = !new File(dataFolder + File.separator + "settings.yml").exists();
		hostServices = getHostServices();

		// PREPARATION

		settings = new Settings();

		// force to re-copy all files only if version has changed
		LanguageWindow.copyFiles(!settings.getSettingsVersion().equals(VERSION));
		StyleManager.setup();

		if(languageAsk()){
			if(licenceAsk()){
				startMainWindow();
			}
		}

	}

	public boolean languageAsk(){
		if(settings.getLanguage().isEmpty()){

			String language = LanguageWindow.detectLanguage();
			if(language != null){
				Main.settings.setLanguage(language);
				TR.updateTranslation();
				return true;
			}else{
				Main.settings.setLanguage("English US");
				TR.updateTranslation();
				new LanguageWindow(value -> {
					if(!value.isEmpty()) Main.settings.setLanguage(value);
					TR.updateTranslation();
					if(licenceAsk()){
						startMainWindow();
					}
				});
				return false;
			}
		}else{
			TR.updateTranslation();
			return true;
		}
	}
	public boolean licenceAsk(){
		if(firstLaunch){
			new LicenseWindow(value -> {
				startMainWindow();
			});
			return false;
		}else{
			return true;
		}
	}

	public static void startMainWindow(){
		window = new MainWindow();
		window.setup();
	}


}
