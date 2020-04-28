package fr.themsou.main;

import java.io.File;
import java.text.DecimalFormat;

import fr.themsou.utils.TR;
import fr.themsou.windows.LanguageWindow;
import fr.themsou.windows.LicenseWindow;
import fr.themsou.windows.MainWindow;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class Main extends Application {

	public static MainWindow window;

	public static Settings settings;

	public static DecimalFormat format = new DecimalFormat("0.#");
	public static HostServices hostServices;

	public static String dataFolder = System.getProperty("user.home") + File.separator + ".PDF4Teachers" + File.separator;
	public static final String VERSION_CODE = "010100";
	public static final String VERSION = "Snapshot 1.1.0";
	public static final boolean DEBUG = false;

	public static boolean firstLaunch;

	public static void main(String[] args){
		launch(args);

	}
	@Override
	public void start(Stage window) throws Exception {
		if(System.getProperty("os.name").toLowerCase().contains("win")){
			dataFolder = System.getenv("APPDATA") + File.separator + "PDF4Teachers" + File.separator;
		}
		firstLaunch = !new File(dataFolder + File.separator + "settings.yml").exists();
		hostServices = getHostServices();

		// PREPARATION

		settings = new Settings();
		LanguageWindow.copyFiles();


		if(languageAsk()){
			if(liscenceAsk()){
				startMainWindow();
			}
		}

	}

	public boolean languageAsk(){
		if(settings.getLanguage().isEmpty()){
			new LanguageWindow(value -> {
				if(liscenceAsk()){
					startMainWindow();
				}
			});
			return false;
		}
		TR.updateTranslation();
		return true;
	}
	public boolean liscenceAsk(){
		if(firstLaunch){
			new LicenseWindow(value -> {
				startMainWindow();
			});
			return false;
		}else{
			return true;
		}
	}

	public void startMainWindow(){
		window = new MainWindow();
		window.setup();
	}


}
