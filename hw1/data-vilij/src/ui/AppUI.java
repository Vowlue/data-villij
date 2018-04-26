package ui;

import actions.AppActions;
import algorithms.Algorithm;
import classification.RandomClassifier;
import components.RunConfiguration;
import components.YesNoDialog;
import data.DataSet;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

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
    private String                       algorithmSelected;
    private Button                       runButton;
    private Dialog                       runConfig;
    private VBox                         metaPane;
    private VBox                         runPane;
    private ArrayList<RadioButton>       radioButtons;
    private HashMap<String, RunConfiguration.ClassificationConfig> classificationHashMap;
    private HashMap<String, RunConfiguration.ClusteringConfig> clusteringHashMap;
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

    public void setAlgorithmRunning(boolean algorithmRunning) {
        this.algorithmRunning = algorithmRunning;
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
        classificationHashMap = new HashMap<>();
        clusteringHashMap = new HashMap<>();
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
        if(type == 0)
            runImage.setImage(new Image(getClass().getResourceAsStream(runPath)));
        else
            runImage.setImage(new Image(getClass().getResourceAsStream(continuePath)));
    }

    private void continueAlgorithm(){
        synchronized (this){
            algorithmPaused = false;
            this.notifyAll();
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
                //check on this after
                if (!classificationHashMap.containsKey(algorithmSelected) && !clusteringHashMap.containsKey(algorithmSelected))
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
                    switch (algorithmSelected) {
                        case "Random Classifier":
                            RunConfiguration.ClassificationConfig config = classificationHashMap.get(manager.getPropertyValue(RANDOM_CLASSIFIER.name()));
                            Algorithm classifier = new RandomClassifier(applicationTemplate, dataSet, config.getMaxIterations(), config.getUpdateInterval(), config.isContinuous());
                            new Thread(classifier).start();
                            return;
                        default:

                    }
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

        ToggleGroup classGroup = new ToggleGroup();
        HBox classificationAlg1 = createAlgorithmOption(manager.getPropertyValue(RANDOM_CLASSIFIER.name()), classGroup, manager.getPropertyValue(CLASSIFICATION.name()));
        classificationSpace = new VBox(classificationAlg1);
        ToggleGroup clustGroup = new ToggleGroup();
        HBox clusteringAlg1 = createAlgorithmOption(manager.getPropertyValue(RANDOM_CLUSTERER.name()), clustGroup, manager.getPropertyValue(CLUSTERING.name()));
        clusteringSpace = new VBox(clusteringAlg1);
        algorithmSpace = new VBox();

        dataSpace = new VBox(textArea, toggleButton, new Separator());
        runPane = new VBox();
        Pane userSpace = new VBox(dataSpace, algorithmSpace, runPane);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setTitle(manager.getPropertyValue(DATA_VISUALIZATION.name()));
        chart.setMinSize(windowWidth*0.65, windowHeight*0.7);
        chart.setMaxHeight(windowHeight*0.7);
        chart.setLegendVisible(false);
        workspace.getChildren().addAll(userSpace, chart);
        appPane.getChildren().add(workspace);
        appPane.getStylesheets().add(manager.getPropertyValue(CSS_PATH.name()));
    }

    private HBox createAlgorithmOption(String name, ToggleGroup group, String algoType){
        RadioButton button = new RadioButton(name);
        button.setToggleGroup(group);
        button.setOnMouseClicked(e -> {
            algorithmSelected = name;
            showRunButton();
        });
        radioButtons.add(button);
        ImageView settingButton = new ImageView(new Image(getClass().getResourceAsStream(cogPath)));
        settingButton.setOnMouseClicked(e -> {
            RunConfiguration runConfiguration = (RunConfiguration)runConfig;
            if(algoType.equals(manager.getPropertyValue(CLASSIFICATION.name()))){
                if(!classificationHashMap.containsKey(name))
                    classificationHashMap.put(name, new RunConfiguration.ClassificationConfig());
                runConfiguration.openConfig(name+manager.getPropertyValue(RUN_CONFIGURATION.name()), classificationHashMap.get(name));
            }
            else{
                if(!clusteringHashMap.containsKey(name))
                    clusteringHashMap.put(name, new RunConfiguration.ClusteringConfig());
                runConfiguration.openConfig(name+manager.getPropertyValue(RUN_CONFIGURATION.name()), clusteringHashMap.get(name));
            }
        });
        return new HBox(button, settingButton);
    }

    public void hideRunButton(){ runPane.getChildren().remove(runButton); }
    public void showRunButton(){ if(!runPane.getChildren().contains(runButton)) runPane.getChildren().add(runButton); }
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
