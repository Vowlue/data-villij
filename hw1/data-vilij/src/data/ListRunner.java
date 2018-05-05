package data;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.concurrent.Task;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.List;

public class ListRunner implements Runnable{
    private ListCollector collector;
    private ApplicationTemplate applicationTemplate;
    private boolean continuous;
    public ListRunner(ListCollector collector, ApplicationTemplate applicationTemplate, boolean continuous){
        this.collector = collector;
        this.applicationTemplate = applicationTemplate;
        this.continuous = continuous;
    }

    @Override
    public void run() {
        AppUI appUI = (AppUI)applicationTemplate.getUIComponent();
        AppActions appActions = (AppActions)applicationTemplate.getActionComponent();
        AppData appData = (AppData)applicationTemplate.getDataComponent();
        System.out.println(collector.take());
        for(List dataList = collector.take(); dataList != null; dataList = collector.take()){
            List finalDataList = dataList;
            Task<Void> iterationTask = new Task<Void>() {
                @Override
                protected Void call() {
                    Platform.runLater(() -> {
                        if(continuous) {
                            appUI.hideRunButton();
                            appUI.enableScreenshotButton(false);
                        }
                        appData.processList(finalDataList);
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
                appActions.waitForUser();
            }
        }
        Platform.runLater(appUI::finishAlgorithm);
    }
}
