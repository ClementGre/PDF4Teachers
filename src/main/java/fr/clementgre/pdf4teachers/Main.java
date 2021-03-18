package fr.clementgre.pdf4teachers;

import java.io.*;
import java.util.List;

import fr.clementgre.pdf4teachers.datasaving.SyncUserData;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.datasaving.settings.Settings;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguageWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.LicenseWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.LogWindow;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.DataFormat;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	public static MainWindow window;

	public static Settings settings;
	public static SyncUserData syncUserData;

	public static HostServices hostServices;

	public static String dataFolder = System.getProperty("user.home") + File.separator + ".PDF4Teachers" + File.separator;
	public static final String VERSION = "sn1-1.3.0";
	public static final boolean DEBUG = true;
	public static final boolean COPY_CONSOLE = false;
	public static final boolean TRANSLATIONS_IN_CODE = true;

	public static boolean firstLaunch;
	public static final Rectangle2D SCREEN_BOUNDS = Screen.getPrimary().getBounds();
	public static String systemShortcut = "Ctrl";
	public static List<String> params;

	public static final DataFormat INTERNAL_FORMAT = new DataFormat("application/pdf4teachers-internal-format; class=java.lang.String");

	public static void main(String[] args){
		if(COPY_CONSOLE) LogWindow.copyLogs();
		System.out.println("Starting PDF4Teachers...");

		///// START APP /////
		launch(args);

	}
	@Override
	public void start(Stage window){

		// define crucial vars
		if(isWindows()) dataFolder = System.getenv("APPDATA") + File.separator + "PDF4Teachers" + File.separator;
		else if(isOSX()) systemShortcut = "Cmd";

		firstLaunch = !new File(dataFolder + File.separator + "settings.yml").exists();
		hostServices = getHostServices();

		// read params
		if(DEBUG){
			System.out.println("Starting with parameters: \nRaw: " + getParameters().getRaw().toString()
					+ "\n Unnamed: " + getParameters().getUnnamed().toString()
					+ "\n Named: " + getParameters().getNamed().toString());
		}

		params = getParameters().getRaw();

		// PREPARATION

		settings = new Settings();
		syncUserData = new SyncUserData();

		// setups
		TR.setup();
		StyleManager.setup();
		AutoTipsManager.setup();
		ImageUtils.setupListeners();


		if(languageAsk()){
			if(licenceAsk()){
				startMainWindow();
			}
		}

	}

	public boolean languageAsk(){
		if(settings.language.getValue().isEmpty()){
			String language = TR.getLanguageFromComputerLanguage();
			if(language != null){
				Main.settings.language.setValue(language);
				TR.updateLocale();
			}else{
				new LanguageWindow(value -> {
					if(!value.isEmpty()) Main.settings.language.setValue(value);
					TR.updateLocale();
					if(licenceAsk()){
						startMainWindow();
					}
				});
				return false;
			}
		}
		return true;
	}
	public boolean licenceAsk(){

		// Disabling the license
		if(true) return true;

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

	public static boolean isWindows(){
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	public static boolean isOSX(){
		return System.getProperty("os.name").toLowerCase().contains("mac os x");
	}
	public static boolean isLinux(){
		return !isWindows() && !isOSX();
	}




}
