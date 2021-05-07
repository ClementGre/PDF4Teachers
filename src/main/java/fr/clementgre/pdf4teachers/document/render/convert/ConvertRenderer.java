package fr.clementgre.pdf4teachers.document.render.convert;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class ConvertRenderer{
    
    
    ArrayList<ConvertedFile> convertedFiles = new ArrayList<>();
    ConvertWindow.ConvertPane convertPane;
    
    public ConvertRenderer(ConvertWindow.ConvertPane convertPane){
        this.convertPane = convertPane;
    }
    
    // entry : String current document name | Double document internal advancement (range 0 ; 1)
    CallBackArg<Map.Entry<String, Double>> documentCallBack;
    public ArrayList<ConvertedFile> start(CallBackArg<Map.Entry<String, Double>> documentCallBack) throws Exception{
        this.documentCallBack = documentCallBack;
        
        String out = convertPane.outDir.getText();
        new File(out).mkdirs();
        if(!out.endsWith(File.separator)) out += File.separator;
        
        if(convertPane.convertDirs){
            File mainDir = new File(convertPane.srcDir.getText());
            for(File dir : Objects.requireNonNull(mainDir.listFiles())){
                if(shouldStop) break;
                
                if(isValidDir(dir)){
                    documentCallBack.call(Map.entry(dir.getName() + ".pdf", -1d));
                    convertFile(Objects.requireNonNull(dir.listFiles()), new File(out + dir.getName() + ".pdf"));
                }else if(isValidFile(dir) && convertPane.convertAloneFiles.isSelected()){
                    String imgName = StringUtils.removeBeforeLastRegex(dir.getName(), ".");
                    documentCallBack.call(Map.entry(imgName + ".pdf", -1d));
                    convertFile(new File[]{dir}, new File(out + imgName + ".pdf"));
                }
            }
        }else{
            documentCallBack.call(Map.entry(StringUtils.removeAfterLastRegex(convertPane.docName.getText(), ".pdf") + ".pdf", 0d));
            File[] files = new File[convertPane.srcFiles.getText().split(Pattern.quote("\n")).length];
            int i = 0;
            for(String filePath : convertPane.srcFiles.getText().split(Pattern.quote("\n"))){
                files[i] = new File(filePath);
                i++;
            }
            convertFile(files, new File(out + StringUtils.removeAfterLastRegex(convertPane.docName.getText(), ".pdf") + ".pdf"));
        }
        documentCallBack.call(Map.entry("", 0d));
    
        if(shouldStop){
            closeAll();
            return new ArrayList<>();
        }
        return convertedFiles;
    }
    
    private void closeAll(){
        for(ConvertedFile file : convertedFiles){
            try{
                file.document.close();
            }catch(IOException e){ e.printStackTrace(); }
        }
    }
    
    private void convertFile(File[] files, File out) throws IOException{
        
        
        ConvertedFile convertedFile = new ConvertedFile(out);
        /*if(convertPane.convertToExistingDoc){
            convertedFile = new ConvertedFile(out);
        }else{
            convertedFile = new ConvertedFile(out, MainWindow.mainScreen.document.pdfPagesRender.getDocument());
        }*/
        
        double pageHeight = 841; // the page height is always 841 (A4 72dpi)
        double pageWidth = convertPane.widthFactor * pageHeight / convertPane.heightFactor;
        PDRectangle pageSize = new PDRectangle((float) pageWidth, (float) pageHeight);
        PDRectangle defaultPageSize = new PDRectangle((float) pageWidth, (float) pageHeight);
        if(convertPane.format.getEditor().getText().equals(TR.tr("convertWindow.options.format.fitToImage")))
            defaultPageSize = PDRectangle.A4;
        
        int index = 0;
        for(File file : files){
            if(shouldStop) return;
            
            documentCallBack.call(Map.entry(out.getName(), ((double) index) / files.length));
            index++;
            
            if(isGoodFormat(file)){
                // load page and image
                
                PDImageXObject pdImage = PDImageXObject.createFromFileByContent(file, convertedFile.document);
                
                if(convertPane.format.getEditor().getText().equals(TR.tr("convertWindow.options.format.fitToImage"))){
                    // redefine the page size with the image size
                    pageWidth = pdImage.getWidth() * pageHeight / pdImage.getHeight();
                    pageSize = new PDRectangle((float) pageWidth, (float) pageHeight);
                }
                
                PDPage page = new PDPage(pageSize);
                PDPageContentStream contentStream = new PDPageContentStream(convertedFile.document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                
                /////////////// DEFINE IMAGE SIZE AND SHIFT ON PAGE + IMAGE DEFINITION ///////////////////
                
                // define image size on page by maximizing the width
                double byWidthFactor = pageWidth / pdImage.getWidth();
                double height = pdImage.getHeight() * byWidthFactor;
                double width = pageWidth;
                
                if(height > pageHeight){
                    // the height is to big, we need to maximize by height and not by width
                    double byHeightFactor = pageHeight / pdImage.getHeight();
                    width = (float) (pdImage.getWidth() * byHeightFactor);
                    height = pageHeight;
                    
                    // resize image only if we don't adapt
                    if(!convertPane.definition.getEditor().getText().equals(TR.tr("convertWindow.options.format.fitToImage"))){
                        // set image resolution by height
                        if(convertPane.height < pdImage.getHeight()){ // don't redefine size if the image has a less quality than we want, we want to reduce image size, not increase it
                            int imagePixelsWidth = (int) (((double) pdImage.getWidth()) / pdImage.getHeight() * convertPane.height); // calculate image resolution width
                            // resize image
                            pdImage = JPEGFactory.createFromImage(convertedFile.document, scaleImage(pdImage.getImage(), imagePixelsWidth, convertPane.height));
                        }
                    }
                }else{
                    // resize image only if we don't adapt
                    if(!convertPane.definition.getEditor().getText().equals(TR.tr("convertWindow.options.format.fitToImage"))){
                        // set image resolution by width
                        if(convertPane.width < pdImage.getWidth()){ // don't redefine size if the image has a less quality than we want, we want to reduce image size, not increase it
                            int imagePixelsHeight = (int) (((double) pdImage.getHeight()) / pdImage.getWidth() * convertPane.width); // calculate image resolution height
                            // resize image
                            pdImage = JPEGFactory.createFromImage(convertedFile.document, scaleImage(pdImage.getImage(), convertPane.width, imagePixelsHeight));
                        }
                    }
                }
                
                int borderX = (int) ((pageWidth - width) / 2d);
                int borderY = (int) ((pageHeight - height) / 2d);
                
                ///////////////////////////////////////////////////////////////////////
                
                contentStream.drawImage(pdImage, borderX, borderY, (float) width, (float) height);
                
                contentStream.close();
                convertedFile.addPage(page);
            }else if(isValidFile(file)){
                convertedFile.addPage(new PDPage(defaultPageSize));
            }
        }
        
        
        if(convertedFile.document.getNumberOfPages() >= 1){
            convertedFiles.add(convertedFile);
        }else{
            convertedFile.addPage(new PDPage(defaultPageSize));
            convertedFiles.add(convertedFile);
        }
    }
    
    public static boolean isGoodFormat(File file){
        String ext = StringUtils.removeBeforeLastRegex(file.getName(), ".");
        if(!file.exists()) ext = "";
        return ImageUtils.ACCEPTED_EXTENSIONS.contains(ext);
    }
    
    private boolean isValidFile(File file){
        return isGoodFormat(file) || (convertPane.convertVoidFiles.isSelected() && !file.isHidden());
    }
    
    private boolean isValidDir(File dir){
        if(!dir.isDirectory()) return false;
        if(!convertPane.convertVoidFiles.isSelected()){
            int compatibleFiles = 0;
            for(File file : Objects.requireNonNull(dir.listFiles())) if(isValidFile(file)) compatibleFiles++;
            return compatibleFiles != 0;
        }
        return true;
    }
    
    private BufferedImage scaleImage(BufferedImage image, int width, int height){
        
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image2.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return image2;
    }
    
    public int getFilesLength(){
        if(convertPane.convertDirs){
            File mainDir = new File(convertPane.srcDir.getText());
            int i = 0;
            if(mainDir.listFiles() == null) throw new RuntimeException("The input directory contains no files.");
            for(File dir : Objects.requireNonNull(mainDir.listFiles())){
                if(isValidDir(dir) || (isValidFile(dir) && convertPane.convertAloneFiles.isSelected())){
                    i++;
                }
            }
            return i;
        }else{
            return 1;
        }
    }
    
    private boolean shouldStop = false;
    public void stop(){
        shouldStop = true;
    }
}
