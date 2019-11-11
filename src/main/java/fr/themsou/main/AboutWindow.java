package fr.themsou.main;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;

public class AboutWindow extends Stage {

    public AboutWindow(){

        VBox root = new VBox();
        Scene scene = new Scene(root, 550, 450);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/App Logo.png")+""));
        setWidth(550);
        setHeight(450);
        setMinWidth(550);
        setMinHeight(450);
        setTitle("PDF Teacher - À Propos");
        setResizable(false);
        setScene(scene);
        setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(javafx.stage.WindowEvent e){ close(); }
        });
        new JMetro(root, Style.LIGHT);

        setupUi(root);

        show();
    }

    private void setupUi(VBox root){

        VBox vBox = new VBox();

            ImageView logo = new ImageView(getClass().getResource(File.separator + "App Logo.png")+"");
            logo.setFitWidth(200);
            logo.setPreserveRatio(true);

            Label name = new Label("PDFTeacher");
            name.setFont(new Font(23));
            name.setAlignment(Pos.CENTER);

            Label version = new Label("version 1.0.0");
            version.setFont(new Font(15));
            version.setAlignment(Pos.CENTER);

            HBox devInfo = new HBox();
                Label dev = new Label("Développeur :");
                dev.setFont(new Font(17));

                Hyperlink devName = new Hyperlink("themsou");
                devName.setFont(new Font(17));
                devName.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                        Main.hostServices.showDocument("https://github.com/themsou");
                    }
                });
            devInfo.getChildren().addAll(dev, devName);
            devInfo.setAlignment(Pos.CENTER);

            HBox gitInfo = new HBox();
                Label git = new Label("Projet GitHub :");
                git.setFont(new Font(17));

                Hyperlink gitName = new Hyperlink("themsou/PDFTeacher");
                gitName.setFont(new Font(17));
                gitName.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                        Main.hostServices.showDocument("https://github.com/themsou/PDFTeacher");
                    }
                });
            gitInfo.getChildren().addAll(git, gitName);
            gitInfo.setAlignment(Pos.CENTER);

            HBox issueInfo = new HBox();
                Hyperlink issueName = new Hyperlink("un problème ?");
                issueName.setFont(new Font(17));
                issueName.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent t) {
                        Main.hostServices.showDocument("https://github.com/themsou/PDFTeacher/issues/new");
                    }
                });
            issueInfo.getChildren().addAll(issueName);
            issueInfo.setAlignment(Pos.CENTER);

        vBox.getChildren().addAll(logo, name, version, devInfo, gitInfo, issueInfo);
        vBox.setAlignment(Pos.CENTER);


        VBox.setMargin(logo, new Insets(30, 0, 0, 0));
        VBox.setMargin(name, new Insets(20, 0, 0, 0));
        VBox.setMargin(version, new Insets(3, 0, 0, 0));
        VBox.setMargin(devInfo, new Insets(20, 0, 0, 0));
        VBox.setMargin(issueInfo, new Insets(10, 0, 0, 0));

        root.getChildren().addAll(vBox);
    }
}
