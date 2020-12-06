package fr.clementgre.pdf4teachers;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.datasaving.Settings;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguageWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.LicenseWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.LogWindow;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

	public static MainWindow window;

	public static Settings settings;

	public static DecimalFormat format;
	public static HostServices hostServices;

	public static String dataFolder = System.getProperty("user.home") + File.separator + ".PDF4Teachers" + File.separator;
	public static final String VERSION = "Snapshot-2 1.2.1";
	public static final boolean DEBUG = false;
	public static final boolean COPY_CONSOLE = true;

	public static boolean firstLaunch;
	public static final Rectangle2D SCREEN_BOUNDS = Screen.getPrimary().getBounds();
	public static String systemShortcut = "Ctrl";
	public static List<String> params;

	public static void main(String[] args){
		if(COPY_CONSOLE) LogWindow.copyLogs();
		System.out.println("Starting PDF4Teachers...");

		setupDecimalFormat();

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


		System.out.println("Starting with parameters: \nRaw: " + getParameters().getRaw().toString()
				+ "\n Unnamed: " + getParameters().getUnnamed().toString()
				+ "\n Named: " + getParameters().getNamed().toString());
		params = getParameters().getRaw();


		// PREPARATION

		settings = new Settings();

		// setups
		LanguageWindow.setup();
		StyleManager.setup();

		if(languageAsk()){
			if(licenceAsk()){
				startMainWindow();
			}
		}

	}

	public boolean languageAsk(){
		if(settings.getLanguage().isEmpty()){

			String language = LanguageWindow.getLanguageFromComputerLanguage();
			if(language != null){
				Main.settings.setLanguage(language);
				TR.updateTranslation();
				return true;
			}else{
				Main.settings.setLanguage("en-us");
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

		// Disabling the license
		//if(true) return true;

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

	private static void setupDecimalFormat(){
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
		char separator = TR.tr("Decimal separator").charAt(0);
		if(separator == 'D') separator = ',';
		else if(separator != ',' && separator != '.') separator = '.';
		symbols.setDecimalSeparator(separator);
		format = new DecimalFormat("0.####", symbols);
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
