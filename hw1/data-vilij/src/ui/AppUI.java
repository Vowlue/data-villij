package ui;

import actions.AppActions;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
import static settings.AppPropertyTypes.*;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Pane                         dataSpace;      // the half of the workspace devoted to data
    private Pane                         graphSpace;     // the other half for the graph
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private Label                        displayTitle;   // label for title
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private static final String SEPARATOR = "/";
    private static final String TEXT_TITLE = "Data File";
    private static final String DISPLAY = "Display";
    private static final String DATA_VISUALIZATION = "Data Visualization";
    private String ssPath;

    public ScatterChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        workspace = new HBox();
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        ssPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        // TODO for homework 1
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
        // TODO for homework 1 i think clears the chart?
    }

    private void layout() {
        // TODO for homework 1 i think does the layout of the charts etc
        displayTitle = new Label(TEXT_TITLE);
        textArea = new TextArea();
        displayButton = new Button(DISPLAY);
        dataSpace = new VBox(displayTitle, textArea, displayButton);
        chart = new ScatterChart<>(xAxis, yAxis);
        chart.setTitle(DATA_VISUALIZATION);
        chart.setPrefSize(windowWidth*0.9, windowHeight*0.66);
        graphSpace = new VBox(chart);
        workspace.getChildren().addAll(dataSpace, chart);
        appPane.getChildren().add(workspace);
    }

    private void setWorkspaceActions() {
        // TODO for homework 1
        // must make the new button only enable when there is text in the textarea and disable when there isnt
        // same with save?
        textArea.textProperty().addListener(e -> {
            newButton.setDisable(false);
            saveButton.setDisable(false);
        });
        displayButton.setOnAction(e -> System.out.println(textArea.getText()));

    }
}
