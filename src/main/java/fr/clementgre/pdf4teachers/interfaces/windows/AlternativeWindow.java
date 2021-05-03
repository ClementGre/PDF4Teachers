package fr.clementgre.pdf4teachers.interfaces.windows;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.fonts.AppFontsLoader;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class AlternativeWindow<R extends Node> extends Stage{
    
    private VBox container = new VBox();
    private VBox header = new VBox();
    public R root;
    private Scene scene = new Scene(container);
    
    private Label headerText = new Label();
    private Label subHeaderText = new Label();
    
    public AlternativeWindow(R root, String title){
        this(root, title, title, null);
    }
    public AlternativeWindow(R root, String title, String header){
        this(root, title, header, null);
    }
    public AlternativeWindow(R root, String title, String header, String subHeader){
        this.root = root;
        
        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png") + ""));
        
        setTitle("PDF4Teachers - " + title);
        setScene(scene);
        StyleManager.putStyle(container, Style.DEFAULT);
        
        setup(header, subHeader);
        show();
    }
    
    private void setup(String header, String subHeader){
        
        AppFontsLoader.loadFont("Marianne-Bold.otf");
        AppFontsLoader.loadFont("Marianne-Regular.otf");
        headerText.setStyle("-fx-font: 35 Marianne; -fx-font-weight: 700;");
        subHeaderText.setStyle("-fx-font: 16 Marianne; -fx-font-weight: 400;");
        
        VBox.setMargin(headerText, new Insets(30, 20, 5, 20));
        VBox.setMargin(subHeaderText, new Insets(0, 20, 30, 20));
        
        headerText.setWrapText(true);
        subHeaderText.setWrapText(true);
        
        setHeaderText(header);
        setSubHeaderText(subHeader);
        this.header.getChildren().addAll(headerText, subHeaderText);
        container.getChildren().addAll(this.header, root);
    }
    
    public void setHeaderText(String text){
        headerText.setText(text);
    }
    public void setSubHeaderText(String text){
        subHeaderText.setText(text);
        if(text == null){
        
        }else{
        
        }
    }
    
}
