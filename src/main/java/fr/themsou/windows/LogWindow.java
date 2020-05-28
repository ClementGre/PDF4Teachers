package fr.themsou.windows;

import fr.themsou.main.Main;
import fr.themsou.utils.CustomPrintStream;
import fr.themsou.utils.FontUtils;
import fr.themsou.utils.TR;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;

import java.io.PrintStream;

public class LogWindow extends Stage {

    public static StringBuffer logs = new StringBuffer();

    public static PrintStream newConsole = new CustomPrintStream(System.out, logs);
    public static PrintStream newErrConsole = new CustomPrintStream(System.err, logs);

    public LogWindow(){

        Pane root = new Pane();
        Scene scene = new Scene(root, Main.SCREEN_BOUNDS.getWidth()-200 >= 1200 ? 1200 : Main.SCREEN_BOUNDS.getWidth()-200, Main.SCREEN_BOUNDS.getHeight()-200 >= 675 ? 675 : Main.SCREEN_BOUNDS.getHeight()-200);

        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setResizable(true);
        setTitle(TR.tr("PDF4Teachers - Console"));
        setScene(scene);
        setOnCloseRequest(e -> {
            updater.stop();
            close();
        });
        StyleManager.putStyle(scene, Style.DEFAULT);

        Pane pane = new Pane();
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        ScrollPane scrollPane = new ScrollPane(pane);
        scrollPane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.prefWidthProperty().bind(scene.widthProperty());
        scrollPane.prefHeightProperty().bind(scene.heightProperty());
        pane.minHeightProperty().bind(text.heightProperty());
        root.getChildren().add(scrollPane);
        setupUi(pane);

        show();
    }

    private Label text = new Label(logs.toString());
    private Thread updater = new Thread(() -> {
        while(true){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> text.setText(logs.toString()));
        }
    });

    private void setupUi(Pane root){

        text.setStyle("-fx-font-size: 14;");
        text.setStyle("-fx-color: white; -fx-padding: 10;");
        text.setWrapText(true);
        text.prefWidthProperty().bind(root.widthProperty());
        text.minHeight(Double.MAX_VALUE);

        root.getChildren().add(text);
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));


        updater.start();
    }

    public static void copyLogs() {
        System.setOut(newConsole);
        System.setErr(newErrConsole);
    }

}
