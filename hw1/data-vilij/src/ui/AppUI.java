package ui;

import actions.AppActions;
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
    private CheckBox                     checkBox;       // when checked, makes textarea read-only
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       toggleButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display (feels unneeded)
    private Label                        label;
    private ComboBox<String>             comboBox;
    private Pane                         classificationSpace;
    private Pane                         clusteringSpace;
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private static final String SEPARATOR = "/";
    private String ssPath;
    private PropertyManager manager;

    public LineChart<Number, Number> getChart() { return chart; }

    public TextArea getTextArea(){
        return textArea;
    }

    public Label getLabel() { return label; }

    public ComboBox<String> getComboBox() { return comboBox; }

    public Button getToggleButton() { return toggleButton; }

    public Button getSaveButton() { return saveButton; }

    public Pane getDataSpace() { return dataSpace; }

    AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        workspace = new HBox();
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
        chart.getData().clear();
        saveButton.setDisable(true);
        scrnshotButton.setDisable(true);
        toggleButton.setVisible(false);
        showComboBox();
        comboBox.setVisible(false);
        hideClassification();
        hideClustering();
    }

    private void layout() {
        newButton.setDisable(false);
        scrnshotButton.setDisable(true);

        textArea = new TextArea();
        textArea.setPrefHeight(windowHeight/3);
        textArea.setVisible(false);
        label = new Label();
        label.setWrapText(true);
        comboBox = new ComboBox<>();
        comboBox.setVisible(false);
        toggleButton = new Button("Done");
        toggleButton.setVisible(false);

        Label classificationLabel = new Label("Classification");
        RadioButton classr1 = new RadioButton("Random Classifier");
        ImageView classi1 = new ImageView(new Image(getClass().getResourceAsStream("/gui/icons/cog.png")));
        HBox classificationAlg1 = new HBox(classr1, classi1);
        classificationSpace = new VBox(classificationLabel, classificationAlg1);
        Label clusteringLabel = new Label("Clustering");
        RadioButton clustr1 = new RadioButton("Useless Clusterer");
        ImageView clusti1 = new ImageView(new Image(getClass().getResourceAsStream("/gui/icons/cog.png")));
        HBox clusteringAlg1 = new HBox(clustr1, clusti1);
        clusteringSpace = new VBox(clusteringLabel, clusteringAlg1);

        dataSpace = new VBox(textArea, toggleButton, label, comboBox);

        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(DATA_VISUALIZATION.name()));
        chart.setMinSize(windowWidth*0.65, windowHeight*0.7);
        chart.setMaxHeight(windowHeight*0.7);
        chart.setLegendVisible(false);
        workspace.getChildren().addAll(dataSpace, chart);
        appPane.getChildren().add(workspace);
        appPane.getStylesheets().add(manager.getPropertyValue(CSS_PATH.name()));
    }

    public void enableScreenshotButton(boolean b){
        scrnshotButton.setDisable(!b);
    }
    public void enableSaveButton(boolean b){
        saveButton.setDisable(!b);
    }
    public void hideComboBox(){ dataSpace.getChildren().remove(comboBox); }
    public void showComboBox(){ if(!dataSpace.getChildren().contains(comboBox)) dataSpace.getChildren().add(comboBox); }
    public void hideClassification(){ dataSpace.getChildren().remove(classificationSpace); }
    public void showClassification(){ if(!dataSpace.getChildren().contains(classificationSpace)) dataSpace.getChildren().add(classificationSpace); }
    public void hideClustering(){ dataSpace.getChildren().remove(clusteringSpace); }
    public void showClustering(){ if(!dataSpace.getChildren().contains(clusteringSpace)) dataSpace.getChildren().add(clusteringSpace); }

    private void setWorkspaceActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        if (newValue.charAt(newValue.length() - 1) == '\n')
                            hasNewText = true;
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
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
            }
            else{
                toggleButton.setText("Done");
                textArea.setDisable(false);
                //go back to editing
            }

        });
        /*displayButton.setOnAction(e -> {
            if(hasNewText) {
                applicationTemplate.getDataComponent().clear();
                getChart().getData().clear();
                ((AppData)applicationTemplate.getDataComponent()).loadData(textArea.getText());
            }
        });

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> textArea.setDisable(newValue));*/
    }
}
