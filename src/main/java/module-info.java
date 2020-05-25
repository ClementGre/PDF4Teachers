module fr.themsou {

    // JAVA MODULES

    requires java.xml;
    requires java.base;
    requires java.logging;
    requires java.sql;
    requires java.desktop;
    requires java.management;
    requires jdk.crypto.ec;

    // OTHER DEPENDENCIES

    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires commons.logging;

    requires org.jfxtras.styles.jmetro;

    requires org.yaml.snakeyaml;

    requires com.fasterxml.jackson.core;

    requires jlatexmath;

    // JAVAFX

    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    // EXPORTS

    exports fr.themsou.yaml;
    exports fr.themsou.main;
    exports fr.themsou.document;
    exports fr.themsou.document.render.display;
    exports fr.themsou.document.render.export;
    exports fr.themsou.document.editions;
    exports fr.themsou.document.editions.elements;
    exports fr.themsou.panel;
    exports fr.themsou.panel.leftBar.files;
    exports fr.themsou.panel.leftBar.texts;
    exports fr.themsou.panel.leftBar.texts.TreeViewSections;
    exports fr.themsou.panel.leftBar.grades;
    exports fr.themsou.panel.leftBar.paint;
    exports fr.themsou.panel.leftBar.grades.export;
    exports fr.themsou.utils;
    exports fr.themsou.windows;
}