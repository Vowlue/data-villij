package classification;

import algorithms.Classifier;
import data.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.concurrent.Task;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;
    private ApplicationTemplate applicationTemplate;



    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(ApplicationTemplate applicationTemplate, DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void run() {
        AppData appData = (AppData)applicationTemplate.getDataComponent();
        AppUI appUI = (AppUI)applicationTemplate.getUIComponent();
        appUI.setAlgorithmRunning(true);
        for (int i = 1; i <= maxIterations; i++) {
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            if(i % updateInterval == 0 || i > maxIterations * .6 && RAND.nextDouble() < 0.05){
                Task<Void> iterationTask = new Task<Void>() {
                    @Override
                    protected Void call() {
                        Platform.runLater(() -> {
                            if(tocontinue.get()) {
                                appUI.hideRunButton();
                                appUI.enableScreenshotButton(false);
                            }
                            appData.showOutput(output);
                        });
                        return null;
                    }
                };
                if(tocontinue.get()){
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
                    synchronized (this){
                        while(appUI.isAlgorithmPaused()){
                            try {
                                this.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        new Thread(iterationTask).start();
                    }
                }
            }
        }
        Platform.runLater(() -> {
            appUI.showRunButton();
            appUI.enableScreenshotButton(true);
            appUI.setAlgorithmRunning(false);
            appUI.changeRunButton(0);
        });
    }
}
