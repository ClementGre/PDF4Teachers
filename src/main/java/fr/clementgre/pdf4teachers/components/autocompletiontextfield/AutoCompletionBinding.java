/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components.autocompletiontextfield;

import fr.clementgre.pdf4teachers.Main;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;
import java.util.UUID;

public abstract class AutoCompletionBinding<T> {
    
    
    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/
    private final Node completionTarget;
    private final AutoCompletePopup<T> autoCompletionPopup;
    private final Object suggestionsTaskLock = new Object();
    
    private FetchSuggestionsTask suggestionsTask;
    private final Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> suggestionProvider;
    private boolean ignoreInputChanges;
    private long delay = 250;
    
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/
    
    
    /**
     * Creates a new AutoCompletionBinding
     *
     * @param completionTarget The target node to which auto-completion shall be added
     * @param suggestionProvider The strategy to retrieve suggestions
     * @param converter The converter to be used to convert suggestions to strings
     */
    protected AutoCompletionBinding(Control completionTarget,
                                    Callback<ISuggestionRequest, Collection<T>> suggestionProvider,
                                    StringConverter<T> converter){
        
        this.completionTarget = completionTarget;
        this.suggestionProvider = suggestionProvider;
        this.autoCompletionPopup = new AutoCompletePopup<>();
        this.autoCompletionPopup.setConverter(converter);
    
        autoCompletionPopup.prefWidthProperty().bind(completionTarget.widthProperty().multiply(Main.settings.zoom.getValue()));
        
        
        autoCompletionPopup.setOnSuggestion(sce -> {
            try{
                setIgnoreInputChanges(true);
                completeUserInput(sce.getSuggestion());
                fireAutoCompletion(sce.getSuggestion());
                hidePopup();
            }finally{
                // Ensure that ignore is always set back to false
                setIgnoreInputChanges(false);
            }
        });
    }
    
    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/
    
    /**
     * Specifies whether the PopupWindow should be hidden when an unhandled
     * escape key is pressed while the popup has focus.
     *
     * @param value
     */
    public void setHideOnEscape(boolean value) {
        autoCompletionPopup.setHideOnEscape(value);
    }
    
    /**
     * Set the current text the user has entered
     * @param userText
     */
    public final void setUserInput(String userText){
        if(!isIgnoreInputChanges()){
            onUserInputChanged(userText);
        }
    }
    
    /**
     * Sets the delay in ms between a key press and the suggestion popup being displayed.
     *
     * @param delay
     */
    public final void setDelay(long delay) {
        this.delay = delay;
    }
    
    /**
     * Gets the target node for auto completion
     * @return the target node for auto completion
     */
    public Node getCompletionTarget(){
        return completionTarget;
    }
    
    /**
     * Disposes the binding.
     */
    public abstract void dispose();
    
    
    /**
     * Set the maximum number of rows to be visible in the popup when it is
     * showing.
     *
     * @param value
     */
    public final void setVisibleRowCount(int value) {
        autoCompletionPopup.setVisibleRowCount(value);
    }
    
    /**
     * Return the maximum number of rows to be visible in the popup when it is
     * showing.
     *
     * @return the maximum number of rows to be visible in the popup when it is
     * showing.
     */
    public final int getVisibleRowCount() {
        return autoCompletionPopup.getVisibleRowCount();
    }
    
    /**
     * Return an property representing the maximum number of rows to be visible
     * in the popup when it is showing.
     *
     * @return an property representing the maximum number of rows to be visible
     * in the popup when it is showing.
     */
    public final IntegerProperty visibleRowCountProperty() {
        return autoCompletionPopup.visibleRowCountProperty();
    }
    
    /**
     * Sets the prefWidth of the popup.
     *
     * @param value
     */
    public final void setPrefWidth(double value) {
        autoCompletionPopup.setPrefWidth(value);
    }
    
    /**
     * Return the pref width of the popup.
     *
     * @return the pref width of the popup.
     */
    public final double getPrefWidth() {
        return autoCompletionPopup.getPrefWidth();
    }
    
    /**
     * Return the property associated with the pref width.
     * @return
     */
    public final DoubleProperty prefWidthProperty() {
        return autoCompletionPopup.prefWidthProperty();
    }
    
    /**
     * Sets the minWidth of the popup.
     *
     * @param value
     */
    public final void setMinWidth(double value) {
        autoCompletionPopup.setMinWidth(value);
    }
    
    /**
     * Return the min width of the popup.
     *
     * @return the min width of the popup.
     */
    public final double getMinWidth() {
        return autoCompletionPopup.getMinWidth();
    }
    
    /**
     * Return the property associated with the min width.
     * @return
     */
    public final DoubleProperty minWidthProperty() {
        return autoCompletionPopup.minWidthProperty();
    }
    
    /**
     * Sets the maxWidth of the popup.
     *
     * @param value
     */
    public final void setMaxWidth(double value) {
        autoCompletionPopup.setMaxWidth(value);
    }
    
    /**
     * Return the max width of the popup.
     *
     * @return the max width of the popup.
     */
    public final double getMaxWidth() {
        return autoCompletionPopup.getMaxWidth();
    }
    
    /**
     * Return the property associated with the max width.
     * @return
     */
    public final DoubleProperty maxWidthProperty() {
        return autoCompletionPopup.maxWidthProperty();
    }
    
    /**
     * Get the {@link AutoCompletePopup} used by this binding. Note that this gives access to the
     * internal API and should be used with great care (and in the expectation that things may break in
     * the future). All relevant methods of the popup are already exposed in this class.
     * <p/>
     * The only reason this is exposed is to allow custom skins for the popup.
     *
     * @return the {@link AutoCompletePopup} used by this binding
     */
    public AutoCompletePopup<T> getAutoCompletionPopup() {
        return autoCompletionPopup;
    }
    
    /***************************************************************************
     *                                                                         *
     * Protected methods                                                       *
     *                                                                         *
     **************************************************************************/
    
    /**
     * Complete the current user-input with the provided completion.
     * Sub-classes have to provide a concrete implementation.
     * @param completion
     */
    protected abstract void completeUserInput(T completion);
    
    
    /**
     * Show the auto completion popup
     */
    protected void showPopup(){
        autoCompletionPopup.show(completionTarget);
        selectFirstSuggestion(autoCompletionPopup);
    }
    
    /**
     * Hide the auto completion targets
     */
    protected void hidePopup(){
        autoCompletionPopup.hide();
    }
    
    protected void fireAutoCompletion(T completion){
        onAutoCompleted.get().handle(new AutoCompletionBinding.AutoCompletionEvent<>(completion));
    }
    
    
    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/
    
    /**
     * Selects the first suggestion (if any), so the user can choose it
     * by pressing enter immediately.
     */
    private void selectFirstSuggestion(AutoCompletePopup<?> autoCompletionPopup) {
        Skin<?> skin = autoCompletionPopup.getSkin();
        if (skin instanceof AutoCompletePopupSkin<?> au) {
            if (au.getNode() instanceof ListView<?> li && li.getItems() != null && !li.getItems().isEmpty()) {
                li.getSelectionModel().select(0);
            }
        }
    }
    
    /**
     * Occurs when the user text has changed and the suggestions require an update
     * @param userText
     */
    private void onUserInputChanged(final String userText){
        synchronized (suggestionsTaskLock) {
            if(suggestionsTask != null && suggestionsTask.isRunning()){
                // cancel the current running task
                suggestionsTask.cancel();
            }
            // create a new fetcher task
            suggestionsTask = new AutoCompletionBinding.FetchSuggestionsTask(userText, delay);
            new Thread(suggestionsTask).start();
        }
    }
    
    /**
     * Shall changes to the user input be ignored?
     * @return
     */
    private boolean isIgnoreInputChanges(){
        return ignoreInputChanges;
    }
    
    /**
     * If IgnoreInputChanges is set to true, all changes to the user input are
     * ignored. This is primary used to avoid self triggering while
     * auto completing.
     * @param state
     */
    private void setIgnoreInputChanges(boolean state){
        ignoreInputChanges = state;
    }
    
    /***************************************************************************
     *                                                                         *
     * Inner classes and interfaces                                            *
     *                                                                         *
     **************************************************************************/
    
    
    /**
     * Represents a suggestion fetch request
     *
     */
    public interface ISuggestionRequest {
        /**
         * Is this request canceled?
         * @return {@code true} if the request is canceled, otherwise {@code false}
         */
        boolean isCancelled();
        
        /**
         * Get the user text to which suggestions shall be found
         * @return {@link String} containing the user text
         */
        String getUserText();
    }
    
    
    
    /**
     * This task is responsible to fetch suggestions asynchronous
     * by using the current defined suggestionProvider
     *
     */
    private class FetchSuggestionsTask extends Task<Void> implements AutoCompletionBinding.ISuggestionRequest {
        private final String userText;
        private final long delay;
        
        public FetchSuggestionsTask(String userText, long delay){
            this.userText = userText;
            this.delay = delay;
        }
        
        @Override
        protected Void call() throws Exception {
            Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> provider = suggestionProvider;
            if(provider != null){
                long startTime = System.currentTimeMillis();
                long sleepTime = startTime + delay - System.currentTimeMillis();
                if (sleepTime > 0 && !isCancelled()) {
                    Thread.sleep(sleepTime);
                }
                if(!isCancelled()){
                    final Collection<T> fetchedSuggestions = provider.call(this);
                    Platform.runLater(() -> {
                        // check whether completionTarget is still valid
                        boolean validNode = completionTarget.getScene() != null
                                && completionTarget.getScene().getWindow() != null;
                        if(fetchedSuggestions != null && !fetchedSuggestions.isEmpty() && validNode){
                            autoCompletionPopup.getSuggestions().setAll(fetchedSuggestions);
                            showPopup();
                        }else{
                            // No suggestions found, so hide the popup
                            hidePopup();
                        }
                    });
                }
            }else {
                // No suggestion provider
                hidePopup();
            }
            return null;
        }
        
        @Override
        public String getUserText() {
            return userText;
        }
    }
    
    /***************************************************************************
     *                                                                         *
     * Events                                                                  *
     *                                                                         *
     **************************************************************************/
    
    
    // --- AutoCompletionEvent
    
    /**
     * Represents an Event which is fired after an auto completion.
     */
    @SuppressWarnings("serial")
    public static class AutoCompletionEvent<TE> extends Event {
        
        /**
         * The event type that should be listened to by people interested in
         * knowing when an auto completion has been performed.
         */
        public static final EventType<AutoCompletionBinding.AutoCompletionEvent<?>> AUTO_COMPLETED
                = new EventType<>("AUTO_COMPLETED" + UUID.randomUUID()); //$NON-NLS-1$
        
        private final TE completion;
        
        /**
         * Creates a new event that can subsequently be fired.
         */
        public AutoCompletionEvent(TE completion) {
            super(AUTO_COMPLETED);
            this.completion = completion;
        }
        
        /**
         * Returns the chosen completion.
         */
        public TE getCompletion() {
            return completion;
        }
    }
    
    
    private ObjectProperty<EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>>> onAutoCompleted;
    
    /**
     * Set a event handler which is invoked after an auto completion.
     * @param value
     */
    public final void setOnAutoCompleted(EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>> value) {
        onAutoCompletedProperty().set(value);
    }
    
    public final EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>> getOnAutoCompleted() {
        return onAutoCompleted == null ? null : onAutoCompleted.get();
    }
    
    public final ObjectProperty<EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>>> onAutoCompletedProperty() {
        if (onAutoCompleted == null) {
            onAutoCompleted = new ObjectPropertyBase<>() {
                @Override
                @SuppressWarnings({ "rawtypes", "unchecked" })
                protected void invalidated(){}
                @Override
                public Object getBean(){
                    return AutoCompletionBinding.this;
                }
                @Override
                public String getName(){
                    return "onAutoCompleted"; //$NON-NLS-1$
                }
            };
        }
        return onAutoCompleted;
    }
    
    
}
