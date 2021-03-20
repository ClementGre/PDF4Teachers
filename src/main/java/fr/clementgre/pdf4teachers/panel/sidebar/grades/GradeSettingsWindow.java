package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTab;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToDoubleConverter;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.FontUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
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

public class GradeSettingsWindow extends Stage {

    public GradeSettingsWindow(){

        VBox root = new VBox();
        Scene scene = new Scene(root);

        initOwner(Main.window);
        initModality(Modality.WINDOW_MODAL);
        getIcons().add(new Image(getClass().getResource("/logo.png")+""));
        setTitle(TR.tr("gradeTab.gradeFormatWindow.title"));
        setResizable(false);
        setScene(scene);
        StyleManager.putStyle(root, Style.DEFAULT);

        setupPanel(root);
        show();
    }

    public void setupPanel(VBox root){

        Text info = new Text(TR.tr("gradeTab.gradeFormatWindow.header"));
        VBox.setMargin(info, new Insets(40, 0, 40, 0));

        root.setStyle("-fx-padding: 10;");
        root.getChildren().add(info);

        for(int i = 0; i < 5 ; i++){
            root.getChildren().add(new TierPane(i));
        }

    }

    private class TierPane extends HBox{

        private final int tier;

        private ComboBox<String> fontCombo = new ComboBox<>(FontUtils.fonts);
        private ToggleButton boldBtn = new ToggleButton();
        private ToggleButton itBtn = new ToggleButton();
        private ComboBox<Double> sizeCombo = new ComboBox<>(FontUtils.sizes);
        private SyncColorPicker colorPicker = new SyncColorPicker();
        private CheckBox showName = new CheckBox(TR.tr("gradeTab.gradeFormatWindow.options.showGradeName"));
        private CheckBox hide = new CheckBox(TR.tr("gradeTab.gradeFormatWindow.options.hideGrade"));

        public TierPane(int tier){
            this.tier = tier;

            setStyle("-fx-padding: 2.5;");
            Font font = GradeTab.getTierFont(tier);

            Label name = new Label(TR.tr("gradeTab.gradeFormatWindow.tiers") + " " + (tier+1) + " :");
            name.setStyle("-fx-font-size: 13");
            HBox.setMargin(name, new Insets(7));

            PaneUtils.setHBoxPosition(fontCombo, 150, 30, 2.5);
            fontCombo.setStyle("-fx-font-size: 13");
            fontCombo.getSelectionModel().select(font.getFamily());
            fontCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());
            fontCombo.setCellFactory((ListView<String> stringListView) -> new TextTab.ShapeCell());

            PaneUtils.setHBoxPosition(sizeCombo, 80, 30, 2.5);
            sizeCombo.setStyle("-fx-font-size: 13");
            sizeCombo.setEditable(true);
            sizeCombo.getSelectionModel().select(font.getSize());
            sizeCombo.setConverter(new StringToDoubleConverter(sizeCombo.getValue()));
            sizeCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());

            PaneUtils.setHBoxPosition(colorPicker, 120, 30, 2.5);
            colorPicker.setStyle("-fx-font-size: 13");
            colorPicker.setValue(GradeTab.getTierColor(tier));
            colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());

            PaneUtils.setHBoxPosition(boldBtn, 45, 29, 2.5);
            boldBtn.setCursor(Cursor.HAND);
            boldBtn.setSelected(FontUtils.getFontWeight(font) == FontWeight.BOLD);
            boldBtn.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/TextTab/bold.png")+"", 0, 0));
            boldBtn.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            PaneUtils.setHBoxPosition(itBtn, 45, 29, 2.5);
            itBtn.setCursor(Cursor.HAND);
            itBtn.setSelected(FontUtils.getFontPosture(font) == FontPosture.ITALIC);
            itBtn.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/TextTab/italic.png")+"", 0, 0));
            itBtn.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            PaneUtils.setHBoxPosition(showName, 0, 29, 2.5);
            showName.setSelected(GradeTab.getTierShowName(tier));
            showName.setCursor(Cursor.HAND);
            showName.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());
            showName.disableProperty().bind(hide.selectedProperty());

            PaneUtils.setHBoxPosition(hide, 0, 29, 2.5);
            hide.setSelected(GradeTab.getTierHide(tier));
            hide.setCursor(Cursor.HAND);
            hide.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            getChildren().addAll(name, fontCombo, boldBtn, itBtn, sizeCombo, colorPicker, showName, hide);

        }

        private void updateFont(){

            GradeTab.fontTiers.put(tier, new TiersFont(
                    Font.loadFont(FontUtils.getFontFile(fontCombo.getSelectionModel().getSelectedItem(), itBtn.isSelected(), boldBtn.isSelected()), sizeCombo.getSelectionModel().getSelectedItem()),
                    colorPicker.getValue(), showName.isSelected(), hide.isSelected())); // Color + ShowName
            MainWindow.gradeTab.updateElementsFont();

        }

    }
}
