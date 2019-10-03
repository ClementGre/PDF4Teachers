package fr.themsou.panel.LeftBar;

import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;

@SuppressWarnings("serial")
public class LBPaintTab extends Tab {

    public ScrollPane scroller = new ScrollPane();
    public GridPane pane = new GridPane();

    public LBPaintTab(){

        setClosable(false);
        setContent(scroller);
        scroller.setContent(pane);

        setGraphic(Builders.buildImage(getClass().getResource("/img/Paint.png")+"", 0, 25));
        Main.leftBar.getTabs().add(3, this);

        setup();
        repaint();
    }

    public void repaint(){

    }
    public void setup(){

    }
	
}
