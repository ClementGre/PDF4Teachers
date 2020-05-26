package fr.themsou.document.render.convert;

import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.util.ArrayList;

public class ConvertedFile {

    public ArrayList<PDPage> pages;
    public File file;

    public ConvertedFile(ArrayList<PDPage> pages, File file) {
        this.pages = pages;
        this.file = file;
    }
}
