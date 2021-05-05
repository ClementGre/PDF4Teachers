package fr.clementgre.pdf4teachers.utils.dialog.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class LoadingAlert extends CustomAlert{
    
    private Label label = new Label();
    private ProgressBar loadingBar = new ProgressBar();
    
    private String currentTaskText = "";
    private int progress = -1;
    private int total = 100;
    
    public LoadingAlert(boolean cancelButton, String title){
        this(cancelButton, title, "");
    }
    public LoadingAlert(boolean cancelButton, String title, String header){
        super(AlertType.INFORMATION, title, header);
        
        loadingBar.setMinHeight(10);
        VBox.setMargin(loadingBar, new Insets(10, 0, 0, 0));
        
        VBox pane = new VBox();
        pane.getChildren().addAll(label, loadingBar);
        getDialogPane().setContent(pane);
        
        if(cancelButton) addCancelButton(ButtonPosition.DEFAULT);
        else addCloseButton(TR.tr("actions.hide"));
        
        updateLabel();
        updateProgressBar();
    
        getDialogPane().setMinWidth(600);
        setWidth(1000);
    }
    
    public void showAsync(CallBack cancel){
        setOnHiding((e) -> {
            if(getResult() != null){
                if(getResult().getButtonData().isDefaultButton()) cancel.call();
            }
        });
        show();
    }
    
    private void updateProgressBar(){
        if(progress == -1) loadingBar.setProgress(-1);
        else loadingBar.setProgress(progress / ((float) total));
    }
    private void updateLabel(){
        if(progress == -1) label.setText(currentTaskText);
        else if(currentTaskText.isBlank()) label.setText(progress + "/" + total);
        else label.setText(currentTaskText + " (" + progress + "/" + total + ")");
    }
    
    public void setCurrentTaskText(String text){
        currentTaskText = text;
        updateLabel();
        updateProgressBar();
    }
    public void setTotal(int total){
        this.total = total;
        updateLabel();
        updateProgressBar();
    }
    public void setProgress(int progress){
        this.progress = progress;
        updateLabel();
        updateProgressBar();
    }
    
    
    
}
