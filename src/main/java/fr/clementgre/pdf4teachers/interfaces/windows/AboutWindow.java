/*
 * Copyright (c) 2019-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.StagesUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
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
    public Label statsLabel;
    
    @FXML
    public void initialize(){
        Scene scene = new Scene(root);
        
        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        setTitle(TR.tr("aboutWindow.title"));
        setResizable(false);
        setScene(scene);
        setOnCloseRequest(e -> close());
        StyleManager.putStyle(root, Style.DEFAULT);
        StyleManager.putCustomStyle(root, "otherWindows.css");
        PaneUtils.setupScaling(root, true, false);
        scene.setFill(Color.web("#252525"));
        
        setOnShown((e) -> {
            StagesUtils.resizeStageAccordingToAppScale(this, scene);
            Main.window.centerWindowIntoMe(this);
            MainWindow.preventWindowOverflowScreen(this, MainWindow.getScreen().getVisualBounds());
        });
        
        setupUi();
        Main.window.centerWindowIntoMe(this);
        show();
    }
    
    private void setupUi(){
        
        setupChildrenHyperlinks(root);
        
        versionName.setText(TR.tr("aboutWindow.version", Main.VERSION));
        
        if(UpdateWindow.newVersion){
            newRelease.setText(TR.tr("aboutWindow.version.update.available"));
            newRelease.setStyle("-fx-background-color: #e5b100;");
            newRelease.setOnAction(event -> new UpdateWindow());
        }else if(UpdateWindow.newPre){
            newRelease.setText(TR.tr("aboutWindow.version.update.preRelease"));
            newRelease.setStyle("-fx-background-color: #24bcfe;");
            newRelease.setOnAction(event -> new UpdateWindow());
        }else if(UpdateWindow.error){
            newRelease.setText(TR.tr("aboutWindow.version.update.error"));
            newRelease.setStyle("-fx-background-color: #ff3434;");
        }else{
            newRelease.setText(TR.tr("aboutWindow.version.update.good"));
            newRelease.setStyle("-fx-background-color: #5bd600;");
        }
        
        developerLabel.setText(TR.tr("aboutWindow.info.developer") + " ");
        designerLabel.setText(TR.tr("aboutWindow.info.designer") + " ");
        if(!TR.tr("aboutWindow.info.translator").equals("Traducteur : <Votre nom>")){
            translatorText.setText(TR.tr("aboutWindow.info.translator"));
        }else root.getChildren().remove(translatorText);
        
        githubLabel.setText(TR.tr("aboutWindow.info.gitHubProject") + " ");
        
        donateLabel.setText(TR.tr("aboutWindow.info.donate") + " ");
        paypalLinkPane.setPrefWidth(150);
        githubSponsorsPane.setPrefWidth(150);
        
        dependenciesLabel.setText(TR.tr("aboutWindow.info.dependencies"));
        dependenciesLeft.setPrefWidth(160);
        dependenciesRight.setPrefWidth(160);
        
        liscenselabel.setText(TR.tr("aboutWindow.info.license", "Apache 2"));
        
        statsLabel.setText(TR.tr("aboutWindow.statistics", MainWindow.twoDigFormat.format(MainWindow.userData.foregroundTime / 61d), String.valueOf(MainWindow.userData.startsCount)));
        statsLabel.setWrapText(true);
        statsLabel.setTextAlignment(TextAlignment.CENTER);
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
