package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display (feels unneeded)
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private static final String SEPARATOR = "/";
    private String ssPath;
    private PropertyManager manager;

    public LineChart<Number, Number> getChart() { return chart; }

    public TextArea getTextArea(){
        return textArea;
    }

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
        newButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private void layout() {
        Label displayTitle = new Label(manager.getPropertyValue(TEXT_AREA.name()));
        displayTitle.setPrefWidth(windowWidth/2);
        displayTitle.setFont(new Font(19));
        displayTitle.setAlignment(Pos.CENTER);
        textArea = new TextArea();
        textArea.setPrefHeight(windowHeight/3);
        displayButton = new Button(manager.getPropertyValue(DISPLAY.name()));
        checkBox = new CheckBox(manager.getPropertyValue(READ_ONLY.name()));
        Pane bottomOptions = new VBox(displayButton, checkBox);
        dataSpace = new VBox(displayTitle, textArea, bottomOptions);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(DATA_VISUALIZATION.name()));
        chart.setPrefSize(windowWidth*0.9, windowHeight*0.66);
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

    private void setWorkspaceActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions)applicationTemplate.getActionComponent()).setIsSaved(false);
                        if (newValue.charAt(newValue.length() - 1) == '\n')
                            hasNewText = true;
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
                int oldLines = ((AppData)applicationTemplate.getDataComponent()).getLineCount(oldValue);
                int newLines = ((AppData)applicationTemplate.getDataComponent()).getLineCount(newValue);
                if(((AppData)applicationTemplate.getDataComponent()).isOverflow() && newLines < oldLines)
                    ((AppData)applicationTemplate.getDataComponent()).transferLines(oldLines-newLines);
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

        displayButton.setOnAction(e -> {
            if(hasNewText) {
                applicationTemplate.getDataComponent().clear();
                getChart().getData().clear();
                ((AppData)applicationTemplate.getDataComponent()).loadData(textArea.getText());
            }
        });

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> textArea.setDisable(newValue));
    }
}
