package dataprocessors;

import ui.AppUI;
import vilij.components.DataComponent;
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
            ErrorDialog.getDialog().setWidth(applicationTemplate.getUIComponent().getPrimaryWindow().getWidth()*((double)1/3));
            ErrorDialog.getDialog().setHeight(applicationTemplate.getUIComponent().getPrimaryWindow().getWidth()*((double)1/5));
            String errorMessage = e.getMessage();
            System.out.println(errorMessage);
            if(errorMessage.contains("@"))
                ErrorDialog.getDialog().show("Invalid Format", "data names start with @");
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
