package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.texts.LBTextTab;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.util.Map;

public class NoteSettingsWindow extends Stage {

    public NoteSettingsWindow(){

        ScrollPane scroller = new ScrollPane();
        VBox root = new VBox();
        scroller.setContent(root);

        Scene scene = new Scene(scroller);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setWidth(750);
        setHeight(390);
        setTitle(TR.tr("PDF4Teachers - Polices et Couleurs des Notes"));
        setScene(scene);
        new JMetro(scene, Style.LIGHT);

        setupPanel(root);
        show();
    }

    public void setupPanel(VBox root){

        Text info = new Text(TR.tr("Configurez les polices pour chaque catégorie de note.") + "\n" + TR.tr("Ces paramètres sonts indépendants de l'édition du document."));
        VBox.setMargin(info, new Insets(40, 0, 40, 0));

        root.setStyle("-fx-padding: 10;");
        root.getChildren().add(info);

        for(int i = 0; i < 5 ; i++){
            root.getChildren().add(new TierPane(i));
        }

    }

    private class TierPane extends HBox{

        private final int tier;

        private ComboBox<String> fontCombo = new ComboBox<>(Element.fonts);
        private ToggleButton boldBtn = new ToggleButton();
        private ToggleButton itBtn = new ToggleButton();
        private ComboBox<Integer> sizeCombo = new ComboBox<>(Element.sizes);
        private ColorPicker colorPicker = new ColorPicker();
        private CheckBox showName = new CheckBox(TR.tr("Afficher le nom de la note"));

        public TierPane(int tier){
            this.tier = tier;

            setStyle("-fx-padding: 2.5;");
            Font font = LBNoteTab.getTierFont(tier);

            Label name = new Label(TR.tr("Niveau") + " " + (tier+1) + " :");
            name.setStyle("-fx-font-size: 13");
            HBox.setMargin(name, new Insets(7));

            Builders.setHBoxPosition(fontCombo, 150, 30, 2.5);
            fontCombo.setStyle("-fx-font-size: 13");
            fontCombo.setCursor(Cursor.HAND);
            fontCombo.getSelectionModel().select(font.getFamily());
            fontCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());
            fontCombo.setCellFactory((ListView<String> stringListView) -> new LBTextTab.ShapeCell());

            Builders.setHBoxPosition(sizeCombo, 60, 30, 2.5);
            sizeCombo.setStyle("-fx-font-size: 13");
            sizeCombo.setCursor(Cursor.HAND);
            sizeCombo.getSelectionModel().select((Integer) ((int) font.getSize()));
            sizeCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());

            Builders.setHBoxPosition(colorPicker, 120, 30, 2.5);
            colorPicker.setStyle("-fx-font-size: 13");
            colorPicker.setCursor(Cursor.HAND);
            colorPicker.setValue(LBNoteTab.getTierColor(tier));
            colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());

            Builders.setHBoxPosition(boldBtn, 45, 29, 2.5);
            boldBtn.setCursor(Cursor.HAND);
            boldBtn.setSelected(Element.getFontWeight(font) == FontWeight.BOLD);
            boldBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/bold.png")+"", 0, 0));
            boldBtn.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            Builders.setHBoxPosition(itBtn, 45, 29, 2.5);
            itBtn.setCursor(Cursor.HAND);
            itBtn.setSelected(Element.getFontPosture(font) == FontPosture.ITALIC);
            itBtn.setGraphic(Builders.buildImage(getClass().getResource("/img/TextTab/italic.png")+"", 0, 0));
            itBtn.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            Builders.setHBoxPosition(showName, 0, 29, 2.5);
            showName.setSelected(LBNoteTab.getTierShowName(tier));
            showName.setCursor(Cursor.HAND);
            showName.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            getChildren().addAll(name, fontCombo, boldBtn, itBtn, sizeCombo, colorPicker, showName);

        }

        private void updateFont(){

            LBNoteTab.fontTiers.put(tier, Map.entry(
                    Font.loadFont(Element.getFontFile(fontCombo.getSelectionModel().getSelectedItem(), itBtn.isSelected(), boldBtn.isSelected()), sizeCombo.getSelectionModel().getSelectedItem()), // Font + Size
                    Map.entry(colorPicker.getValue(), showName.isSelected()))); // Color + ShowName
            MainWindow.lbNoteTab.updateElementsFont();

        }

    }
}
