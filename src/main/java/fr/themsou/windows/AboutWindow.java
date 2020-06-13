package fr.themsou.windows;

import fr.themsou.main.Main;
import fr.themsou.utils.TR;
import fr.themsou.utils.style.Style;
import fr.themsou.utils.style.StyleManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;

public class AboutWindow extends Stage {

    public AboutWindow(){

        VBox root = new VBox();
        Scene scene = new Scene(root, 400, 640);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setTitle(TR.tr("PDF4Teachers - À Propos"));
        setResizable(false);
        setScene(scene);
        setOnCloseRequest(e -> close());
        StyleManager.putStyle(root, Style.DEFAULT);

        setupUi(root);

        show();
    }

    private void setupUi(VBox root){

        VBox vBox = new VBox();

            ImageView logo = new ImageView(getClass().getResource("/logo.png")+"");
            logo.setFitWidth(200);
            logo.setPreserveRatio(true);

            Label name = new Label("PDF4Teachers");
            name.setStyle("-fx-font-size: 23;");
            name.setAlignment(Pos.CENTER);

            Label version = new Label(TR.tr("Version") + " " + Main.VERSION);
            version.setStyle("-fx-font-size: 15;");
            version.setAlignment(Pos.CENTER);

            Button newVersion = null;
            if(UpdateWindow.newVersion){
                newVersion = new Button(TR.tr("Une nouvelle version est disponible !"));
                newVersion.setAlignment(Pos.CENTER);
                newVersion.setStyle("-fx-background-color: #e5b100; -fx-text-fill: black;");

                newVersion.setOnAction(event -> {
                    new UpdateWindow();
                });
            }else if(UpdateWindow.newPre){
                newVersion = new Button(TR.tr("Une nouvelle avant-première est disponible !"));
                newVersion.setAlignment(Pos.CENTER);
                newVersion.setStyle("-fx-background-color: #24bcfe; -fx-text-fill: black;");

                newVersion.setOnAction(event -> {
                    new UpdateWindow();
                });
            }else if(UpdateWindow.error){
                newVersion = new Button(TR.tr("Impossible de récupérer la dernière version"));
                newVersion.setAlignment(Pos.CENTER);
                newVersion.setStyle("-fx-background-color: #ff3434; -fx-text-fill: black;");
            }else{
                newVersion = new Button(TR.tr("Vous exécutez la dernière version !"));
                newVersion.setAlignment(Pos.CENTER);
                newVersion.setStyle("-fx-background-color: #5bd600; -fx-text-fill: black;");
            }

            HBox devInfo = new HBox();
                Label dev = new Label(TR.tr("Développeur :") + " ");
                dev.setStyle("-fx-font-size: 17;");

                Hyperlink devName = new Hyperlink("Clément Grennerat");
                devName.setStyle("-fx-font-size: 17;");
                devName.setOnAction(t -> Main.hostServices.showDocument("https://github.com/clementgre"));
            devInfo.getChildren().addAll(dev, devName);
            devInfo.setAlignment(Pos.CENTER);

            HBox consInfo = new HBox();
                Label cons = new Label(TR.tr("Concepteur :") + " ");
                cons.setStyle("-fx-font-size: 17;");

                Hyperlink consName = new Hyperlink("Vincent Grennerat");
                consName.setStyle("-fx-font-size: 17;");
                consName.setOnAction(t -> Main.hostServices.showDocument("https://github.com/grensv"));
                consInfo.getChildren().addAll(cons, consName);
            consInfo.setAlignment(Pos.CENTER);

            HBox transInfo = new HBox();
                if(!TR.tr("Traducteur : <Votre nom>").equals("Traducteur : <Votre nom>")){
                    Label trans = new Label(TR.tr("Traducteur : <Votre nom>"));
                    trans.setStyle("-fx-font-size: 17;");
                    transInfo.getChildren().add(trans);
                }
            transInfo.setAlignment(Pos.CENTER);

            HBox gitInfo = new HBox();
                Label git = new Label(TR.tr("Projet GitHub :") + " ");
                git.setStyle("-fx-font-size: 17;");

                Hyperlink gitName = new Hyperlink("ClementGre/PDF4Teachers");
                gitName.setStyle("-fx-font-size: 17;");
                gitName.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://github.com/clementgre/PDF4Teachers"));
            gitInfo.getChildren().addAll(git, gitName);
            gitInfo.setAlignment(Pos.CENTER);

            HBox twitterInfo = new HBox();
                Label twitter = new Label("Twitter : ");
                twitter.setStyle("-fx-font-size: 17;");

                Hyperlink twitterName = new Hyperlink("@PDF4Teachers");
                twitterName.setStyle("-fx-font-size: 17;");
                twitterName.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://twitter.com/PDF4Teachers"));
            twitterInfo.getChildren().addAll(twitter, twitterName);
            twitterInfo.setAlignment(Pos.CENTER);

            HBox issueInfo = new HBox();
                Hyperlink issueName = new Hyperlink(TR.tr("Demander de l'aide ou signaler un Bug"));
                issueName.setStyle("-fx-font-size: 17;");
                issueName.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://github.com/clementgre/PDF4Teachers/issues/new"));
                issueInfo.getChildren().addAll(issueName);
            issueInfo.setAlignment(Pos.CENTER);

            VBox apiInfo = new VBox();
                Label api = new Label(TR.tr("Dépendances :"));

                GridPane apis = new GridPane();

                    Hyperlink javaFx = new Hyperlink("Java FX 14");
                    javaFx.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://openjfx.io/"));

                    Hyperlink pdfBox = new Hyperlink("Apache PDFBox 2.0.20");
                    pdfBox.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://pdfbox.apache.org/"));

                    Hyperlink jMetro = new Hyperlink("JMetro 11.6.11");
                    jMetro.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://pixelduke.com/java-javafx-theme-jmetro/"));

                    Hyperlink latex = new Hyperlink("JLatexMath 1.0.7");
                    latex.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://github.com/opencollab/jlatexmath"));

                    Hyperlink yaml = new Hyperlink("SnakeYAML 1.26");
                    yaml.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://bitbucket.org/asomov/snakeyaml/src/master/"));

                    Hyperlink json = new Hyperlink("Jackson Streaming API 2.10.3");
                    json.setOnAction((ActionEvent t) -> Main.hostServices.showDocument("https://github.com/FasterXML/jackson-core"));

                apis.add(javaFx, 0, 0); apis.add(pdfBox, 1, 0);
                apis.add(jMetro, 0, 1); apis.add(latex, 1, 1);
                apis.add(yaml, 0, 2); apis.add(json, 1, 2);
                apis.setAlignment(Pos.CENTER);
                apis.setHgap(20);

            apiInfo.getChildren().addAll(api, apis);
            apiInfo.setAlignment(Pos.CENTER);

        vBox.getChildren().addAll(logo, name, version);
        if(newVersion != null) vBox.getChildren().add(newVersion);
        vBox.getChildren().addAll(devInfo, consInfo, transInfo, gitInfo, twitterInfo, issueInfo, apiInfo);
        vBox.setAlignment(Pos.CENTER);

        VBox.setMargin(logo, new Insets(20, 0, 0, 0));
        VBox.setMargin(name, new Insets(5, 0, 0, 0));
        VBox.setMargin(version, new Insets(0, 0, 7, 0));
        VBox.setMargin(devInfo, new Insets(7, 0, 0, 0));
        VBox.setMargin(issueInfo, new Insets(10, 0, 15, 0));

        root.getChildren().addAll(vBox);
    }
}
