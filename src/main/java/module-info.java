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
    
    // pdf
    requires org.apache.pdfbox;
    requires org.apache.fontbox;
    requires commons.logging;
    
    // jfx addons
    requires org.jfxtras.styles.jmetro;
    requires org.controlsfx.controls;
    
    // data parsing/encoding
    requires org.yaml.snakeyaml;
    requires com.fasterxml.jackson.core;
    requires metadata.extractor; // EXIF READER
    
    // latex
    requires jlatexmath;
    requires writertolatex;
    
    // OSX
    requires nsmenufx;
    
    // JAVAFX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    
    // SVG
    requires batik.parser;
    requires de.rototor.pdfbox.graphics2d;
    
    requires org.jetbrains.annotations;
    
    // Utils
    requires google.diffmatchpatch;
    requires uniqueforj;
    
    // Android
    /*requires com.gluonhq.attach.display;
    requires com.gluonhq.attach.util;
    requires com.gluonhq.attach.lifecycle;
    requires com.gluonhq.attach.storage;
    requires com.gluonhq.attach.statusbar;
    requires com.gluonhq.charm.glisten;*/
    
    // EXPORTS
    
    exports fr.clementgre.pdf4teachers;
    exports fr.clementgre.pdf4teachers.components;
    exports fr.clementgre.pdf4teachers.datasaving;
    exports fr.clementgre.pdf4teachers.datasaving.settings;
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
    exports fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;
    exports fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;
    exports fr.clementgre.pdf4teachers.panel.sidebar.grades.export;
    exports fr.clementgre.pdf4teachers.utils;
    exports fr.clementgre.pdf4teachers.utils.dialogs;
    exports fr.clementgre.pdf4teachers.utils.image;
    exports fr.clementgre.pdf4teachers.utils.interfaces;
    exports fr.clementgre.pdf4teachers.utils.sort;
    exports fr.clementgre.pdf4teachers.utils.style;
    exports fr.clementgre.pdf4teachers.interfaces;
    exports fr.clementgre.pdf4teachers.interfaces.windows;
    exports fr.clementgre.pdf4teachers.utils.fonts;
    exports fr.clementgre.pdf4teachers.utils.exceptions;
    exports fr.clementgre.pdf4teachers.datasaving.simpleconfigs;
    exports fr.clementgre.pdf4teachers.utils.dialogs.alerts;
    exports fr.clementgre.pdf4teachers.components.menus;
    exports fr.clementgre.pdf4teachers.interfaces.windows.settings;
    exports fr.clementgre.pdf4teachers.utils.svg;
    exports fr.clementgre.pdf4teachers.utils.panes;
    exports fr.clementgre.pdf4teachers.document.editions.undoEngine;
    exports fr.clementgre.pdf4teachers.utils.locking;
    
}