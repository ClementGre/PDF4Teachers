package fr.themsou.panel.leftBar.notes.export;

import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.notes.NoteRating;
import fr.themsou.panel.leftBar.notes.NoteTreeView;
import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class NoteExportRenderer {

    String text = "";

    public ArrayList<NoteRating> ratingScale;
    ArrayList<ExportFile> files = new ArrayList<>();
    int exportTier;

    int exported = 0;
    boolean mkdirs = true;
    boolean erase = false;

    NoteExportWindow.ExportPane pane;
    public NoteExportRenderer(NoteExportWindow.ExportPane pane){
        this.pane = pane;
        this.exportTier = (int) pane.settingsTiersExportSlider.getValue();

        if(Main.mainScreen.hasDocument(false)) Main.mainScreen.document.save();

    }

    public int start(){

        if(!getFiles()){
            return exported;
        }

        if(pane.type != 1){
            try{
                if(pane.settingsAttributeTotalLine.isSelected()){
                    generateNamesLine(false);
                    generateRatingScaleLine();
                }else{
                    generateNamesLine(true);
                }
                if(pane.settingsAttributeMoyLine.isSelected()){
                    generateMoyLine();
                }

                for(ExportFile file : files){
                    generateStudentLine(file);
                }

                if(!save(null)) return exported;
            }catch(Exception e){
                e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                Builders.secureAlert(alert);
                alert.setTitle(TR.tr("Erreur de rendu"));
                alert.setHeaderText(TR.tr("Une erreur s'est produite lors de la génération du document"));
                alert.setContentText(TR.tr("Impossible d'exporter."));

                TextArea textArea = new TextArea(e.getMessage());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(new Label(TR.tr("L'erreur survenue est la suivante :")), 0, 0);
                expContent.add(textArea, 0, 1);
                alert.getDialogPane().setExpandableContent(expContent);

                alert.getButtonTypes().setAll(new ButtonType(TR.tr("Ok"), ButtonBar.ButtonData.OK_DONE));
                alert.showAndWait();
                return exported;
            }
        }else{ // SPLIT

            for(ExportFile file : files){
                try{
                    if(pane.settingsAttributeTotalLine.isSelected()){
                        generateNamesLine(false);
                        generateRatingScaleLine();
                    }else{
                        generateNamesLine(true);

                    }
                    generateStudentLine(file);

                    if(!save(file)) return exported;

                }catch(Exception e){
                    e.printStackTrace();

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    new JMetro(alert.getDialogPane(), Style.LIGHT);
                    Builders.secureAlert(alert);
                    alert.setTitle(TR.tr("Erreur de rendu"));
                    alert.setHeaderText(TR.tr("Une erreur s'est produite lors de la génération du document " + file.file.getName()));
                    alert.setContentText(TR.tr("Choisissez une action."));

                    TextArea textArea = new TextArea(e.getMessage());
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    GridPane expContent = new GridPane();
                    expContent.setMaxWidth(Double.MAX_VALUE);
                    expContent.add(new Label(TR.tr("L'erreur survenue est la suivante :")), 0, 0);
                    expContent.add(textArea, 0, 1);
                    alert.getDialogPane().setExpandableContent(expContent);

                    ButtonType stopAll = new ButtonType(TR.tr("Arreter tout"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType continueRender = new ButtonType(TR.tr("Continuer"), ButtonBar.ButtonData.NEXT_FORWARD);
                    alert.getButtonTypes().setAll(stopAll, continueRender);

                    Optional<ButtonType> option = alert.showAndWait();
                    if(option.get() == stopAll){
                        return exported;
                    }
                }
            }

        }
        return exported;
    }

    // GENERATORS

    public void generateNamesLine(boolean includeRatingScale){

        text += TR.tr("Parties");

        for(NoteRating rating : ratingScale){
            text += ";" + rating.name + (includeRatingScale ? " /" + rating.total : "");
        }
        text += "\n";
    }
    public void generateRatingScaleLine(){

        text += TR.tr("Barème");

        for(NoteRating rating : ratingScale){
            text += ";" + rating.total;
        }
        text += "\n";

    }
    public void generateMoyLine(){

        char x = 'B';
        int startY = pane.settingsAttributeTotalLine.isSelected() ? 4 : 3;
        int endY = startY + files.size()-1;

        text += TR.tr("Moyenne");

        for(NoteRating rating : ratingScale){
            text += ";=AVERAGE(" + x + startY + ":" + x + endY + ")";
            x++;
        }
        text += "\n";
    }
    public void generateStudentLine(ExportFile file){

        if(pane.studentNameSimple != null){
            text += pane.studentNameSimple.getText();
        }else{
            text += StringUtils.removeAfterLastRejex(file.file.getName(), ".pdf").replaceAll(Pattern.quote(pane.studentNameReplace.getText()), pane.studentNameBy.getText());
        }

        for(NoteElement note : file.notes){
            text += ";" + (note.getValue() == -1 ? "" : note.getValue());
        }
        text += "\n";

        if(pane.settingsWithTxtElements.isSelected()){
            generateCommentsLines(file);
        }
    }
    public void generateCommentsLines(ExportFile file){

        text += TR.tr("Commentaires");

        if(file.comments.size() >= 1){
            ArrayList<String> lines = new ArrayList<>();

            file.comments.sort((element1, element2) ->
                    (element2.getPageNumber()-9999 + "" + (element2.getRealY()-9999) + "" + (element2.getRealX()-9999))
                            .compareToIgnoreCase(element1.getPageNumber()-9999 + "" + (element1.getRealY()-9999) + "" + (element1.getRealX()-9999)));

            for(int i = 1 ; i < file.notes.size(); i++){
                NoteElement note = file.notes.get(i);
                int maxPage = note.getPageNumber();
                int maxY = note.getRealY();

                TextElement element = file.comments.size() > 0 ? file.comments.get(0) : null;
                int k = -1;
                while(element != null){

                    if(element.getPageNumber() <= maxPage && element.getRealY() < maxY){
                        k++;
                        if(lines.size() > k){
                            lines.set(k, lines.get(k) + ";" + element.getText().replaceAll(Pattern.quote("\n"), " "));
                        }else{
                            lines.add(";" + element.getText().replaceAll(Pattern.quote("\n"), ""));
                        }

                        file.comments.remove(0);
                        element = file.comments.size() > 0 ? file.comments.get(0) : null;
                    }else{
                        element = null;
                    }
                }
                for(k++; k < 20; k++){
                    if(lines.size() > k){
                        lines.set(k, lines.get(k) + ";");
                    }else{
                        lines.add(";");
                    }
                }
            }
            int k = 0;
            for(TextElement element : file.comments){
                if(lines.size() > k){
                    lines.set(k, lines.get(k) + ";" + element.getText().replaceAll(Pattern.quote("\n"), " "));
                }else{
                    lines.add(";" + element.getText().replaceAll(Pattern.quote("\n"), ""));
                }
                k++;
            }

            for(String line : lines){
                text += line + "\n";
            }
        }else text += "\n";
    }

    // OTHERS

    public boolean getFiles(){

        try {
            ExportFile defaultFile = new ExportFile(Main.mainScreen.document.getFile(), exportTier, pane.settingsWithTxtElements.isSelected());

            ratingScale = defaultFile.generateRatingScale();
            if(!(pane.settingsOnlyCompleted.isSelected() && !defaultFile.isCompleted())) files.add(defaultFile);
        }catch(Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            Builders.secureAlert(alert);
            alert.setTitle(TR.tr("Impossible de lire les notes"));
            alert.setHeaderText(TR.tr("Une erreur de rendu s'est produite lors de la lecture des notes du document : ") + Main.mainScreen.document.getFileName());
            alert.setContentText(TR.tr("Ce document est le document principale de l'exportation, l'exportation ne peut pas continuer."));

            TextArea textArea = new TextArea(e.getMessage());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(new Label(TR.tr("L'erreur survenue est la suivante :")), 0, 0);
            expContent.add(textArea, 0, 1);
            alert.getDialogPane().setExpandableContent(expContent);

            alert.getButtonTypes().add(new ButtonType(TR.tr("Ok"), ButtonBar.ButtonData.CANCEL_CLOSE));
            alert.showAndWait();
            return false;
        }


        if(pane.type != 2){
            for(File file : Main.lbFilesTab.files.getItems()){

                try{
                    if(Main.mainScreen.document.getFile().equals(file)) continue;
                    if(pane.settingsOnlySameDir.isSelected() && !Main.mainScreen.document.getFile().getParent().equals(file.getParent())) continue;

                    ExportFile exportFile = new ExportFile(file, exportTier, pane.settingsWithTxtElements.isSelected());

                    if(pane.settingsOnlySameRatingScale.isSelected() && !exportFile.isSameRatingScale(ratingScale)) continue;
                    if(pane.settingsOnlyCompleted.isSelected() && !exportFile.isCompleted()) continue;
                    files.add(exportFile);

                }catch(Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    new JMetro(alert.getDialogPane(), Style.LIGHT);
                    Builders.secureAlert(alert);
                    alert.setTitle(TR.tr("Impossible de lire les notes"));
                    alert.setHeaderText(TR.tr("Une erreur de rendu s'est produite lors de la lecture des notes du document : ") + file.getName());
                    alert.setContentText(TR.tr("Choisissez une action."));

                    TextArea textArea = new TextArea(e.getMessage());
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    GridPane expContent = new GridPane();
                    expContent.setMaxWidth(Double.MAX_VALUE);
                    expContent.add(new Label(TR.tr("L'erreur survenue est la suivante :")), 0, 0);
                    expContent.add(textArea, 0, 1);
                    alert.getDialogPane().setExpandableContent(expContent);

                    ButtonType stopAll = new ButtonType(TR.tr("Arreter tout"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType continueRender = new ButtonType(TR.tr("Continuer"), ButtonBar.ButtonData.NEXT_FORWARD);
                    alert.getButtonTypes().setAll(stopAll, continueRender);

                    Optional<ButtonType> option = alert.showAndWait();
                    if (option.get() == stopAll) return false;
                    continue;
                }
            }
        }
        return true;
    }

    public boolean save(ExportFile source) throws IOException {

        System.out.println("Saving file, text = ");
        System.out.println(text);

        String filePath = pane.filePath.getText();
        String fileName;

        if(source != null){ // type = 1 -> Splited export
            fileName = pane.fileNamePrefix.getText() + StringUtils.removeAfterLastRejex(source.file.getName(), ".pdf")
                    .replaceAll(Pattern.quote(pane.fileNameReplace.getText()), pane.fileNameBy.getText()) + pane.fileNameSuffix.getText();
        }else{ // other
            fileName = StringUtils.removeAfterLastRejex(pane.fileNameSimple.getText(), ".csv");
        }

        File file = new File(filePath + File.separator + fileName + ".csv");
        file.getParentFile().mkdirs();

        if(!file.createNewFile()){
            switch(fileAlreadyExist(file)) { // 0: Continue | 1: Cancel | 2: Cancel All | 3: ContinueAll | 4: Rename
                case 1:
                    return true;
                case 2:
                    return false;
                case 4:
                    String tmpName = fileName; int i = 1;
                    while(new File(filePath + File.separator + tmpName + ".csv").exists()){
                        tmpName = fileName + " (" + i + ")";
                        i++;
                    }
                    file = new File(filePath + File.separator + tmpName + ".csv");
                    file.createNewFile();
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

        writer.write(text);

        writer.flush();
        writer.close();

        exported++;
        text = "";
        return true;
    }

    public int fileAlreadyExist(File file){

        Alert alert = new Alert(Alert.AlertType.WARNING);
        new JMetro(alert.getDialogPane(), Style.LIGHT);
        alert.setTitle(TR.tr("Fichier déjà existant"));
        alert.setHeaderText(TR.tr("Le fichier de destination \"") + file.getAbsolutePath() + TR.tr("\" existe déjà"));
        alert.setContentText(TR.tr("Voulez-vous l'écraser ?"));
        ButtonType yesButton = new ButtonType(TR.tr("Écraser"), ButtonBar.ButtonData.YES);
        ButtonType yesAlwaysButton = new ButtonType(TR.tr("Toujours écraser"), ButtonBar.ButtonData.YES);
        ButtonType renameButton = new ButtonType(TR.tr("Renomer"), ButtonBar.ButtonData.OTHER);
        ButtonType cancelButton = new ButtonType(TR.tr("Sauter"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType cancelAllButton = new ButtonType(TR.tr("Tout Arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, yesAlwaysButton, renameButton, cancelButton, cancelAllButton);
        Builders.secureAlert(alert);
        Optional<ButtonType> option = alert.showAndWait();
        if(option.get() == cancelAllButton){
            return 2;
        }else if(option.get() == cancelButton){
            return 1;
        }else if(option.get() == yesButton){
            return 0;
        }else if(option.get() == yesAlwaysButton){
            erase = true;
            return 3;
        }else if(option.get() == renameButton){
            return 4;
        }
        return 1;

    }

}
