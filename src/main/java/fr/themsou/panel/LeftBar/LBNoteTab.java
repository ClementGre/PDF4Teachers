package fr.themsou.panel.LeftBar;

import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LBNoteTab extends Tab {

    public ScrollPane scroller = new ScrollPane();
    public GridPane pane = new GridPane();

    public LBNoteTab(){
        setup();
    }

    public void repaint(){

    }
    public void setup(){

        setClosable(false);
        setContent(scroller);
        scroller.setContent(pane);

        setGraphic(Builders.buildImage(getClass().getResource("/img/Note.png")+"", 0, 25));
        Main.leftBar.getTabs().add(2, this);

    }

}
