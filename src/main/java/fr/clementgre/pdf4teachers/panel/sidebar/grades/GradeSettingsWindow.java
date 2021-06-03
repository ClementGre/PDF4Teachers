package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.components.ScaledComboBox;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.components.FontComboBox;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToDoubleConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class GradeSettingsWindow extends AlternativeWindow<HBox>{

    public GradeSettingsWindow(){
        super(new HBox(), StageWidth.ULTRA_LARGE, TR.tr("gradeTab.gradeFormatWindow.title"),
                TR.tr("gradeTab.gradeFormatWindow.title"), TR.tr("gradeTab.gradeFormatWindow.header"));
    }

    @Override
    public void setupSubClass(){
    
        for(int i = 0; i < 5; i++)
            root.getChildren().add(new TierPane(i));
        
        setupBtns();
    }
    
    public void setupBtns(){
        
        Button ok = new Button(TR.tr("actions.ok"));
        ok.setOnAction(event -> {
            close();
        });
    
        setButtons(ok);
    }
    
    @Override
    public void afterShown(){
    
    }
    
    private static class TierPane extends VBox{

        private final int tier;

        private final FontComboBox fontCombo = new FontComboBox(false);
        private final ToggleButton boldBtn = new ToggleButton();
        private final ToggleButton itBtn = new ToggleButton();
        private final Spinner<Double> sizeCombo = new Spinner<>(2d, 999d, 14d, 2d);
        private final SyncColorPicker colorPicker = new SyncColorPicker();
        private final CheckBox showName = new CheckBox(TR.tr("gradeTab.gradeFormatWindow.options.showGradeName"));
        private final CheckBox hide = new CheckBox(TR.tr("gradeTab.gradeFormatWindow.options.hideGrade"));
        private final CheckBox hideWhenAllPoints = new CheckBox(TR.tr("gradeTab.gradeFormatWindow.options.hideGradeWhenAllPoints"));

        public TierPane(int tier){
            this.tier = tier;

            setStyle("-fx-padding: 2.5;");
            Font font = GradeTab.getTierFont(tier);

            Label name = new Label(TR.tr("gradeTab.gradeFormatWindow.tiers") + " " + (tier + 1) + " :");
            name.setStyle("-fx-font-size: 13");
            VBox.setMargin(name, new Insets(7, 0, 7, 2.5));

            PaneUtils.setVBoxPosition(fontCombo, 0, 30, 2.5);
            fontCombo.setMaxWidth(250);
            fontCombo.setStyle("-fx-font-size: 13");
            fontCombo.getSelectionModel().select(font.getFamily());
            fontCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());

            HBox fontSpecs = new HBox();
            fontSpecs.setSpacing(2.5);
            VBox.setMargin(fontSpecs, new Insets(0, 2.5, 0, 2.5));

            PaneUtils.setHBoxPosition(boldBtn, 40, 29, 0);
            boldBtn.setCursor(Cursor.HAND);
            boldBtn.setSelected(FontUtils.getFontWeight(font) == FontWeight.BOLD);
            boldBtn.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/TextTab/bold.png") + "", 0, 0, ImageUtils.defaultFullDarkColorAdjust));
            boldBtn.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            PaneUtils.setHBoxPosition(itBtn, 40, 29, 0);
            itBtn.setCursor(Cursor.HAND);
            itBtn.setSelected(FontUtils.getFontPosture(font) == FontPosture.ITALIC);
            itBtn.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/TextTab/italic.png") + "", 0, 0, ImageUtils.defaultFullDarkColorAdjust));
            itBtn.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            PaneUtils.setVBoxPosition(sizeCombo, 0, 30, 0);
            sizeCombo.setStyle("-fx-font-size: 13");
            sizeCombo.setMaxWidth(165);
            sizeCombo.setEditable(true);
            sizeCombo.getValueFactory().setConverter(new StringToDoubleConverter(font.getSize()));
            sizeCombo.getValueFactory().setValue(font.getSize());
            sizeCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());

            fontSpecs.getChildren().addAll(boldBtn, itBtn, sizeCombo);

            PaneUtils.setVBoxPosition(colorPicker, 0, 30, 2.5);
            colorPicker.setStyle("-fx-font-size: 13");
            colorPicker.setMaxWidth(250);
            colorPicker.setValue(GradeTab.getTierColor(tier));
            colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> updateFont());

            PaneUtils.setVBoxPosition(showName, 0, 0, new Insets(5, 2.5, 5, 2.5));
            showName.setSelected(GradeTab.getTierShowName(tier));
            showName.setCursor(Cursor.HAND);
            showName.setWrapText(true);
            showName.setAlignment(Pos.TOP_LEFT);
            showName.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            showName.disableProperty().bind(hide.selectedProperty());
            hideWhenAllPoints.disableProperty().bind(hide.selectedProperty());
            hide.disableProperty().bind(hideWhenAllPoints.selectedProperty());

            PaneUtils.setVBoxPosition(hide, 0, 0, new Insets(5, 2.5, 5, 2.5));
            hide.setSelected(GradeTab.getTierHide(tier));
            hide.setCursor(Cursor.HAND);
            hide.setWrapText(true);
            hide.setAlignment(Pos.TOP_LEFT);
            hide.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            PaneUtils.setVBoxPosition(hideWhenAllPoints, 0, 0, new Insets(5, 2.5, 5, 2.5));
            hideWhenAllPoints.setSelected(GradeTab.getTierHideWhenAllPoints(tier));
            hideWhenAllPoints.setCursor(Cursor.HAND);
            hideWhenAllPoints.setWrapText(true);
            hideWhenAllPoints.setAlignment(Pos.TOP_LEFT);
            hideWhenAllPoints.selectedProperty().addListener((observable, oldValue, newValue) -> updateFont());

            getChildren().addAll(name, fontCombo, fontSpecs, colorPicker, showName, hide, hideWhenAllPoints);

        }

        private void updateFont(){

            GradeTab.fontTiers.put(tier, new TiersFont(
                    FontUtils.getFont(fontCombo.getSelectionModel().getSelectedItem(), itBtn.isSelected(), boldBtn.isSelected(), sizeCombo.getValue()),
                    colorPicker.getValue(), showName.isSelected(), hide.isSelected(), hideWhenAllPoints.isSelected())); // Color + ShowName
            MainWindow.gradeTab.updateElementsFont();

        }

    }
}
