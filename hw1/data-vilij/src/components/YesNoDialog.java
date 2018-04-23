package components;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.components.Dialog;

import java.util.Arrays;
import java.util.List;

public class YesNoDialog extends Stage implements Dialog {

    public enum Option {

        YES("Yes"), NO("No");

        @SuppressWarnings("unused")
        private String option;

        Option(String option) { this.option = option; }
    }

    private static YesNoDialog dialog;

    private Label confirmationMessage = new Label();
    private Option selectedOption;

    private YesNoDialog() { /* empty constructor */ }

    public static YesNoDialog getDialog() {
        if (dialog == null)
            dialog = new YesNoDialog();
        return dialog;
    }

    private void setConfirmationMessage(String message) {
        this.confirmationMessage.setText(message);
    }

    /**
     * Completely initializes the error dialog to be used.
     *
     * @param owner the window on top of which the error dialog window will be displayed
     */
    @Override
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        List<Button> buttons = Arrays.asList(new Button(Option.YES.name()),
                new Button(Option.NO.name()));

        buttons.forEach(button -> button.setOnAction((ActionEvent event) -> {
            this.selectedOption = Option.valueOf(((Button) event.getSource()).getText());
            this.hide();
        }));

        HBox buttonBox = new HBox(5);
        buttonBox.getChildren().addAll(buttons);

        VBox messagePane = new VBox(confirmationMessage, buttonBox);
        messagePane.setAlignment(Pos.CENTER);
        messagePane.setPadding(new Insets(10, 20, 20, 20));
        messagePane.setSpacing(10);

        this.setScene(new Scene(messagePane));
    }

    /**
     * Loads the specified title and message into the error dialog and then displays the dialog.
     *
     * @param dialogTitle the specified dialog title
     * @param message     the specified message
     */
    @Override
    public void show(String dialogTitle, String message) {
        setTitle(dialogTitle);           // set the title of the dialog
        setConfirmationMessage(message); // set the main error message
        showAndWait();                   // open the dialog and wait for the user to click the close button
    }


    public Option getSelectedOption() { return selectedOption; }
}
