package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.UserData;
import fr.clementgre.pdf4teachers.interfaces.OSXTouchBarManager;
import fr.clementgre.pdf4teachers.panel.FooterBar;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import fr.clementgre.pdf4teachers.panel.leftBar.files.FileTab;
import fr.clementgre.pdf4teachers.panel.leftBar.grades.GradeTab;
import fr.clementgre.pdf4teachers.panel.leftBar.paint.PaintTab;
import fr.clementgre.pdf4teachers.panel.leftBar.texts.TextTab;
import fr.clementgre.pdf4teachers.interfaces.Macro;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguageWindow;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class MainWindow extends Stage{

    public static boolean hasToClose = false;
    public static UserData userData;

    public static DecimalFormat format;
    public static DecimalFormat twoDigFormat;

    public static BorderPane root;
    public static SplitPane mainPane;

    public static MainScreen mainScreen;
    public static FooterBar footerBar;
    public static MenuBar menuBar;

    public static TabPane leftBar;
    public static FileTab filesTab;
    public static TextTab textTab;
    public static GradeTab gradeTab;
    public static PaintTab paintTab;

    public OSXTouchBarManager osxTouchBarManager;

    Thread userDataSaver = new Thread(() -> {
        while(true){
            try{ Thread.sleep(1000*60); }catch(InterruptedException e){ e.printStackTrace(); }
            if(isFocused()){
                MainWindow.userData.foregroundTime++;
                if(MainWindow.userData.foregroundTime % (60*50) == 0){
                    Platform.runLater(() -> {
                        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Statistiques"),
                                TR.tr("Vous avez passé") + " " + MainWindow.userData.foregroundTime/60 + " " + TR.tr("heures sur PDF4Teachers."),
                                TR.tr("Remerciez-moi avec un don pour toutes les heures que vous avez gagnés."));
                        ButtonType paypal = new ButtonType(TR.tr("Paypal"), ButtonBar.ButtonData.OTHER);
                        ButtonType github = new ButtonType(TR.tr("GitHub Sponsors"), ButtonBar.ButtonData.OTHER);
                        ButtonType ignore = new ButtonType(TR.tr("Ignorer"), ButtonBar.ButtonData.YES);
                        alert.getButtonTypes().setAll(paypal, github, ignore);
                        Optional<ButtonType> option = alert.showAndWait();
                        if(option.get() == paypal){
                            Main.hostServices.showDocument("https://paypal.me/themsou");
                        }else if (option.get() == github){
                            Main.hostServices.showDocument("https://github.com/sponsors/ClementGre");
                        }
                    });
                }
            }
            MainWindow.userData.save();
        }
    }, "userData AutoSaver");

    public MainWindow(){

        setupDecimalFormat();

        root = new BorderPane();

        StyleManager.putStyle(root, Style.DEFAULT);

        Scene scene = new Scene(root);
        loadDimensions();

        setTitle(TR.tr("PDF4Teachers - Aucun document"));
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));

        setMinWidth(700);
        setMinHeight(393);
        setResizable(true);
        setScene(scene);

        new Macro(scene);

        setOnCloseRequest(e -> {
            userData.save();
            Main.params = new ArrayList<>();
            if(e.getSource().equals(menuBar)) return;
            hasToClose = true;

            if(!mainScreen.closeFile(!Main.settings.autoSave.getValue())) {
                e.consume(); hasToClose = false; return;
            }
            System.exit(0);
        });

        widthProperty().addListener((observable, oldValue, newValue) -> {
            saveDimensions();
        });
        heightProperty().addListener((observable, oldValue, newValue) -> {
            saveDimensions();
        });
        xProperty().addListener((observable, oldValue, newValue) -> {
            saveDimensions();
        });
        yProperty().addListener((observable, oldValue, newValue) -> {
            saveDimensions();
        });

    }
    public void setup(){

        //		SETUPS

        mainPane = new SplitPane();
        leftBar = new TabPane();
        leftBar.setStyle("-fx-tab-max-width: 22px;");

        mainScreen = new MainScreen();
        footerBar = new FooterBar();

        filesTab = new FileTab();
        textTab = new TextTab();
        gradeTab = new GradeTab();
        paintTab = new PaintTab();

        menuBar = new MenuBar();

        mainScreen.repaint();
        footerBar.repaint();

//		PANELS

        show();

        mainPane.getItems().addAll(leftBar, mainScreen);
        mainPane.setDividerPositions(270 / root.getWidth());
        mainPane.getDividers().get(0).positionProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            double width = newValue.doubleValue() * root.getWidth();
            if(width >= 400){
                mainPane.setDividerPositions(400 / root.getWidth());
            }
        });

        root.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            double width = mainPane.getDividerPositions()[0] * oldValue.doubleValue();
            mainPane.setDividerPositions(width / newValue.doubleValue());
        });

        root.setCenter(mainPane);
        root.setTop(menuBar);
        root.setBottom(footerBar);

        Main.window.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) root.setBottom(null);
            else root.setBottom(footerBar);
        });

        root.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.TAB){
                if(leftBar.getSelectionModel().getSelectedIndex() == 1) leftBar.getSelectionModel().select(2);
                else leftBar.getSelectionModel().select(1);
                e.consume();
            }
        });

        ImageUtils.setupListeners();
        setupDesktopEvents();

//		SHOWING

        userDataSaver.start();
        mainScreen.repaint();

//      OPEN DOC

        List<File> toOpenFiles = new ArrayList<>();
        for(String param : Main.params){
            if(new File(param).exists()){
                toOpenFiles.add(new File(param));
            }
        }
        filesTab.openFiles(toOpenFiles);

        if(Main.firstLaunch || !Main.settings.getSettingsVersion().equals(Main.VERSION)){
            mainScreen.openFile(LanguageWindow.getDocFile());
        }else if(toOpenFiles.size() >= 1){
            if(FilesUtils.getExtension(toOpenFiles.get(0).getName()).equalsIgnoreCase("pdf")){
                mainScreen.openFile(toOpenFiles.get(0));
            }
        }

//      Other interfaces

        //osxTouchBarManager = new OSXTouchBarManager(this);

//      CHECK UPDATES
        new Thread(() -> {

            userData = new UserData();

            if(UpdateWindow.checkVersion()){
                Platform.runLater(() -> {
                    if(UpdateWindow.newVersion){
                        if(menuBar.isSystemMenuBarSupported()){
                            menuBar.about.setText(TR.tr("À Propos") + " " + TR.tr("(Nouvelle Version Disponible)"));
                        }else{
                            menuBar.about.setStyle("-fx-background-color: #d6a600;");
                        }

                        Tooltip.install(menuBar.about.getGraphic(), new Tooltip(TR.tr("Une nouvelle version est disponible !")));

                        if(Main.settings.checkUpdates.getValue()){
                            new UpdateWindow();
                        }
                    }
                });
            }
        }).start();

    }
    public void loadDimensions(){

        String[] size = Main.settings.mainScreenSize.getValue().split(Pattern.quote(";"));
        if(size.length == 5){
            try{
                setMaximized(Boolean.parseBoolean(size[4]));
                double w = Double.parseDouble(size[0]);
                double h = Double.parseDouble(size[1]);
                double x = Double.parseDouble(size[2]);
                double y = Double.parseDouble(size[3]);

                double sw = Main.SCREEN_BOUNDS.getWidth();
                double sh = Main.SCREEN_BOUNDS.getHeight();
                if(w > sw) w = sw - 200;
                if(h > sh) h = sh - 200;
                if(x != -1){
                    if(x + sh <= sw) setX(x);
                }
                if(y != -1){
                    if(y + h <= sh) setY(y);
                }

                setWidth(w);
                setHeight(h);

            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
    }
    public void saveDimensions(){
        String lastDimensions = getWidth() + ";" + getHeight() + ";" + getX() + ";" + getY() + ";" + isMaximized();

        new Thread(() -> {
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){ e.printStackTrace(); }
            if(lastDimensions.equals(getWidth() + ";" + getHeight() + ";" + getX() + ";" + getY() + ";" + isMaximized())){
                Main.settings.mainScreenSize.setValue(lastDimensions);
            }
        }, "MainScreenDimensionsPreSaver").start();

    }
    public void centerWindowIntoMe(Window window){
        double w = window.getWidth();
        double h = window.getHeight();
        double sw = Main.SCREEN_BOUNDS.getWidth();
        double sh = Main.SCREEN_BOUNDS.getHeight();

        double x = getX() + getWidth()/2 - w/2;
        double y = getY() + getHeight()/2 - h/2;

        if(x > sw-w) x = sw-w;
        if(y > sh-h) y = sh-h;
        if(x < 0) x = 0;
        if(y < 0) y = 0;

        window.setX(x);
        window.setY(y);
    }

    public void setupDesktopEvents(){

        if(Desktop.isDesktopSupported()){
            if(Desktop.getDesktop().isSupported(Desktop.Action.APP_ABOUT)){
                Desktop.getDesktop().setAboutHandler(e -> {
                    Platform.runLater(AboutWindow::new);
                });
            }

            if(Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_URI)){
                Desktop.getDesktop().setOpenURIHandler(e -> {
                    System.out.println(e.getURI());
                    Platform.runLater(() -> {
                        File file = new File(e.getURI());
                        if(file.exists()){
                            MainWindow.filesTab.openFiles(new File[]{file});
                            MainWindow.mainScreen.openFile(file);
                        }
                    });
                });
            }

            if(Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)){
                Desktop.getDesktop().setOpenFileHandler(e -> {
                    Platform.runLater(() -> {
                        System.out.println(e.getFiles().get(0).getAbsolutePath());
                        MainWindow.filesTab.openFiles((File[]) e.getFiles().toArray());
                        if(e.getFiles().size() == 1) MainWindow.mainScreen.openFile(e.getFiles().get(0));
                    });

                });
            }

            if(Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES)){
                Desktop.getDesktop().setPreferencesHandler(e -> {
                    Platform.runLater(() -> {
                        menuBar.settings.fire();
                    });
                });
            }

        }

    }

    private static void setupDecimalFormat(){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        char separator = TR.tr("Decimal separator").charAt(0);
        if(separator == 'D') separator = ',';
        else if(separator != ',' && separator != '.') separator = '.';
        symbols.setDecimalSeparator(separator);
        MainWindow.format = new DecimalFormat("0.####", symbols);
        MainWindow.twoDigFormat = new DecimalFormat("0.##", symbols);
    }

}
