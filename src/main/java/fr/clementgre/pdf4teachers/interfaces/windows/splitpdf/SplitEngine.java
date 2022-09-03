/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.splitpdf;

import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialogs.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class SplitEngine {
    
    private final SplitWindow splitWindow;
    private final ArrayList<Color> colors = new ArrayList<>();
    
    // Stores the page indice of the first and last page of each section (ordered)
    private ArrayList<Integer> sectionsBounds = new ArrayList<>();
    
    public SplitEngine(SplitWindow splitWindow){
        this.splitWindow = splitWindow;
    }
    
    public void process() throws IOException {
        File out = splitWindow.getOutputDir();
        out.mkdirs();
        
        boolean recursive = sectionsBounds.size() > 2;
    
        AlreadyExistDialogManager alreadyExistDialogManager = new AlreadyExistDialogManager(recursive);
        new TwoStepListAction<>(true, recursive, new TwoStepListInterface<ExportPart, ExportPart>() {
            @Override
            public List<ExportPart> prepare(boolean recursive){
                
                ArrayList<ExportPart> exportParts = new ArrayList<>();
                for(int i = 0; i < sectionsBounds.size(); i+=2){
                    String path = out.getAbsolutePath() + File.separator + splitWindow.getNames()[i/2];
                    if(!path.toLowerCase().endsWith(".pdf")) path += ".pdf";
                    
                    exportParts.add(new ExportPart(new File(path), sectionsBounds.get(i), sectionsBounds.get(i+1)));
                }
                return exportParts;
            }
        
            @Override
            public Map.Entry<ExportPart, Integer> sortData(ExportPart exportPart, boolean recursive){
                
                if(exportPart.output.exists()){ // Check Already Exist
                    AlreadyExistDialogManager.ResultType result = alreadyExistDialogManager.showAndWait(exportPart.output);
                    if(result == AlreadyExistDialogManager.ResultType.SKIP)
                        return Map.entry(exportPart, TwoStepListAction.CODE_SKIP_2); // SKIP
                    else if(result == AlreadyExistDialogManager.ResultType.STOP)
                        return Map.entry(exportPart, TwoStepListAction.CODE_STOP); // STOP
                    else if(result == AlreadyExistDialogManager.ResultType.RENAME)
                        exportPart = new ExportPart(AlreadyExistDialogManager.rename(exportPart.output), exportPart.startIndex, exportPart.endIndex());
                }
            
                return Map.entry(exportPart, TwoStepListAction.CODE_OK);
            }
        
            @Override
            public String getSortedDataName(ExportPart exportPart, boolean recursive){
                return exportPart.output.getName();
            }
        
            @Override
            public TwoStepListAction.ProcessResult completeData(ExportPart exportPart, boolean recursive){
                try{
                    exportPart(exportPart);
                }catch(Exception e){
                    Log.e(e);
                    if(PlatformUtils.runAndWait(() -> new ErrorAlert(TR.tr("exportWindow.dialogs.exportError.header", exportPart.output.getName()), e.getMessage(), recursive).execute())){
                        return TwoStepListAction.ProcessResult.STOP;
                    }
                    if(!recursive){
                        return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                    }
                    return TwoStepListAction.ProcessResult.SKIPPED;
                }
                return TwoStepListAction.ProcessResult.OK;
            }
        
            @Override
            public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive){
                splitWindow.close();
                SideBar.selectTab(MainWindow.filesTab);
            
                String header;
                if(completedSize == 0) header = TR.tr("splitEngine.dialogs.completed.header.noDocument");
                else if(completedSize == 1) header = TR.tr("splitEngine.dialogs.completed.header.oneDocument");
                else header = TR.tr("splitEngine.dialogs.completed.header.multipleDocument", completedSize);
            
                String details;
                String alreadyExistText = !excludedReasons.containsKey(TwoStepListAction.CODE_SKIP_2) ? "" : "\n(" + TR.tr("exportWindow.dialogs.completed.ignored.alreadyExisting", excludedReasons.get(2)) + ")";
                details = TR.tr("splitEngine.dialogs.completed.exported", completedSize, originSize) + alreadyExistText;
            
                DialogBuilder.showAlertWithOpenDirButton(TR.tr("actions.export.completedMessage"), header, details, out.getAbsolutePath());
            }
        });
        
    }
    
    private record ExportPart(File output, int startIndex, int endIndex){}
    
    private void exportPart(ExportPart exportPart) throws IOException{
        PDDocument extracted = MainWindow.mainScreen.document.pdfPagesRender.editor.extractPages(exportPart.startIndex, exportPart.endIndex);
        extracted.save(exportPart.output);
        extracted.close();
        Platform.runLater(() -> {
            MainWindow.filesTab.openFiles(new File[]{exportPart.output});
        });
    }
    
    public void updatePagesColors(CallBack callBack){
        new Thread(() -> {
            for(PageRenderer page : MainWindow.mainScreen.document.getPages()){
                BufferedImage image = MainWindow.mainScreen.document.pdfPagesRender.renderPageBasic(page.getPage(), 12, (int) (12 / page.getRatio()));
                colors.add(averageColor(image));
            }
            final List<Color> uniqueColors = getColorsUnique();
            Platform.runLater(() -> {
                splitWindow.getCustomColors().setAll(uniqueColors);
                callBack.call();
            });
        }, "Detect noticeable pages").start();
    }
    
    // Get uniques colors of pages for propositions in the color chooser
    private List<Color> getColorsUnique(){
    
        List<Color> colors = new ArrayList<>(this.colors);
        
        // Remove similar colors
        for(int i = 0; i < colors.size(); i++){
            Color color = colors.get(i);
            colors = colors.stream()
                    .filter((match) -> getColorDiff(color, match) > .1 || color == match)
                    .toList();
        }
        
        return colors;
    }
    
    // Update the sections bounds
    public int countMatchPages(){
        int lastPage = MainWindow.mainScreen.document.numberOfPages - 1;
        
        if(splitWindow.selection){
            sectionsBounds.clear();
            List<Integer> selected = MainWindow.mainScreen.document.getSelectedPages().stream().sorted().toList();
    
            if(splitWindow.doKeepSelectedPages()) sectionsBounds.add(0);
            else if(selected.get(0) != 0) sectionsBounds.add(0);
    
            int lastSelected = -1;
            for(Integer page : selected){
    
                // Last page not selected -> end of section (and start of next section if option selected).
                if(lastSelected != page-1){
                    sectionsBounds.add(page-1);
    
                    // Reopen a new section right after closing the last one (including the selected page in the next section).
                    if(splitWindow.doKeepSelectedPages()) sectionsBounds.add(page);
                }
                
                // Next page not selected -> start of section
                if(!splitWindow.doKeepSelectedPages() && !selected.contains(page+1) && page+1 <= lastPage){
                    sectionsBounds.add(page+1);
                }
                lastSelected = page;
            }
        }else{
            if(colors.isEmpty()) return -1;
    
            Color match = splitWindow.getColor();
            double sensibility = splitWindow.getSensibility();
            sectionsBounds.clear();
            MainWindow.mainScreen.document.clearSelectedPages();
    
            if(splitWindow.doKeepSelectedPages()) sectionsBounds.add(0);
            int i = 0;
            boolean hasLastPageMatched = true;
            for(Color color : colors){
        
                double diff = getColorDiff(color, match);
                if(diff < sensibility){ // Matched
                    if(!hasLastPageMatched){
                        sectionsBounds.add(i-1); // End of section
                        // Reopen a new section right after closing the last one (including the selected page in the next section).
                        if(splitWindow.doKeepSelectedPages()) sectionsBounds.add(i);
                    }
                    hasLastPageMatched = true;
                    MainWindow.mainScreen.document.addSelectedPage(i);
                }else{ // Not matched
                    if(!splitWindow.doKeepSelectedPages() && hasLastPageMatched) sectionsBounds.add(i); // Start of section
                    hasLastPageMatched = false;
                }
                i++;
            }
        }
        // Close last section if necessary
        if(sectionsBounds.size() % 2 != 0) sectionsBounds.add(lastPage);
        
        if(sectionsBounds.isEmpty()) sectionsBounds = new ArrayList<>(Arrays.asList(0, colors.size()-1));
        return sectionsBounds.size()/2;
    }
    
    // Colors difference between 0 and 1
    private double getColorDiff(Color c1, Color c2){
        return (Math.abs(c1.getRed() - c2.getRed()) + Math.abs(c1.getGreen() - c2.getGreen()) + Math.abs(c1.getBlue() - c2.getBlue())) / 3;
    }
    
    
    public static Color averageColor(BufferedImage bi) {
        long sumr = 0, sumg = 0, sumb = 0;
        
        for(int x = 0; x < bi.getWidth(); x++){
            for (int y = 0; y < bi.getHeight(); y++){
                java.awt.Color pixel = new java.awt.Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        float num = bi.getWidth() * bi.getHeight() * 255; // Switching from range 0-255 to range 0-1
        return Color.color(sumr / num, sumg / num, sumb / num);
    }
}
