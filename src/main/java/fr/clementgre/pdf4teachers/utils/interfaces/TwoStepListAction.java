package fr.clementgre.pdf4teachers.utils.interfaces;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TwoStepListAction<T, D> {

    private int completedSize = 0;
    private boolean recursive;
    private final List<T> data;
    private final List<D> sortedData = new ArrayList<>();
    private final HashMap<Integer, Integer> excludedReasons = new HashMap<>();

    public static final int CODE_STOP = -1;
    public static final int CODE_OK = 0;
    public static final int CODE_SKIP_1 = 1;
    public static final int CODE_SKIP_2 = 2;
    public static final int CODE_SKIP_3 = 3;
    public static final int CODE_SKIP_4 = 4;


    public enum ProcessResult{
        OK,
        SKIPPED,
        STOP,
        STOP_WITHOUT_ALERT
    }

    public TwoStepListAction(boolean async, boolean recursive, TwoStepListInterface<T, D> caller){
        this.recursive = recursive;
        this.data = caller.prepare(isRecursive());

        if(sortData(caller)){
            if(async){
                processDataAsync(caller, () -> {
                    caller.finish(data.size(), sortedData.size(), completedSize, excludedReasons, isRecursive());
                });
            }else{
                if(processData(caller)){
                    caller.finish(data.size(), sortedData.size(), completedSize, excludedReasons, isRecursive());
                }
            }

        }

    }

    public boolean sortData(TwoStepListInterface<T, D> caller){
        for(T value : data){
            try{
                Entry<D, Integer> result = caller.sortData(value, isRecursive());
                if(result != null){
                    if(result.getValue() == 0){
                        sortedData.add(result.getKey());
                    }if(result.getValue() == -1){
                        return false;
                    }else{
                        if(excludedReasons.containsKey(result.getValue())){
                            excludedReasons.put(result.getValue(), excludedReasons.get(result.getValue())+1);
                        }else{
                            excludedReasons.put(result.getValue(), 1);
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                boolean result = DialogBuilder.showErrorAlert(null, e.getMessage(), data.size() > 1);
                if(data.size() <= 1) return false;
                if(result) return false;
            }
        }
        return true;
    }

    public boolean processData(TwoStepListInterface<T, D> caller){
        for(D value : sortedData){
            TwoStepListAction.ProcessResult result = caller.completeData(value, isRecursive());
            if(result == ProcessResult.OK) completedSize++;
            else if(result == ProcessResult.STOP) break;
            else if(result == ProcessResult.STOP_WITHOUT_ALERT) return false;
        }
        return true;
    }

    public void processDataAsync(TwoStepListInterface<T, D> caller, CallBack callBack){
        Alert loadingAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("dialogs.asyncAction.header.title"));
        loadingAlert.setWidth(600);
        loadingAlert.setHeaderText(TR.tr("dialogs.asyncAction.header"));
        VBox pane = new VBox();
        Label currentDocument = new Label();
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setMinHeight(10);
        VBox.setMargin(loadingBar, new Insets(10, 0, 0,0));
        pane.getChildren().addAll(currentDocument, loadingBar);
        loadingAlert.getDialogPane().setContent(pane);
        loadingAlert.show();

        new Thread(() -> {
            boolean isCanceled = false;
            for(D value : sortedData){
                ProcessResult result = caller.completeData(value, isRecursive());
                if(result == ProcessResult.OK) completedSize++;
                else if(result == ProcessResult.STOP) break;
                else if(result == ProcessResult.STOP_WITHOUT_ALERT){
                    isCanceled = true; break;
                }

                Platform.runLater(() -> {
                    currentDocument.setText(caller.getSortedDataName(value, isRecursive()) + "(" + completedSize + "/" + sortedData.size() + ")");
                    loadingBar.setProgress(completedSize/((float)sortedData.size()));
                });

            }
            boolean finalIsCanceled = isCanceled;
            Platform.runLater(() -> {
                loadingAlert.close();
                if(!finalIsCanceled) callBack.call();
            });
        }, "TwoStepListAction Async data processing").start();


    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
}
