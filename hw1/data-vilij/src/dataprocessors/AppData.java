package dataprocessors;

import actions.AppActions;
import javafx.scene.control.ComboBox;
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

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.LOAD_ERROR_TITLE;
import static vilij.settings.PropertyTypes.SAVE_ERROR_TITLE;

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

    private int instances;
    private int labels;
    private String labelNames;
    private boolean nullInData;
    private boolean dataIsValid;
    private boolean amChangingComboBox;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
        manager = applicationTemplate.manager;
        loadedData = "";
        amChangingComboBox = true;
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
    public static class InvalidFormatException extends Exception {
        private static final String DATA_ERROR_MSG = "This line is malformatted";
        InvalidFormatException() {
            super(DATA_ERROR_MSG );
        }
    }

    public boolean isDataIsValid(){ return dataIsValid; }

    @Override
    public void loadData(Path dataFilePath) {
        try{
            clear();
            BufferedReader reader = new BufferedReader(new FileReader(dataFilePath.toString()));
            StringBuilder buffer;
            buffer = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line).append("\n");
            }
            loadedData = buffer.toString();
            TextArea textArea = ((AppUI)applicationTemplate.getUIComponent()).getTextArea();
            transferLines(loadedData);
            if(getLineCount(loadedData) > 10)
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(TOO_MUCH_DATA.name()), manager.getPropertyValue(MANY_LINES_1.name())+(getLineCount(loadedData)-1)+manager.getPropertyValue(MANY_LINES_2.name()));
            textArea.setDisable(true);
            textArea.setVisible(true);
            loadData(loadedData);
            ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
        }
        catch (Exception e){
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(LOAD_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
        }
    }

    public void loadData(String dataString) {
        try {
            dataIsValid = false;
            ((AppUI)applicationTemplate.getUIComponent()).enableScreenshotButton(true);
            checkString(dataString);
            processor.processString(dataString);
            displayData();
            showMetaData();
            AppUI appUI = (AppUI)applicationTemplate.getUIComponent();
            appUI.showMetaLabel();
            appUI.showComboBox();
        }
        catch (Exception e) {
            if(((AppUI)applicationTemplate.getUIComponent()).getTextArea().isDisabled()) ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setVisible(false);
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(LOAD_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        try{
            String data = ((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText();
            checkString(data);
            PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath));
            data = data.replaceAll("\n", System.lineSeparator());
            writer.write(data);
            writer.close();
            ((AppUI)applicationTemplate.getUIComponent()).enableSaveButton(false);
        }
        catch (Exception e) {
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
        }
    }

    @Override
    public void clear() {
        if(!loadedData.equals(""))
            loadedData = "";
        processor.clear();
        applicationTemplate.getUIComponent().clear();
        clearMetaData();
    }

    private void displayData() {
        processor.toChartData(((AppUI)applicationTemplate.getUIComponent()).getChart());
    }

    private void showMetaData(){
        AppUI appUI = ((AppUI)applicationTemplate.getUIComponent());
        Path dataFilePath = ((AppActions)applicationTemplate.getActionComponent()).getDataPath();
        String path = (dataFilePath == null)? "the user" : dataFilePath.toString();
        appUI.getMetaLabel().setText("There are "+instances+" instances with "+labels+" labels loaded from "+path+". The labels are: \n"+labelNames);
        ComboBox<String> comboBox = appUI.getComboBox();
        amChangingComboBox = true;
        comboBox.getItems().clear();
        comboBox.setPromptText("Choose an algorithm.");
        comboBox.getItems().add("Clustering");
        if(!nullInData && labels == 2)
            comboBox.getItems().add("Classification");
        comboBox.setOnAction(e -> {
            if(!amChangingComboBox) {
                appUI.hideComboBox();
                switch (comboBox.getValue()) {
                    case "Classification":
                        appUI.showClassification();
                        return;
                    default:
                        appUI.showClustering();
                }
            }
        });
        comboBox.setVisible(true);
        amChangingComboBox = false;
    }

    private void clearMetaData(){
        AppUI appUI = ((AppUI)applicationTemplate.getUIComponent());
        appUI.getComboBox().setVisible(false);
        appUI.getMetaLabel().setText("");
    }

    private void transferLines(String data){
        int lines = 10;
        while(!data.equals("") && lines > 0){
            lines--;
            TextArea displayedTextArea = ((AppUI)applicationTemplate.getUIComponent()).getTextArea();
            displayedTextArea.setText(displayedTextArea.getText()+data.substring(0, data.indexOf("\n")+1));
            data = data.substring(data.indexOf("\n")+1);
        }
    }

    private int getLineCount(String text) {
        int lines = 0;
        for(int i = 0; i<text.length(); i++){
            if(text.substring(i, i+1).equals("\n"))
                lines++;
        }
        return lines;
    }

    //this method will give the error line by parsing the string for the error
    public void checkString(String dataString) throws IOException{
        nullInData = false;
        instances = 0;
        labels = 0;
        labelNames = "";
        StringBuilder ln = new StringBuilder();
        ArrayList<String[]> dataArray = new ArrayList<>();
        String[] lines = dataString.split("\n");
        for(String line: lines){
            dataArray.add(line.split("\t"));
        }
        ArrayList<String> instanceList = new ArrayList<>();
        ArrayList<String> labelList = new ArrayList<>();
        for(int i = 0; i<dataArray.size(); i++){
            try {
                String[] line = dataArray.get(i);
                if(line.length != 3)
                    throw new InvalidFormatException();
                instanceList.add(checkDuplicate(checkName(line[0]), instanceList));
                instances++;
                if(!labelList.contains(line[1])) {
                    labelList.add(line[1]);
                    if(line[1].equals("null"))
                        nullInData = true;
                    labels++;
                    ln.append("- ").append(line[1]).append("\n");
                }
                checkPoints(line[2]);
            }
            catch(Exception e){
                throw new IOException(manager.getPropertyValue(ERROR_THIS_LINE.name())+(i+1)+": "+e.getMessage()+".");
            }
        }
        dataIsValid = true;
        labelNames = ln.toString();
    }

    private String checkName(String name) throws TSDProcessor.InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new TSDProcessor.InvalidDataNameException(name);
        return name;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
