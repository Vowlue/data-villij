package classification;

import algorithms.Classifier;
import data.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.concurrent.Task;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();
    private final long SLEEPTIME = 1000;

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
        //int i = 1; i <= maxIterations && tocontinue.get(); i++
        for (int i = 1; i <= maxIterations; i++) {
            int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int constant     = new Double(RAND.nextDouble() * 100).intValue();

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            /*if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i);
                flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                break;
            }*/
            if(i % updateInterval == 0 || i > maxIterations * .6 && RAND.nextDouble() < 0.05){
                Task<Void> iterationTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
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
                        Thread.sleep(SLEEPTIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    new Thread(iterationTask).start();
                    appUI.setAlgorithmPaused(true);
                    appUI.changeRunButton("continue");
                    synchronized (applicationTemplate){
                        while(appUI.isAlgorithmPaused()){
                            try {
                                applicationTemplate.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        new Thread(iterationTask).start();
                    }
                    //continueOnAlgorithm(iterationTask);
                }
                /*new Thread(iterationTask).start();
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                appUI.showRunButton();
                appUI.enableScreenshotButton(true);
                appUI.setAlgorithmRunning(false);
                appUI.changeRunButton("run");
            }
        });
    }

    private void continueOnAlgorithm(Task<Void> task){
        AppUI appUI = (AppUI)applicationTemplate.getUIComponent();
        System.out.println("this got called");
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                System.out.println(appUI.isAlgorithmPaused());
                while(appUI.isAlgorithmPaused()){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                new Thread(task).start();
                appUI.setAlgorithmPaused(true);
                return null;
            }
        }).start();
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }
}
