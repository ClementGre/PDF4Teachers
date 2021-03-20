package fr.clementgre.pdf4teachers.interfaces;

import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.AlertIconType;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jfxtras.styles.jmetro.JMetroStyleClass;
import org.controlsfx.control.NotificationPane;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoHideNotificationPane extends NotificationPane{


    private static class Notification{
        public String text;
        public AlertIconType iconType;
        public TextField input;
        public int autoHideTime;
        public Notification(String text, AlertIconType iconType, int autoHideTime) {
            this.text = text;
            this.iconType = iconType;
            this.autoHideTime = autoHideTime;
        }
        public Notification(String text, AlertIconType iconType, TextField input) {
            this.text = text;
            this.iconType = iconType;
            this.input = input;
            this.autoHideTime = -1;
        }
    }

    // PENDING MANAGING

    ArrayList<Notification> pendingList = new ArrayList<>();
    public AutoHideNotificationPane(Node content){
        super(content);
        final EventHandler<Event> onHideEvent = e -> checkPending();
        addEventHandler(NotificationPane.ON_HIDDEN, onHideEvent);
        setShowFromTop(false);
    }

    public void addToPending(String text, AlertIconType iconType, int autoHideTime){
        pendingList.add(new Notification(text, iconType, autoHideTime));
        checkPending();
    }
    public void showNow(String text, AlertIconType iconType, int autoHideTime){
        pendingList.add(0, new Notification(text, iconType, autoHideTime));
        if(isShowing()) hide(); else checkPending();
    }
    public void showNow(String text, AlertIconType iconType, TextField input){
        pendingList.add(0, new Notification(text, iconType, input));
        if(isShowing()) hide(); else checkPending();
    }

    private void checkPending(){
        if(pendingList.size() > 0 && !isShowing()){
            Notification notif = pendingList.get(0);
            if(notif.input == null) show(notif.text, notif.iconType, notif.autoHideTime);
            else showWithInput(notif.text, notif.input, notif.iconType, notif.autoHideTime);
            pendingList.remove(0);
        }
    }


    // SHOW AND UI DESIGN

    public void show(String text, AlertIconType iconType, int autoHideTime){
        show(text, getGraphic(iconType), autoHideTime);
    }
    private HBox getGraphic(AlertIconType iconType){
        Image image = new Image(getClass().getResourceAsStream("/img/dialogs/" + iconType.getFileName() + ".png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(35);
        imageView.setPreserveRatio(true);

        HBox graphic = new HBox(imageView);
        HBox.setMargin(imageView, new Insets(4));
        return graphic;
    }
    public void showWithInput(String text, TextField input, AlertIconType iconType, int autoHideTime){
        show("", getInputGraphic(iconType, text, input), autoHideTime);
    }
    private HBox getInputGraphic(AlertIconType iconType, String text, TextField input){
        HBox graphic = getGraphic(iconType);
        VBox textGraphic = new VBox();
        HBox.setMargin(textGraphic, new Insets(4, 20, 4, 20));
        HBox.setHgrow(textGraphic, Priority.ALWAYS);
        input.setMaxWidth(400);

        Label textLabel = new Label(text);
        textGraphic.getChildren().addAll(textLabel, input);

        graphic.getChildren().add(textGraphic);
        return graphic;
    }



    // SHOW PROCESS

    private void show(String text, Pane graphic, int autoHideTime){
        hideAndThen(() -> {
            show(text, graphic);
            Platform.runLater(() -> {
                setupStyle();
                setupAutoHide(autoHideTime);
            });
        });
    }
    private void setupStyle(){
        Pane region = (Pane) lookup(".notification-bar > .pane");
        if(region != null){
            region.getStyleClass().add(JMetroStyleClass.BACKGROUND);
            region.setEffect(new DropShadow());
        }
        TextField input = (TextField) lookup(".text-field");
        if(input != null){
            input.requestFocus();
            PlatformUtils.runLaterOnUIThread(100, input::requestFocus);
        }
    }
    private void setupAutoHide(int autoHideTime){
        if(autoHideTime > 0){
            AtomicBoolean hidden = new AtomicBoolean(false);
            setOnHiding((e) -> hidden.set(true));

            new Thread(() -> {
                try{ Thread.sleep(autoHideTime * 1000L); }catch(InterruptedException e){ e.printStackTrace(); }
                if(!hidden.get()) hide();

            }, "notification auto hide").start();
        }
    }



    private void hideAndThen(final Runnable r) {
        if (isShowing()) {
            final EventHandler<Event> eventHandler = new EventHandler<>() {
                @Override public void handle(Event e) {
                    r.run();
                    removeEventHandler(NotificationPane.ON_HIDDEN, this);
                }
            };
            addEventHandler(NotificationPane.ON_HIDDEN, eventHandler);
            hide();
        } else {
            r.run();
        }
    }

}
