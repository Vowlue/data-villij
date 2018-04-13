package components;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.components.Dialog;

public class RunConfiguration extends Stage implements Dialog {

    public static class ClusteringConfig extends ClassificationConfig{
        private int labelNumber;
        public ClusteringConfig(){
            super();
            this.labelNumber = 2;
        }

        int getLabelNumber() {
            return labelNumber;
        }

        void setLabelNumber(int labelNumber) {
            this.labelNumber = labelNumber;
        }
    }

    public static class ClassificationConfig{
        private int maxIterations;
        private int updateInterval;
        private boolean continuous;
        public ClassificationConfig(){
            this.maxIterations = 1000; this.updateInterval = 5; this.continuous = false;
        }

        int getMaxIterations() {
            return maxIterations;
        }

        void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        int getUpdateInterval() {
            return updateInterval;
        }

        void setUpdateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
        }

        boolean isContinuous() {
            return continuous;
        }

        void setContinuous(boolean continuous) {
            this.continuous = continuous;
        }
    }


    private static RunConfiguration config;
    private TextField iterationField;
    private TextField updateField;
    private CheckBox continuousCheckBox;
    private TextField labelNumberField;
    private Button setConfig;
    private HBox labelContainer;
    private VBox choiceContainer;

    private RunConfiguration(){}

    public static RunConfiguration getRunConfiguration(){
        if(config == null)
            config = new RunConfiguration();
        return config;
    }

    @Override
    public void show(String title, String message) {
        setTitle(title);    // set the title of the dialog
        showAndWait();
    }

    @Override
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        Label iterationLabel = new Label("Maximum Iterations: ");
        iterationField = new TextField();
        HBox iterationPane = new HBox(iterationLabel, iterationField);
        Label updateLabel = new Label("Update Interval: ");
        updateField = new TextField();
        HBox updatePane = new HBox(updateLabel, updateField);
        Label continuousLabel = new Label("Continuous Run? ");
        continuousCheckBox = new CheckBox();
        HBox continuousPane = new HBox(continuousLabel, continuousCheckBox);
        Label labelNumber = new Label("Number of Labels: ");
        labelNumberField = new TextField();
        labelContainer = new HBox(labelNumber, labelNumberField);
        choiceContainer = new VBox(iterationPane, updatePane, continuousPane);
        setConfig = new Button("Set Configuration");
        VBox container = new VBox(choiceContainer, new Separator(), setConfig);
        container.setAlignment(Pos.CENTER);
        Scene configScene = new Scene(container);
        this.setScene(configScene);
    }

    public void openConfig(String title, ClassificationConfig config){
        iterationField.setText(""+config.getMaxIterations());
        updateField.setText(""+config.getUpdateInterval());
        continuousCheckBox.setSelected(config.isContinuous());
        setConfig.setOnAction(null);
        // clusters need number of labels
        if(config instanceof ClusteringConfig) {
            ClusteringConfig c = (ClusteringConfig)config;
            labelNumberField.setText(""+c.getLabelNumber());
            if (!choiceContainer.getChildren().contains(labelContainer))
                choiceContainer.getChildren().add(labelContainer);
            setConfig.setOnAction(e -> applyClusteringSettings(config));
        }
        else {
            choiceContainer.getChildren().remove(labelContainer);
            setConfig.setOnAction(e -> applyClassificationSettings(config));
        }
        show(title, "");
    }

    private void applyClassificationSettings(ClassificationConfig config){
        int iterations = confirmIterations(iterationField.getText());
        iterationField.setText(""+iterations);
        config.setMaxIterations(iterations);
        int interval = confirmInterval(updateField.getText(), iterations);
        config.setUpdateInterval(interval);
        updateField.setText(""+interval);
        config.setContinuous(continuousCheckBox.isSelected());
    }

    private void applyClusteringSettings(ClassificationConfig config){
        applyClassificationSettings(config);
        ClusteringConfig c = (ClusteringConfig)config;
        int labelNum = confirmLabels(labelNumberField.getText());
        labelNumberField.setText(""+labelNum);
        c.setLabelNumber(labelNum);
    }

    private int confirmLabels(String labels) {
        try{
            int i = Integer.parseInt(labels);
            if(i<1)
                return 1;
            return i;
        }
        catch(Exception e){
            return 1;
        }
    }

    private int confirmIterations(String iterations){
        try{
            int i = Integer.parseInt(iterations);
            if(i<0)
                return 1;
            return i;
        }
        catch(Exception e){
            return 1;
        }
    }

    private int confirmInterval(String interval, int iterations){
        try{
            int i = Integer.parseInt(interval);
            if(i<0)
                return 1;
            if(i>iterations)
                return iterations;
            return i;
        }
        catch(Exception e){
            return 1;
        }
    }
}
