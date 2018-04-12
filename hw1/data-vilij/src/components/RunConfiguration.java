package components;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.components.Dialog;

public class RunConfiguration extends Stage implements Dialog {
    enum AlgorithmType{
        CLASSIFICATION, CLUSTERING
    }
    private static RunConfiguration config;
    private AlgorithmType type;

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

        Label title = new Label("Algorithm Run Configuration");
        Label iterationLabel = new Label("Maximum Iterations: ");
        TextField iterationField = new TextField("1000");
        HBox iterationPane = new HBox(iterationLabel, iterationField);
        Label updateLabel = new Label("Update Interval: ");
        TextField updateField = new TextField("5");
        HBox updatePane = new HBox(updateLabel, updateField);
        Label continuousLabel = new Label("Continuous Run? ");
        CheckBox continuousCheckBox = new CheckBox();
        HBox continuousPane = new HBox(continuousLabel, continuousCheckBox);
        Button setConfig = new Button("Set Configuration");
        setConfig.setOnAction(e -> {
            this.close();
        });
        VBox container = new VBox(title, iterationPane, updatePane, continuousPane, setConfig);
        container.setAlignment(Pos.CENTER);
        Scene configScene = new Scene(container);
        this.setScene(configScene);
    }
}
