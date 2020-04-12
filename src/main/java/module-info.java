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

    exports fr.themsou.main;
    exports fr.themsou.document;
    exports fr.themsou.document.render;
    exports fr.themsou.document.editions;
    exports fr.themsou.document.editions.elements;
    exports fr.themsou.panel;
    exports fr.themsou.panel.leftBar;
    exports fr.themsou.utils;
}