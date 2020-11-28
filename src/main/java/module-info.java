module fr.clementgre.pdf4teachers {

    // JAVA MODULES

    requires java.xml;
    requires java.base;
    requires java.logging;
    requires java.sql;
    requires java.desktop;
    requires java.management;
    requires jdk.crypto.ec;
    requires jdk.accessibility;

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

    exports fr.clementgre.pdf4teachers;
    exports fr.clementgre.pdf4teachers.datasaving;
    exports fr.clementgre.pdf4teachers.document;
    exports fr.clementgre.pdf4teachers.document.render.display;
    exports fr.clementgre.pdf4teachers.document.render.export;
    exports fr.clementgre.pdf4teachers.document.editions;
    exports fr.clementgre.pdf4teachers.document.editions.elements;
    exports fr.clementgre.pdf4teachers.panel;
    exports fr.clementgre.pdf4teachers.panel.leftBar.files;
    exports fr.clementgre.pdf4teachers.panel.leftBar.texts;
    exports fr.clementgre.pdf4teachers.panel.leftBar.texts.TreeViewSections;
    exports fr.clementgre.pdf4teachers.panel.leftBar.grades;
    exports fr.clementgre.pdf4teachers.panel.leftBar.paint;
    exports fr.clementgre.pdf4teachers.panel.leftBar.grades.export;
    exports fr.clementgre.pdf4teachers.utils;
    exports fr.clementgre.pdf4teachers.interfaces.windows;
    opens fr.clementgre.pdf4teachers.interfaces;
}