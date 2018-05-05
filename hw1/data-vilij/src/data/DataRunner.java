package data;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.concurrent.Task;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

public class DataRunner implements Runnable{
    private DataCollector collector;
    private ApplicationTemplate applicationTemplate;
    private boolean continuous;
    public DataRunner(DataCollector collector, ApplicationTemplate applicationTemplate, boolean continuous){
        this.collector = collector;
        this.applicationTemplate = applicationTemplate;
        this.continuous = continuous;
    }

    @Override
    public void run() {
        AppUI appUI = (AppUI)applicationTemplate.getUIComponent();
        AppData appData = (AppData)applicationTemplate.getDataComponent();
        for(DataSet dataSet = collector.take(); dataSet != null; dataSet = collector.take()){
            DataSet finalDataSet = dataSet;
            Task<Void> iterationTask = new Task<Void>() {
                @Override
                protected Void call() {
                    Platform.runLater(() -> {
                        if(continuous) {
                            appUI.hideRunButton();
                            appUI.enableScreenshotButton(false);
                        }
                        appData.processDataSet(finalDataSet);
                    });
                    return null;
                }
            };
            if(continuous){
                new Thread(iterationTask).start();
                try {
                    long SLEEPTIME = 1000;
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                new Thread(iterationTask).start();
                appUI.setAlgorithmPaused(true);
                appUI.changeRunButton(1);
                ((AppActions)applicationTemplate.getActionComponent()).waitForUser();
            }
        }
        Platform.runLater(appUI::finishAlgorithm);
    }
}
