package dataprocessors;

import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Path;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        // TODO: NOT A PART OF HW 1
    }

    public void loadData(String dataString) {
        // TODO for homework 1
        try {
            processor.processString(dataString);
            displayData();
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            System.out.println(errorMessage);
            if(errorMessage.contains("@"))
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show("Invalid Data Format", "The lines of your data should start with @.");
            else
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show("Invalid Data Format", "The format of your data should consist of @x where x represents a unique tag, a unique label name, and your x and y coordinates all separated by tabs.");
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
}
