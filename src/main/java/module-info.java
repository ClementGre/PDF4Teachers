open module fr.clementgre.pdf4teachers {

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

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    //requires jtouchbar;
    //requires jtouchbar.javafx;

    // EXPORTS

    exports fr.clementgre.pdf4teachers;
    exports fr.clementgre.pdf4teachers.components;
    exports fr.clementgre.pdf4teachers.datasaving;
    exports fr.clementgre.pdf4teachers.document;
    exports fr.clementgre.pdf4teachers.document.render.display;
    exports fr.clementgre.pdf4teachers.document.render.export;
    exports fr.clementgre.pdf4teachers.document.editions;
    exports fr.clementgre.pdf4teachers.document.editions.elements;
    exports fr.clementgre.pdf4teachers.panel;
    exports fr.clementgre.pdf4teachers.panel.MainScreen;
    exports fr.clementgre.pdf4teachers.panel.sidebar;
    exports fr.clementgre.pdf4teachers.panel.sidebar.files;
    exports fr.clementgre.pdf4teachers.panel.sidebar.texts;
    exports fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections;
    exports fr.clementgre.pdf4teachers.panel.sidebar.grades;
    exports fr.clementgre.pdf4teachers.panel.sidebar.paint;
    exports fr.clementgre.pdf4teachers.panel.sidebar.grades.export;
    exports fr.clementgre.pdf4teachers.utils;
    exports fr.clementgre.pdf4teachers.interfaces.windows;

}