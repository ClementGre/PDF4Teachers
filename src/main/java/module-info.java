module fr.themsou {

    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires commons.logging;

    requires org.jfxtras.styles.jmetro;

    requires com.fasterxml.jackson.core;

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.logging;
    requires java.xml;
    requires java.desktop;

    requires jdk.crypto.ec;

    exports fr.themsou.main;
    exports fr.themsou.document;
    exports fr.themsou.document.render;
    exports fr.themsou.document.editions;
    exports fr.themsou.document.editions.elements;
    exports fr.themsou.panel;
    exports fr.themsou.panel.leftBar.files;
    exports fr.themsou.panel.leftBar.texts;
    exports fr.themsou.panel.leftBar.notes;
    exports fr.themsou.panel.leftBar.paint;
    exports fr.themsou.panel.leftBar.notes.export;
    exports fr.themsou.utils;
    exports fr.themsou.windows;
}