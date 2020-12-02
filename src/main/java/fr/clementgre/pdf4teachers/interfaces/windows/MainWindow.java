package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.UserData;
import fr.clementgre.pdf4teachers.panel.FooterBar;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import fr.clementgre.pdf4teachers.panel.leftBar.files.FileTab;
import fr.clementgre.pdf4teachers.panel.leftBar.grades.GradeTab;
import fr.clementgre.pdf4teachers.panel.leftBar.paint.PaintTab;
import fr.clementgre.pdf4teachers.panel.leftBar.texts.TextTab;
import fr.clementgre.pdf4teachers.interfaces.Macro;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.interfaces.windows.language.LanguageWindow;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainWindow extends Stage{

    public static boolean hasToClose = false;
    public static UserData userData;


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


    Thread userDataSaver = new Thread(() -> {
        while(true){
            try{ Thread.sleep(300000); }catch(InterruptedException e){ e.printStackTrace(); }
            MainWindow.userData.save();
        }
    }, "userData AutoSaver");

    public MainWindow(){

        root = new BorderPane();

        StyleManager.putStyle(root, Style.DEFAULT);

        Scene scene = new Scene(root, Main.SCREEN_BOUNDS.getWidth()-200 >= 1200 ? 1200 : Main.SCREEN_BOUNDS.getWidth()-200, Main.SCREEN_BOUNDS.getHeight()-200 >= 675 ? 675 : Main.SCREEN_BOUNDS.getHeight()-200);

        setTitle(TR.tr("PDF4Teachers - Aucun document"));
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));

        setMinWidth(700);
        setMinHeight(393);
        setResizable(true);
        setScene(scene);

        new Macro(scene);

        setOnCloseRequest(e -> {
            userData.save();
            if(e.getSource().equals(menuBar)) return;
            hasToClose = true;

            if(!mainScreen.closeFile(!Main.settings.isAutoSave())) {
                e.consume(); hasToClose = false; return;
            }
            System.exit(0);
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

//		SHOWING

        userDataSaver.start();
        mainScreen.repaint();

//      OPEN DOC

        if(Main.firstLaunch || !Main.settings.getSettingsVersion().equals(Main.VERSION)){
            mainScreen.openFile(LanguageWindow.getDocFile());
        }

//      CHECK UPDATES
        new Thread(() -> {

            userData = new UserData();

            if(UpdateWindow.checkVersion()){
                Platform.runLater(() -> {
                    if(UpdateWindow.newVersion){
                        menuBar.about.setStyle("-fx-background-color: #d6a600;");
                        Tooltip.install(menuBar.about.getGraphic(), new Tooltip(TR.tr("Une nouvelle version est disponible !")));

                        if(Main.settings.isCheckUpdates()){
                            new UpdateWindow();
                        }
                    }
                });
            }
        }).start();

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

}
