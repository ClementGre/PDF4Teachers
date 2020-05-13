package fr.themsou.windows;

import fr.themsou.main.Main;
import fr.themsou.utils.FontUtils;
import fr.themsou.utils.TR;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import javax.swing.*;
import java.io.OutputStream;
import java.io.PrintStream;

public class LogWindow extends Stage {

    public static PrintStream console = System.out;
    public static PrintStream errConsole = System.err;
    public static StringBuilder consoleText = new StringBuilder();

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
        new JMetro(scene, Style.DARK);

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

    private Label text = new Label(consoleText.toString());
    private Thread updater = new Thread(() -> {
        while(true){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> text.setText(consoleText.toString()));
        }
    });

    private void setupUi(Pane root){

        text.setFont(FontUtils.getFont("Arial", false, false, 14));
        text.setStyle("-fx-color: white; -fx-padding: 10;");
        text.setWrapText(true);
        text.prefWidthProperty().bind(root.widthProperty().subtract(20));
        text.minHeight(Double.MAX_VALUE);

        root.getChildren().add(text);
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));


        updater.start();
    }

    public static void copyLogs() {

        OutputStream newConsole = new OutputStream() {
            @Override public void write(int b){
                consoleText.append((char) b);
                console.print((char) b);
            }
        };
        OutputStream newErrConsole = new OutputStream() {
            @Override public void write(int b){
                consoleText.append((char) b);
                errConsole.print((char) b);
            }
        };
        System.setOut(new PrintStream(newConsole));
        System.setErr(new PrintStream(newErrConsole));
    }

}
