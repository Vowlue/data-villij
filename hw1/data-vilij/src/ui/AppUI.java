package ui;

import actions.AppActions;
import components.RunConfiguration;
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
    private ImageView                    runButton;
    private Dialog                       runConfig;
    private VBox metaPane;
    private ArrayList<RadioButton> radioButtons;
    private HashMap<String, RunConfiguration.ClassificationConfig> classificationHashMap;
    private HashMap<String, RunConfiguration.ClusteringConfig> clusteringHashMap;
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private static final String SEPARATOR = "/";
    private String ssPath;
    private PropertyManager manager;

    public LineChart<Number, Number> getChart() { return chart; }
    public TextArea getTextArea(){
        return textArea;
    }
    public Label getMetaLabel() { return metaLabel; }
    public ComboBox<String> getComboBox() { return comboBox; }
    public Button getSaveButton() { return saveButton; }

    AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        algorithmSelected = null;
        workspace = new HBox();
        runConfig = RunConfiguration.getRunConfiguration();
        runConfig.init(primaryStage);
        radioButtons = new ArrayList<>();
        classificationHashMap = new HashMap<>();
        clusteringHashMap = new HashMap<>();
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        ssPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
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
        toggleButton.setText("Done");
        toggleButton.setVisible(false);

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

    private void layout() {
        newButton.setDisable(false);
        scrnshotButton.setDisable(true);

        runButton = new ImageView(new Image(getClass().getResourceAsStream("/gui/icons/play.png")));
        runButton.setPreserveRatio(true);
        runButton.fitWidthProperty().bind(primaryStage.widthProperty().divide(30));
        runButton.setOnMouseClicked(e -> {
            if(!classificationHashMap.containsKey(algorithmSelected) && !clusteringHashMap.containsKey(algorithmSelected))
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show("Choose a configuration for your algorithm", "This algorithm has no configuration.");
            System.out.println("play");
        });

        textArea = new TextArea();
        textArea.setPrefHeight(windowHeight/3);
        textArea.setVisible(false);

        metaLabel = new Label();
        metaLabel.setWrapText(true);
        metaPane = new VBox(metaLabel, new Separator());
        comboBox = new ComboBox<>();
        toggleButton = new Button("Done");
        toggleButton.setVisible(false);

        ToggleGroup classGroup = new ToggleGroup();
        Label classificationLabel = new Label("Classification");
        classificationLabel.getStyleClass().add("title");
        HBox classificationAlg1 = createAlgorithmOption("Random Classifier", classGroup, "Classification");
        classificationSpace = new VBox(classificationLabel, classificationAlg1);
        ToggleGroup clustGroup = new ToggleGroup();
        Label clusteringLabel = new Label("Clustering");
        clusteringLabel.getStyleClass().add("title");
        HBox clusteringAlg1 = createAlgorithmOption("Useless Clusterer", clustGroup, "Clustering");
        HBox clusteringAlg2 = createAlgorithmOption("More Useless Clusterer", clustGroup, "Clustering");
        clusteringSpace = new VBox(clusteringLabel, clusteringAlg1, clusteringAlg2);
        algorithmSpace = new VBox();

        dataSpace = new VBox(textArea, toggleButton, new Separator());
        Pane userSpace = new VBox(dataSpace, algorithmSpace);

        chart = new LineChart<>(xAxis, yAxis);
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
        ImageView settingButton = new ImageView(new Image(getClass().getResourceAsStream("/gui/icons/cog.png")));
        settingButton.setOnMouseClicked(e -> {
            RunConfiguration runConfiguration = (RunConfiguration)runConfig;
            if(algoType.equals("Classification")){
                if(!classificationHashMap.containsKey(name))
                    classificationHashMap.put(name, new RunConfiguration.ClassificationConfig());
                runConfiguration.openConfig(name+" RunConfiguration", classificationHashMap.get(name));
            }
            else{
                if(!clusteringHashMap.containsKey(name))
                    clusteringHashMap.put(name, new RunConfiguration.ClusteringConfig());
                runConfiguration.openConfig(name+" RunConfiguration", clusteringHashMap.get(name));
            }
        });
        return new HBox(button, settingButton);
    }

    private void hideRunButton(){algorithmSpace.getChildren().remove(runButton); }
    private void showRunButton(){ if(!algorithmSpace.getChildren().contains(runButton)) algorithmSpace.getChildren().add(runButton); }
    public void enableScreenshotButton(boolean b){
        scrnshotButton.setDisable(!b);
    }
    public void enableSaveButton(boolean b){
        saveButton.setDisable(!b);
    }
    public void hideComboBox(){algorithmSpace.getChildren().remove(comboBox); }
    public void showComboBox(){ if(!algorithmSpace.getChildren().contains(comboBox)) algorithmSpace.getChildren().add(comboBox); }
    private void hideClassification(){algorithmSpace.getChildren().remove(classificationSpace); }
    public void showClassification(){ if(!algorithmSpace.getChildren().contains(classificationSpace)) algorithmSpace.getChildren().add(classificationSpace); }
    private void hideClustering(){algorithmSpace.getChildren().remove(clusteringSpace); }
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
            if(toggleButton.getText().equals("Done")){
                chart.getData().clear();
                ((AppData)applicationTemplate.getDataComponent()).loadData(textArea.getText());
                if(((AppData)applicationTemplate.getDataComponent()).isDataIsValid()) {
                    toggleButton.setText("Edit");
                    textArea.setDisable(true);
                }
                else
                    removeExcess();
            }
            else{
                toggleButton.setText("Done");
                textArea.setDisable(false);
                hideClustering();
                hideClassification();
            }

        });
    }
}
