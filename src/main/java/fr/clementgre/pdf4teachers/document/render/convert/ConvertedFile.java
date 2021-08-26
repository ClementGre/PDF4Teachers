/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.convert;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;

public class ConvertedFile {
    
    public PDDocument document;
    public File file;
    
    public ConvertedFile(File file){
        this.file = file;
        this.document = new PDDocument();
    }
    
    public void addPage(PDPage page){
        document.addPage(page);
    }
}
