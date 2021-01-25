package fr.clementgre.pdf4teachers.panel.sidebar.paint;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class PaintTab extends SideTab {

    public VBox root;
    public VBox optionPane;

    public Button newImage;
    public Button newVector;


    public PaintTab(){
        super(false, SVGPathIcons.DRAW_POLYGON, 28, 30, null);
        MainWindow.paintTab = this;
    }

    @FXML
    public void initialize(){
        setContent(root);
        setup();
    }

    public void setup(){



    }
}
