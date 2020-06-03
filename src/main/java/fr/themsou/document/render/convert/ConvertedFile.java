package fr.themsou.document.render.convert;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.util.ArrayList;

public class ConvertedFile {

    public PDDocument document;
    public File file;

    public ConvertedFile(File file) {
        this.file = file;
        this.document = new PDDocument();
    }

    public void addPage(PDPage page) {
        document.addPage(page);
    }
}
