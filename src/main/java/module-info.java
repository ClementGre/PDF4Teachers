module fr.themsou {

    // OTHER DEPENDENCIES

    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires commons.logging;

    requires org.jfxtras.styles.jmetro;

    requires snakeyaml;

    requires com.fasterxml.jackson.core;

    // JAVAFX

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.logging;
    requires java.xml;
    requires java.desktop;

    // JAVA

    requires jdk.crypto.ec;
    requires java.sql;

    exports fr.themsou.main;
    exports fr.themsou.document;
    exports fr.themsou.document.render;
    exports fr.themsou.document.editions;
    exports fr.themsou.document.editions.elements;
    exports fr.themsou.panel;
    exports fr.themsou.panel.leftBar.files;
    exports fr.themsou.panel.leftBar.texts;
    exports fr.themsou.panel.leftBar.grades;
    exports fr.themsou.panel.leftBar.paint;
    exports fr.themsou.panel.leftBar.grades.export;
    exports fr.themsou.utils;
    exports fr.themsou.windows;
}