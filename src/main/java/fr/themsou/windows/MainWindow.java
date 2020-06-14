package fr.themsou.windows;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.panel.FooterBar;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.MenuBar;
import fr.themsou.panel.leftBar.files.LBFileTab;
import fr.themsou.panel.leftBar.grades.LBGradeTab;
import fr.themsou.panel.leftBar.paint.LBPaintTab;
import fr.themsou.panel.leftBar.texts.LBTextTab;
import fr.themsou.utils.Macro;
import fr.themsou.utils.TR;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class MainWindow extends Stage{

    public static boolean hasToClose = false;
    public static UserData userData;


    public static BorderPane root;
    public static SplitPane mainPane;

    public static MainScreen mainScreen;
    public static FooterBar footerBar;
    public static MenuBar menuBar;

    public static TabPane leftBar;
    public static LBFileTab lbFilesTab;
    public static LBTextTab lbTextTab;
    public static LBGradeTab lbGradeTab;
    public static LBPaintTab lbPaintTab;


    Thread userDataSaver = new Thread(() -> {
        while(true){
            try{ Thread.sleep(300000); }catch(InterruptedException e){ e.printStackTrace(); }
            MainWindow.userData.saveData();
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
            userData.saveData();
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

        mainScreen = new MainScreen();
        footerBar = new FooterBar();

        lbFilesTab = new LBFileTab();
        lbTextTab = new LBTextTab();
        lbGradeTab = new LBGradeTab();
        lbPaintTab = new LBPaintTab();

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

        root.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.TAB){
                if(leftBar.getSelectionModel().getSelectedIndex() == 1) leftBar.getSelectionModel().select(2);
                else leftBar.getSelectionModel().select(1);
                e.consume();
            }
        });

//		SHOWING

        userDataSaver.start();
        mainScreen.repaint();

//      COPY DESC

        if(Main.firstLaunch || !Main.settings.getSettingsVersion().equals(Main.VERSION)){
            mainScreen.openFile(LanguageWindow.getDocFile());
        }

//      CHECK UPDATES
        new Thread(() -> {

            userData = new UserData();

            if(UpdateWindow.checkVersion()){
                Platform.runLater(() -> {
                    if(UpdateWindow.newVersion){
                        menuBar.apropos.setStyle("-fx-background-color: #d6a600;");
                        Tooltip.install(menuBar.apropos.getGraphic(), new Tooltip(TR.tr("Une nouvelle version est disponible !")));

                        if(Main.settings.isCheckUpdates()){
                            new UpdateWindow();
                        }
                    }
                });
            }
        }).start();

    }

}
