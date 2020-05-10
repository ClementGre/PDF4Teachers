package fr.themsou.main;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;

import fr.themsou.yaml.FileConfiguration;
import fr.themsou.utils.TR;
import fr.themsou.windows.LanguageWindow;
import fr.themsou.windows.LicenseWindow;
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
	public static final String VERSION = "Snapshot 1.2.0";
	public static final boolean DEBUG = true;

	public static boolean firstLaunch;
	public static final Rectangle2D SCREEN_BOUNDS = Screen.getPrimary().getBounds();

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

		FileConfiguration config = new FileConfiguration(new File("C:\\Users\\Clement\\Downloads\\test.yml"));
		HashMap<String, Object> data = new HashMap<>();
		data.put("x", 589);
		data.put("y", 1895.5);
		data.put("color", "5fc9d5");
		data.put("fontName", "Arial");
		data.put("fontSize", 42);
		data.put("fontBold", true);
		data.put("fontItalic", false);
		config.getSectionSecure("texts.page1").put("6", data);
		config.save();

		System.exit(0);

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
			Main.settings.setLanguage("English US");
			TR.updateTranslation();
			new LanguageWindow(value -> {
				if(!value.isEmpty()) {
					Main.settings.setLanguage(value);
				}
				TR.updateTranslation();
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

	public static void startMainWindow(){
		window = new MainWindow();
		window.setup();
	}


}
