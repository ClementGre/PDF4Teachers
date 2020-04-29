package fr.themsou.windows;

import fr.themsou.main.Main;
import fr.themsou.main.UserData;
import fr.themsou.panel.FooterBar;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.MenuBar;
import fr.themsou.panel.leftBar.files.LBFilesTab;
import fr.themsou.panel.leftBar.notes.LBNoteTab;
import fr.themsou.panel.leftBar.paint.LBPaintTab;
import fr.themsou.panel.leftBar.texts.LBTextTab;
import fr.themsou.utils.Macro;
import fr.themsou.utils.TR;
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
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MainWindow extends Stage{

    public static boolean hasToClose = false;
    public static UserData userData;


    public static BorderPane root;
    public static SplitPane mainPane;

    public static MainScreen mainScreen;
    public static FooterBar footerBar;
    public static MenuBar menuBar;

    public static TabPane leftBar;
    public static LBFilesTab lbFilesTab;
    public static LBTextTab lbTextTab;
    public static LBNoteTab lbNoteTab;
    public static LBPaintTab lbPaintTab;


    Thread userDataSaver = new Thread(() -> {
        while(true){
            try{ Thread.sleep(300000); }catch(InterruptedException e){ e.printStackTrace(); }
            MainWindow.userData.saveData();
        }
    }, "userData AutoSaver");

    public MainWindow(){

        root = new BorderPane();
        Scene scene = new Scene(root, Main.SCREEN_BOUNDS.getWidth()-100 >= 1200 ? 1200 : Main.SCREEN_BOUNDS.getWidth()-100, Main.SCREEN_BOUNDS.getHeight()-100 >= 675 ? 675 : Main.SCREEN_BOUNDS.getHeight()-100);

        setTitle(TR.tr("PDF4Teachers - Aucun document"));
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));

        setMinWidth(700);
        setMinHeight(393);
        setResizable(true);
        setScene(scene);

        setOnCloseRequest(e -> {
            if(e.getSource().equals(menuBar)) return;
            hasToClose = true;
            if(!mainScreen.closeFile(!Main.settings.isAutoSave())) {
                userData.saveData();
                e.consume();
                hasToClose = false;
                return;
            }
            userData.saveData();
            System.exit(0);
        });
    }
    public void setup(){

        //		SETUPS

        mainPane = new SplitPane();
        leftBar = new TabPane();

        mainScreen = new MainScreen();
        footerBar = new FooterBar();

        lbFilesTab = new LBFilesTab();
        lbTextTab = new LBTextTab();
        lbNoteTab = new LBNoteTab();
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

        new Macro(root);

//		THEME

        new JMetro(root, Style.LIGHT);
        new JMetro(menuBar, Style.DARK);

//		SHOWING

        userDataSaver.start();
        mainScreen.repaint();

//      COPY DESC

        if(Main.firstLaunch){
            mainScreen.openFile(LanguageWindow.getDocFile());
        }

        // load data
        userData = new UserData();

//      OPEN THE LAST FILE
//      AND CHECK FOR UPDATES
        new Thread(() -> {
            Platform.runLater(() -> {
                if(Main.settings.getOpenedFile() != null){
                    mainScreen.openFile(Main.settings.getOpenedFile());
                }
            });

            if(UpdateWindow.checkVersion()){
                Platform.runLater(() -> {
                    menuBar.apropos.setStyle("-fx-background-color: #ba6800;");
                    Tooltip.install(menuBar.apropos.getGraphic(), new Tooltip(TR.tr("Une nouvelle version est disponible !")));

                    if(Main.settings.isCheckUpdates()){
                        new UpdateWindow();
                    }
                });
            }
        }).start();


    }

}
