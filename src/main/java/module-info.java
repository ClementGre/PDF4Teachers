open module fr.clementgre.pdf4teachers {
    
    // JAVA MODULES
    
    requires java.xml;
    requires java.base;
    requires java.logging;
    requires java.sql;
    requires java.desktop;
    requires java.management;
    requires jdk.crypto.cryptoki;
    requires jdk.accessibility;
    
    // OTHER DEPENDENCIES
    
    // pdf
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires org.apache.commons.logging;
    uses javax.imageio.spi.ImageInputStreamSpi;
    uses javax.imageio.spi.ImageOutputStreamSpi;
    uses javax.imageio.spi.ImageWriterSpi;
    requires jai.imageio.core;
    requires jai.imageio.jpeg2000;
    requires org.apache.pdfbox.jbig2;
    uses javax.imageio.spi.ImageReaderSpi;
    
    // style & controls
    requires org.jfxtras.styles.jmetro;
    requires org.controlsfx.controls;
    
    // data parsing/encoding
    requires org.yaml.snakeyaml;
    requires com.fasterxml.jackson.core;
    requires com.opencsv;
    requires metadata.extractor; // EXIF READER
    
    // Latex & StarMath
    requires jlatexmath;
    requires writertolatex;
    
    // OSX
    requires nsmenufx;
    
    // JAVAFX
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    
    // SVG
    requires batik.parser;
    requires de.rototor.pdfbox.graphics2d;
    
    // Utils
    requires google.diff.match.patch;
    requires uniqueforj;
    
    // Os Theme Detector
    requires com.jthemedetector;
    
    // IDE
    requires org.jetbrains.annotations;
    requires org.apache.pdfbox.io;
    requires com.sun.jna;
    requires jfa;
}

