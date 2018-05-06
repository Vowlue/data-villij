package components;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;

import static settings.AppPropertyTypes.*;

public class RunConfiguration extends Stage implements Dialog {

    public static class ClusteringConfig extends ConfigInfo{
        private int labelNumber;
        public ClusteringConfig(){
            super();
            this.labelNumber = 3;
        }

        public int getLabelNumber() {
            return labelNumber;
        }
        void setLabelNumber(int labelNumber) {
            this.labelNumber = labelNumber;
        }
        int confirmLabels(String labels) {
            try{
                int i = Integer.parseInt(labels);
                if(i<2)
                    return 2;
                else if(i>4)
                    return 4;
                return i;
            }
            catch(Exception e){
                return 1;
            }
        }
    }

    public static class ConfigInfo{
        private int maxIterations;
        private int updateInterval;
        private boolean continuous;
        public ConfigInfo(){
            this.maxIterations = 40; this.updateInterval = 5; this.continuous = true;
        }

        public int getMaxIterations() {
            return maxIterations;
        }
        void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }
        public int getUpdateInterval() {
            return updateInterval;
        }
        void setUpdateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
        }
        public boolean isContinuous() {
            return continuous;
        }
        void setContinuous(boolean continuous) {
            this.continuous = continuous;
        }
        int confirmIterations(String iterations){
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

        int confirmInterval(String interval, int iterations){
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


    private static RunConfiguration config;
    private TextField iterationField;
    private TextField updateField;

    private CheckBox continuousCheckBox;
    private TextField labelNumberField;
    private Button setConfig;
    private HBox labelContainer;
    private VBox choiceContainer;
    private PropertyManager manager;

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

    public void windInit(Stage owner, PropertyManager manager){
        this.manager = manager;
        init(owner);
    }

    @Override
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        Label iterationLabel = new Label(manager.getPropertyValue(MAX_ITER.name()));
        iterationField = new TextField();
        HBox iterationPane = new HBox(iterationLabel, iterationField);
        Label updateLabel = new Label(manager.getPropertyValue(UPDATE_INTER.name()));
        updateField = new TextField();
        HBox updatePane = new HBox(updateLabel, updateField);
        Label continuousLabel = new Label(manager.getPropertyValue(CONT.name()));
        continuousCheckBox = new CheckBox();
        HBox continuousPane = new HBox(continuousLabel, continuousCheckBox);
        Label labelNumber = new Label(manager.getPropertyValue(LABEL_NUM.name()));
        labelNumberField = new TextField();
        labelContainer = new HBox(labelNumber, labelNumberField);
        choiceContainer = new VBox(iterationPane, updatePane, continuousPane);
        setConfig = new Button(manager.getPropertyValue(SET_CONFIG.name()));
        VBox container = new VBox(choiceContainer, new Separator(), setConfig);
        container.setAlignment(Pos.CENTER);
        Scene configScene = new Scene(container);
        this.setScene(configScene);
    }

    public void openConfig(String title, ConfigInfo config){
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
            setConfig.setOnAction(e -> applyClusteringSettings(c));
        }
        else {
            choiceContainer.getChildren().remove(labelContainer);
            setConfig.setOnAction(e -> applyClassificationSettings(config));
        }
        show(title, "");
    }

    private void applyClassificationSettings(ConfigInfo config){
        int iterations = config.confirmIterations(iterationField.getText());
        iterationField.setText(""+iterations);
        config.setMaxIterations(iterations);
        int interval = config.confirmInterval(updateField.getText(), iterations);
        config.setUpdateInterval(interval);
        updateField.setText(""+interval);
        config.setContinuous(continuousCheckBox.isSelected());
    }

    private void applyClusteringSettings(ClusteringConfig config){
        applyClassificationSettings(config);
        int labelNum = config.confirmLabels(labelNumberField.getText());
        labelNumberField.setText(""+labelNum);
        config.setLabelNumber(labelNum);
    }

}
