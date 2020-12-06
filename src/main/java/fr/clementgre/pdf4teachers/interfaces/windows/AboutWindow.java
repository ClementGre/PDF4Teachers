package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AboutWindow extends Stage {

    public VBox root;
    public Label versionName;
    public Button newRelease;

    public Label developerLabel;
    public Label designerLabel;
    public Label translatorText;
    public Label githubLabel;

    public Label donateLabel;
    public Pane paypalLinkPane;
    public Pane githubSponsorsPane;

    public Label dependenciesLabel;
    public Pane dependenciesLeft;
    public Pane dependenciesRight;

    public Hyperlink liscenselabel;

    @FXML
    public void initialize(){
        Scene scene = new Scene(root);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);

        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setTitle(TR.tr("PDF4Teachers - À Propos"));
        setResizable(false);
        setScene(scene);
        setOnCloseRequest(e -> close());
        StyleManager.putStyle(root, Style.DEFAULT);
        StyleManager.putCustomStyle(root, "otherWindows.css");

        setupUi();
        show();
        Main.window.centerWindowIntoMe(this);

    }

    private void setupUi(){

        setupChildrenHyperlinks(root);

        versionName.setText(TR.tr("Version") + " " + Main.VERSION);

        if(UpdateWindow.newVersion){
            newRelease.setText(TR.tr("Une nouvelle version est disponible !"));
            newRelease.setStyle("-fx-background-color: #e5b100;");
            newRelease.setOnAction(event -> new UpdateWindow());
        }else if(UpdateWindow.newPre){
            newRelease.setText(TR.tr("Une nouvelle avant-première est disponible !"));
            newRelease.setStyle("-fx-background-color: #24bcfe;");
            newRelease.setOnAction(event -> new UpdateWindow());
        }else if(UpdateWindow.error){
            newRelease.setText(TR.tr("Impossible de récupérer la dernière version"));
            newRelease.setStyle("-fx-background-color: #ff3434;");
        }else{
            newRelease.setText(TR.tr("Vous exécutez la dernière version !"));
            newRelease.setStyle("-fx-background-color: #5bd600;");
        }

        developerLabel.setText(TR.tr("Développeur :") + " ");
        designerLabel.setText(TR.tr("Concepteur :") + " ");
        if(!TR.tr("Traducteur : <Votre nom>").equals("Traducteur : <Votre nom>")){
            translatorText.setText(TR.tr("Traducteur : <Votre nom>"));
        }else root.getChildren().remove(translatorText);

        githubLabel.setText(TR.tr("Projet GitHub :") + " ");

        donateLabel.setText(TR.tr("Faire un don : "));
        paypalLinkPane.setPrefWidth(150);
        githubSponsorsPane.setPrefWidth(150);

        dependenciesLabel.setText(TR.tr("Dépendances :"));
        dependenciesLeft.setPrefWidth(160);
        dependenciesRight.setPrefWidth(160);

        liscenselabel.setText(TR.tr("Licence") + " Apache 2");
    }

    private void setupChildrenHyperlinks(Pane parent){
        for(Node node : parent.getChildren()){
            if(node instanceof Hyperlink){
                ((Hyperlink) node).setOnAction(t -> Main.hostServices.showDocument(node.getId()));
            }else if(node instanceof Pane){
                setupChildrenHyperlinks((Pane) node);
            }
        }
    }
}
