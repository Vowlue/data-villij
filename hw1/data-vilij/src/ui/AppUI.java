package ui;

import actions.AppActions;
import algorithmbase.Algorithm;
import components.RunConfiguration;
import components.YesNoDialog;
import data.*;
import dataprocessors.AppData;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Pane                         dataSpace;      // the half of the workspace devoted to data
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       toggleButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private Label                        metaLabel;
    private ComboBox<String>             comboBox;
    private Pane                         classificationSpace;
    private Pane                         clusteringSpace;
    private Pane                         algorithmSpace;
    private Class                        algorithmSelected;
    private Button                       runButton;
    private Dialog                       runConfig;
    private VBox                         metaPane;
    private VBox                         runPane;
    private ArrayList<RadioButton>       radioButtons;
    private HashMap<String, RunConfiguration.ConfigInfo> configInfoHashMap;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private boolean                      algorithmPaused;
    private boolean                      algorithmRunning;
    private ImageView runImage;

    private static final String SEPARATOR = "/";
    private String ssPath;
    private String runPath;
    private String cogPath;
    private String continuePath;
    private PropertyManager manager;

    public LineChart<Number, Number> getChart() { return chart; }
    public TextArea getTextArea(){
        return textArea;
    }
    public Label getMetaLabel() { return metaLabel; }
    public ComboBox<String> getComboBox() { return comboBox; }
    public Button getSaveButton() { return saveButton; }

    public boolean isAlgorithmRunning() {
        return algorithmRunning;
    }

    AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        algorithmSelected = null;
        workspace = new HBox();
        runConfig = RunConfiguration.getRunConfiguration();
        YesNoDialog.getDialog().init(primaryStage);
        ((RunConfiguration) runConfig).windInit(primaryStage, manager);
        radioButtons = new ArrayList<>();
        configInfoHashMap = new HashMap<>();
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        algorithmPaused = false;
        algorithmRunning = false;

    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        ssPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
        runPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(RUN_ICON.name()));
        cogPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(COG_ICON.name()));
        continuePath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(CONTINUE_ICON.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        scrnshotButton = super.setToolbarButton(ssPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        saveButton.setDisable(true);
        scrnshotButton.setDisable(true);
        toggleButton.setText(manager.getPropertyValue(DONE.name()));
        toggleButton.setVisible(false);
        ((AppData)applicationTemplate.getDataComponent()).removeAlgorithmTrace(chart);

        removeExcess();
    }
    private void removeExcess(){
        chart.getData().clear();
        algorithmSelected = null;
        for(RadioButton b: radioButtons){
            b.setSelected(false);
        }
        hideComboBox();
        hideClassification();
        hideClustering();
        hideMetaLabel();
        hideRunButton();
    }

    public boolean isAlgorithmPaused() {
        return algorithmPaused;
    }

    public void setAlgorithmPaused(boolean algorithmPaused) {
        this.algorithmPaused = algorithmPaused;
    }

    public void changeRunButton(int type){
        runButton.setDisable(true);
        if(type == 0)
            runImage.setImage(new Image(getClass().getResourceAsStream(runPath)));
        else
            runImage.setImage(new Image(getClass().getResourceAsStream(continuePath)));
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {

        }
        runButton.setDisable(false);
    }

    private void continueAlgorithm(){
        synchronized (applicationTemplate.manager){
            algorithmPaused = false;
            applicationTemplate.manager.notifyAll();
        }
    }

    private void layout() {
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            applicationTemplate.getActionComponent().handleExitRequest();
        });
        newButton.setDisable(false);
        scrnshotButton.setDisable(true);

        runImage = new ImageView(new Image(getClass().getResourceAsStream(runPath)));
        runButton = new Button(null, runImage);
        runButton.setOnMouseClicked(e -> {
            if(algorithmPaused){
                continueAlgorithm();
            }
            else {
                if (!configInfoHashMap.containsKey(algorithmSelected.getSimpleName()))
                    applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(CHOOSE_CONFIGURATION.name()), manager.getPropertyValue(NO_CONFIG.name()));
                else {
                    DataSet dataSet = new DataSet();
                    Scanner scanner = new Scanner(textArea.getText());
                    while (scanner.hasNextLine()) {
                        try {
                            dataSet.addInstance(scanner.nextLine());
                        } catch (DataSet.InvalidDataNameException e1) {
                            e1.printStackTrace();
                        }
                    }
                    algorithmRunning = true;
                    RunConfiguration.ConfigInfo c = configInfoHashMap.get(algorithmSelected.getSimpleName());
                    try {
                        if(algorithmSelected.getSuperclass().equals(Class.forName("algorithmbase.Clusterer"))) {
                            DataCollector dataCollector = new DataCollector();
                            Algorithm algorithm = (Algorithm)algorithmSelected.getConstructors()[0].newInstance(dataCollector, dataSet, c.getMaxIterations(), c.getUpdateInterval(), ((RunConfiguration.ClusteringConfig)c).getLabelNumber());
                            new Thread(algorithm).start();
                            new Thread(new DataRunner(dataCollector, applicationTemplate, c.isContinuous())).start();
                        }
                        else{
                            ListCollector listCollector = new ListCollector();
                            Algorithm algorithm = (Algorithm)algorithmSelected.getConstructors()[0].newInstance(listCollector, dataSet, c.getMaxIterations(), c.getUpdateInterval());
                            new Thread(algorithm).start();
                            new Thread(new ListRunner(listCollector, applicationTemplate, c.isContinuous())).start();
                        }
                    } catch (Exception e1) { e1.printStackTrace(); }
                }
            }
        });

        textArea = new TextArea();
        textArea.setPrefHeight(windowHeight/3);
        textArea.setVisible(false);

        metaLabel = new Label();
        metaLabel.setWrapText(true);
        metaPane = new VBox(metaLabel, new Separator());
        comboBox = new ComboBox<>();
        toggleButton = new Button(manager.getPropertyValue(DONE.name()));
        toggleButton.setVisible(false);

        File algoFolder = new File("hw1/data-vilij/src/algorithms");
        File typeFolder = new File("hw1/data-vilij/src/algorithmbase");
        List<Class> classList = new ArrayList<>();
        List<Class> typeList = new ArrayList<>();
        for(File file: Objects.requireNonNull(algoFolder.listFiles())){
            try {
                classList.add(Class.forName("algorithms."+removeExtension(file.getName())));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        for(File file: Objects.requireNonNull(typeFolder.listFiles())){
            try {
                typeList.add(Class.forName("algorithmbase."+removeExtension(file.getName())));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        ToggleGroup classGroup = new ToggleGroup();
        classificationSpace = new VBox();
        ToggleGroup clustGroup = new ToggleGroup();
        clusteringSpace = new VBox();
        boolean classif = false;
        for(Class type: typeList){
            if(!type.isInterface()){
                for(Class algorithm: classList){
                    if(classif) {
                        try {
                            if(algorithm.getSuperclass().equals(Class.forName(type.getName())))
                                classificationSpace.getChildren().add(createAlgorithmOption(algorithm, classGroup, true));

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        try {
                            if(algorithm.getSuperclass().equals(Class.forName(type.getName())))
                                clusteringSpace.getChildren().add(createAlgorithmOption(algorithm, clustGroup, false));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            classif = !classif;
        }

        algorithmSpace = new VBox();

        dataSpace = new VBox(textArea, toggleButton, new Separator());
        runPane = new VBox();
        Pane userSpace = new VBox(dataSpace, algorithmSpace, runPane);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setTitle(manager.getPropertyValue(DATA_VISUALIZATION.name()));
        chart.setMinSize(windowWidth*0.65, windowHeight*0.7);
        chart.setMaxHeight(windowHeight*0.7);
        workspace.getChildren().addAll(userSpace, chart);
        appPane.getChildren().add(workspace);
        appPane.getStylesheets().add(manager.getPropertyValue(CSS_PATH.name()));
    }

    private HBox createAlgorithmOption(Class algorithm, ToggleGroup group, boolean isClassification){
        String name = algorithm.getSimpleName();
        RadioButton button = new RadioButton(name);
        button.setToggleGroup(group);
        button.setOnMouseClicked(e -> {
            algorithmSelected = algorithm;
            showRunButton();
        });
        radioButtons.add(button);
        ImageView settingButton = new ImageView(new Image(getClass().getResourceAsStream(cogPath)));
        settingButton.setOnMouseClicked(e -> {
            RunConfiguration runConfiguration = (RunConfiguration)runConfig;
            if(isClassification) {
                if (!configInfoHashMap.containsKey(name))
                    configInfoHashMap.put(name, new RunConfiguration.ConfigInfo());
                runConfiguration.openConfig(name + manager.getPropertyValue(RUN_CONFIGURATION.name()), configInfoHashMap.get(name));
            }
            else{
                if (!configInfoHashMap.containsKey(name))
                    configInfoHashMap.put(name, new RunConfiguration.ClusteringConfig());
                runConfiguration.openConfig(name + manager.getPropertyValue(RUN_CONFIGURATION.name()), configInfoHashMap.get(name));
            }
        });
        return new HBox(button, settingButton);
    }
    private String removeExtension(String fileName){
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public void unselectRadioButtons(){
        for(RadioButton radioButton: radioButtons){
            radioButton.setSelected(false);
        }
    }

    public void hideRunButton(){ runPane.getChildren().remove(runButton); }
    private void showRunButton(){ if(!runPane.getChildren().contains(runButton)) runPane.getChildren().add(runButton); }
    public void enableScreenshotButton(boolean b){
        scrnshotButton.setDisable(!b);
    }
    public void enableSaveButton(boolean b){
        saveButton.setDisable(!b);
    }
    private void hideComboBox(){algorithmSpace.getChildren().remove(comboBox); }
    public void showComboBox(){ if(!algorithmSpace.getChildren().contains(comboBox)) algorithmSpace.getChildren().add(comboBox); }
    public void hideClassification(){algorithmSpace.getChildren().remove(classificationSpace); }
    public void showClassification(){ if(!algorithmSpace.getChildren().contains(classificationSpace)) algorithmSpace.getChildren().add(classificationSpace); }
    public void hideClustering(){algorithmSpace.getChildren().remove(clusteringSpace); }
    public void showClustering(){ if(!algorithmSpace.getChildren().contains(clusteringSpace)) algorithmSpace.getChildren().add(clusteringSpace); }
    private void hideMetaLabel(){dataSpace.getChildren().remove(metaPane); }
    public void showMetaLabel(){ if(!dataSpace.getChildren().contains(metaPane)) dataSpace.getChildren().add(metaPane); }
    public void showToggleButton(){ toggleButton.setVisible(true); }

    public void finishAlgorithm(){
        this.showRunButton();
        this.enableScreenshotButton(true);
        this.algorithmRunning = false;
        this.changeRunButton(0);
    }

    private void setWorkspaceActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        saveButton.setDisable(false);
                    } else {
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });

        scrnshotButton.setOnAction(e ->{
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            }
            catch(IOException f){
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(SCREENSHOT_ERROR_TITLE.name()), manager.getPropertyValue(SCREENSHOT_ERROR_MSG.name()));
            }
        });

        toggleButton.setOnAction(e -> {
            if(toggleButton.getText().equals(manager.getPropertyValue(DONE.name()))){
                chart.getData().clear();
                ((AppData)applicationTemplate.getDataComponent()).loadData(textArea.getText());
                if(((AppData)applicationTemplate.getDataComponent()).isDataIsValid()) {
                    toggleButton.setText(manager.getPropertyValue(EDIT.name()));
                    textArea.setDisable(true);
                }
                else
                    removeExcess();
            }
            else{
                toggleButton.setText(manager.getPropertyValue(DONE.name()));
                textArea.setDisable(false);
                hideClustering();
                hideClassification();
            }
        });
    }
}
