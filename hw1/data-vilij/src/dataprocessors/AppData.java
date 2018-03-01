package dataprocessors;

import actions.AppActions;
import javafx.scene.control.TextArea;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static settings.AppPropertyTypes.DATA_FORMAT_ERROR_2;
import static vilij.settings.PropertyTypes.LOAD_ERROR_TITLE;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    private PropertyManager manager;

    private String loadedData;
    private boolean overflow;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
        manager = applicationTemplate.manager;
        overflow = false;
    }

    public static class InvalidDataPairException extends Exception {
        private static final String DATA_ERROR_MSG = "All data instances must contain two double values separated by a comma";
        InvalidDataPairException(String name) {
            super(String.format("Invalid data pair '%s'. " + DATA_ERROR_MSG, name));
        }
    }
    public static class DuplicateException extends Exception {
        private static final String DATA_ERROR_MSG = "There is a duplicate entry of ";
        DuplicateException(String occurence) {
            super(DATA_ERROR_MSG + occurence);
        }
    }

    public boolean isOverflow(){return overflow;}

    @Override
    public void loadData(Path dataFilePath) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(dataFilePath.toString()));
            StringBuilder buffer;
            buffer = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line).append("\n");
            }
            loadedData = buffer.toString();
            checkString(loadedData);
            if(getLineCount(loadedData) > 10){
                transferLines(10);
                loadData(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText()+loadedData);
                overflow = true;
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show("Data Too Numerous", "Loaded data consists of "+(getLineCount(loadedData)+10)+" lines. Showing only the first 10 in the text area.");
            }
            else {
                ((AppUI) applicationTemplate.getUIComponent()).getTextArea().setText(loadedData);
                loadData(loadedData);
            }
        }
        catch (Exception e){
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(LOAD_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
        }
    }

    public void loadData(String dataString) {
        try {
            ((AppUI)applicationTemplate.getUIComponent()).enableScreenshotButton(true);
            checkString(dataString);
            processor.processString(dataString);
            displayData();
        }
        catch (Exception e) {
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(LOAD_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        try{
            String data = ((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText();
            PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath));
            data = data.replaceAll("\n", System.lineSeparator());
            writer.write(data);
            writer.close();
            ((AppUI) applicationTemplate.getUIComponent()).enableSaveButton(false);
            ((AppActions)applicationTemplate.getActionComponent()).setIsSaved(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        processor.clear();
        ((AppUI)applicationTemplate.getUIComponent()).enableScreenshotButton(false);
    }

    private void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }

    public void transferLines(int lines){
        while(!loadedData.equals("") && lines > 0){
            lines--;
            TextArea displayedTextArea = ((AppUI)applicationTemplate.getUIComponent()).getTextArea();
            displayedTextArea.setText(displayedTextArea.getText()+loadedData.substring(0, loadedData.indexOf("\n")+1));
            loadedData = loadedData.substring(loadedData.indexOf("\n")+1);
            System.out.println(displayedTextArea.getText());
            System.out.println("text area above");
            System.out.println(loadedData);
            System.out.println("loaded data above");
        }
    }

    public int getLineCount(String text) {
        int lines = 0;
        for(int i = 0; i<text.length(); i++){
            if(text.substring(i, i+1).equals("\n"))
                lines++;
        }
        return lines;
    }

    //this method will give the error line by parsing the string for the error
    public void checkString(String dataString) throws IOException{
        ArrayList<String[]> dataArray = new ArrayList<>();
        String[] lines = dataString.split("\n");
        for(String line: lines){
            dataArray.add(line.split("\t"));
        }
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for(int i = 0; i<dataArray.size(); i++){
            try {
                String[] line = dataArray.get(i);
                names.add(checkDuplicate(checkName(line[0]), names));
                labels.add(checkDuplicate(line[1], labels));
                checkPoints(line[2]);
            }
            catch(Exception e){
                throw new IOException("Error on line "+(i+1)+": "+e.getMessage()+".");
            }
        }
    }

    private String checkName(String name) throws TSDProcessor.InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new TSDProcessor.InvalidDataNameException(name);
        return name;
    }

    private void checkPoints(String pair) throws InvalidDataPairException{
        try {
            String[] pairArray = pair.split(",");
            Double.parseDouble(pairArray[0]);
            Double.parseDouble(pairArray[1]);
        }
        catch (Exception e){
            throw new InvalidDataPairException(pair);
        }
    }

    private String checkDuplicate(String item, List list) throws DuplicateException{
        if(list.contains(item))
            throw new DuplicateException(item);
        return item;
    }
}
