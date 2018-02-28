package actions;

import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.SAVE_ERROR_TITLE;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    private Path dataFilePath;
    private PropertyManager manager;
    private boolean isSaved;

    private static final String SEPARATOR = "/";

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        manager = applicationTemplate.manager;
        dataFilePath = null;
        isSaved = false;
    }

    public void setIsSaved(boolean b){
        isSaved = b;
    }

    @Override
    public void handleNewRequest() {
        try {
            if (isSaved || promptToSave()) {
                applicationTemplate.getUIComponent().clear();
                applicationTemplate.getDataComponent().clear();
                ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                dataFilePath = null;
            }
        }
        catch (IOException e){
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
        }
    }

    @Override
    public void handleSaveRequest() {
        try {
            if(promptToSave()){
                isSaved = true;
                ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(isSaved);
            }
            if(isSaved)
                applicationTemplate.getDataComponent().saveData(dataFilePath);
        } catch (IOException e) {
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
        }
    }

    @Override
    public void handleLoadRequest() {
        String dataResourcePath = SEPARATOR+manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        URL dataResourceURL = getClass().getResource(dataResourcePath);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load");
        fileChooser.setInitialDirectory(new File(dataResourceURL.getFile()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(ASTERISK_CHARACTER.name()) + manager.getPropertyValue(DATA_FILE_EXT.name())));
        File loaded = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(loaded != null){
            dataFilePath = loaded.toPath();
            applicationTemplate.getDataComponent().loadData(dataFilePath);
        }
    }

    @Override
    public void handleExitRequest() {
        Platform.exit();
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException{
        Dialog cd = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        ((ConfirmationDialog)cd).setWidth(applicationTemplate.getUIComponent().getPrimaryWindow().getWidth()*((double)5/12));
        cd.show(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        ConfirmationDialog.Option response = ((ConfirmationDialog)cd).getSelectedOption();
        String dataResourcePath = SEPARATOR+manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        URL dataResourceURL = getClass().getResource(dataResourcePath);
        if(response != null && !response.equals(ConfirmationDialog.Option.CANCEL)){
            if(response.equals(ConfirmationDialog.Option.YES)){
                try{ ((AppData)applicationTemplate.getDataComponent()).checkString(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());}
                catch (IOException e){ throw new IOException(e.getMessage());}
                if(dataFilePath == null) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
                    fileChooser.setInitialDirectory(new File(dataResourceURL.getFile()));
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(ASTERISK_CHARACTER.name()) + manager.getPropertyValue(DATA_FILE_EXT.name())));
                    File saved = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                    if (saved != null) {
                        dataFilePath = saved.toPath();
                        applicationTemplate.getDataComponent().saveData(dataFilePath);
                    }
                }
            }
            return !response.equals(ConfirmationDialog.Option.CANCEL);
        }
        return false;
    }
}
