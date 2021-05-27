package fr.clementgre.pdf4teachers.interfaces.windows.log;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.PrintStream;

public class LogWindow extends Stage{
    
    public static StringBuffer logs = new StringBuffer();
    
    public static PrintStream newConsole = new CustomPrintStream(System.out, logs);
    public static PrintStream newErrConsole = new CustomPrintStream(System.err, logs);
    
    public LogWindow(){
        
        Pane root = new Pane();
        Scene scene = new Scene(root, 1200, 675);
        
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        setResizable(true);
        setTitle(TR.tr("printStreamWindow.title"));
        setScene(scene);
        setOnCloseRequest(e -> {
            stopUpdater();
            close();
        });
        new JMetro(scene, Style.DARK);
        
        Pane pane = new Pane();
        root.setStyle("-fx-background-color: black;");
        ScrollPane scrollPane = new ScrollPane(pane);
        scrollPane.setStyle("-fx-background-color: black;");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.prefWidthProperty().bind(scene.widthProperty());
        scrollPane.prefHeightProperty().bind(scene.heightProperty());
        pane.minHeightProperty().bind(text.heightProperty());
        root.getChildren().add(scrollPane);
        setupUi(pane);
    
        Main.window.centerWindowIntoMe(this);
        show();
        Main.window.centerWindowIntoMe(this);
        MainWindow.preventWindowOverflowScreen(this);
    }
    
    private final Label text = new Label(logs.toString());
    private boolean needToStopUpdater = false;
    public void stopUpdater(){
        needToStopUpdater = true;
    }
    private final Thread updater = new Thread(() -> {
        needToStopUpdater = false;
        try{
            Thread.sleep(200);
        }catch(InterruptedException e){ e.printStackTrace(); }
        while(!needToStopUpdater){
            Platform.runLater(() -> text.setText(logs.toString()));
            try{
                Thread.sleep(200);
            }catch(InterruptedException e){ e.printStackTrace(); }
        }
    });
    
    private void setupUi(Pane root){
        
        text.setStyle("-fx-font-size: 12; -fx-text-fill: white; -fx-padding: 5;");
        text.setWrapText(true);
        text.prefWidthProperty().bind(root.widthProperty());
        text.minHeight(Double.MAX_VALUE);
        
        root.getChildren().add(text);
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        updater.start();
    }
    
    public static void copyLogs(){
        System.setOut(newConsole);
        System.setErr(newErrConsole);
    }
    
}
