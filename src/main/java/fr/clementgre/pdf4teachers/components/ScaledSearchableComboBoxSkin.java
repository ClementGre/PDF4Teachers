/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.CustomTextField;

import java.util.Arrays;
import java.util.function.Predicate;

public class ScaledSearchableComboBoxSkin<T> extends SkinBase<ComboBox<T>> {
    
    //private static final Image filterIcon = new Image(SearchableComboBoxSkin.class.getResource("/impl/org/controlsfx/table/filter.png").toExternalForm());
    
    /**
     * A "normal" combobox used internally as a delegate to get the default combo box behavior.
     * This combo box contains the filtered items and handles the popup.
     */
    private final ComboBox<T> filteredComboBox;
    
    /**
     * The search field shown when the popup is shown.
     */
    private final TextField searchField;
    
    /**
     * Used when pressing ESC
     */
    private T previousValue;
    
    public ScaledSearchableComboBoxSkin(ComboBox<T> comboBox) {
        super(comboBox);
        
        // first create the filtered combo box
        filteredComboBox = createFilteredComboBox();
        getChildren().add(filteredComboBox);
        
        // and the search field
        searchField = createSearchField();
        getChildren().add(searchField);
        
        bindSearchFieldAndFilteredComboBox();
        preventDefaultComboBoxKeyListener();
        
        // open the popup on Cursor Down and up
        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, this::checkOpenPopup);
    }
    
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        // ensure filteredComboBox and searchField have the same size as the field
        filteredComboBox.resizeRelocate(x, y, w, h);
        searchField.resizeRelocate(x, y, w, h);
    }
    
    private TextField createSearchField() {
        /*CustomTextField field = (CustomTextField) TextFields.createClearableTextField();
        field.setPromptText(getString("filterpanel.search.field"));
        field.setId("search");
        field.getStyleClass().add("combo-box-search");
        ImageView imageView = new ImageView(filterIcon);
        imageView.setFitHeight(15);
        imageView.setPreserveRatio(true);
        field.setLeft(imageView);
        return field;*/
    
        CustomTextField field = new CustomTextField();
        field.setPromptText(TR.tr("actions.search.ellipsis"));
        field.setId("search");
        field.getStyleClass().add("combo-box-search");
        
        Region sortImage = SVGPathIcons.generateImage(SVGPathIcons.FILTER, "#999999", 0, 15);
        sortImage.setMaxHeight(15);
        field.setLeft(sortImage);
        Region emptyImage = SVGPathIcons.generateImage(SVGPathIcons.CROSS, "#ff6060", 1, 15);
        emptyImage.setCursor(Cursor.HAND);
        emptyImage.setMaxHeight(15);
        
        field.setRight(emptyImage);
        
        
        emptyImage.setOnMousePressed(event -> {
            getSkinnable().getSelectionModel().select(null);
        });
        
        return field;
    }
    
    private ComboBox<T> createFilteredComboBox() {
        ComboBox<T> box = new ComboBox<>();
        box.setId("filtered");
        box.getStyleClass().add("combo-box-filtered");
        box.setFocusTraversable(false);
        
        // unidirectional bindings -- copy values from skinnable
        Bindings.bindContent(box.getStyleClass(), getSkinnable().getStyleClass());
        box.buttonCellProperty().bind(getSkinnable().buttonCellProperty());
        box.cellFactoryProperty().bind(getSkinnable().cellFactoryProperty());
        box.converterProperty().bind(getSkinnable().converterProperty());
        box.placeholderProperty().bind(getSkinnable().placeholderProperty());
        box.disableProperty().bind(getSkinnable().disableProperty());
        box.visibleRowCountProperty().bind(getSkinnable().visibleRowCountProperty());
        box.promptTextProperty().bind(getSkinnable().promptTextProperty());
        getSkinnable().showingProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal)
                box.show();
            else
                box.hide();
        });
        
        // bidirectional bindings
        box.valueProperty().bindBidirectional(getSkinnable().valueProperty());
        
        return box;
    }
    
    private void bindSearchFieldAndFilteredComboBox() {
        // set the items of the filtered combo box
        filteredComboBox.setItems(createFilteredList());
        // and keep it up to date, even if the original list changes
        getSkinnable().itemsProperty()
                .addListener((obs, oldVal, newVal) -> filteredComboBox.setItems(createFilteredList()));
        // and update the filter, when the text in the search field changes
        searchField.textProperty().addListener(o -> updateFilter());
        
        // the search field must only be visible, when the popup is showing
        searchField.visibleProperty().bind(filteredComboBox.showingProperty());
        
        filteredComboBox.showingProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal) {
                // When the filtered combo box popup is showing, we must also set the showing property
                // of the original combo box. And here we must remember the previous value for the
                // ESCAPE behavior. And we must transfer the focus to the search field, because
                // otherwise the search field would not allow typing in the search text.
                getSkinnable().show();
                previousValue = getSkinnable().getValue();
                searchField.requestFocus();
            } else {
                // When the filtered combo box popup is hidden, we must also set the showing property
                // of the original combo box to false, clear the search field.
                getSkinnable().hide();
                searchField.setText("");
            }
        });
        
        // but when the search field is focussed, the popup must still be shown
        searchField.focusedProperty().addListener((obs, oldVal, newVal) ->
        {
            if (newVal)
                filteredComboBox.show();
            else
                filteredComboBox.hide();
        });
    }
    
    private FilteredList<T> createFilteredList() {
        return new FilteredList<>(getSkinnable().getItems(), predicate());
    }
    
    /**
     * Called every time the filter text changes.
     */
    private void updateFilter() {
        // does not work, because of Bug https://bugs.openjdk.java.net/browse/JDK-8174176
        // ((FilteredList<T>)filteredComboBox.getItems()).setPredicate(predicate());
        
        // therefore we need to do this
        filteredComboBox.setItems(createFilteredList());
    }
    
    /**
     * Return the Predicate to filter the popup items based on the search field.
     */
    private Predicate<T> predicate() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            // don't filter
            return null;
        }
        
        return predicate(searchText);
    }
    
    /**
     * Return the Predicate to filter the popup items based on the given search text.
     */
    private Predicate<T> predicate(String searchText) {
        // OK, if the display text contains all words, ignoring case
        String[] lowerCaseSearchWords = searchText.toLowerCase().split(" ");
        return value ->
        {
            String lowerCaseDisplayText = getDisplayText(value).toLowerCase();
            return Arrays.stream(lowerCaseSearchWords).allMatch(lowerCaseDisplayText::contains);
        };
    }
    
    /**
     * Create a text for the given item, that can be used to compare with the filter text.
     */
    private String getDisplayText(T value) {
        StringConverter<T> converter = filteredComboBox.getConverter();
        return value == null ? "" : (converter != null ? converter.toString(value) : value.toString());
    }
    
    /**
     * The default behavior of the ComboBoxListViewSkin is to close the popup on
     * ENTER and SPACE, but we need to override this behavior.
     */
    private void preventDefaultComboBoxKeyListener() {
        filteredComboBox.skinProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal instanceof ComboBoxListViewSkin<?> cblwSkin) {
                if (cblwSkin.getPopupContent() instanceof ListView<?> listView) {
                    @SuppressWarnings("unchecked")
                    ListView<T> typedListView = (ListView<T>) listView;
                    typedListView.setOnKeyPressed(this::checkApplyAndCancel);
                }
            }
        });
    }

    
    /**
     * Used to alter the behaviour. React on Enter, Tab and ESC.
     */
    private void checkApplyAndCancel(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.ENTER || code == KeyCode.TAB) {
            // select the first item if no selection
            if (filteredComboBox.getSelectionModel().isEmpty())
                filteredComboBox.getSelectionModel().selectFirst();
            getSkinnable().hide();
            if (code == KeyCode.ENTER) {
                // otherwise the focus would be somewhere else
                getSkinnable().requestFocus();
            }
        } else if (code == KeyCode.ESCAPE) {
            getSkinnable().setValue(previousValue);
            getSkinnable().hide();
            // otherwise the focus would be somewhere else
            getSkinnable().requestFocus();
        }
    }
    
    /**
     * Show the popup on UP, DOWN, and on beginning typing a word.
     */
    private void checkOpenPopup(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.UP || code == KeyCode.DOWN) {
            filteredComboBox.show();
            // only open the box navigation
            e.consume();
        } else if (code.isLetterKey() || code.isDigitKey() || code == KeyCode.SPACE) {
            // show the box, let the box handle the KeyEvent
            filteredComboBox.show();
        }
    }
    
}
