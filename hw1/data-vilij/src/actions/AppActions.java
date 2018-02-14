package actions;

import javafx.stage.FileChooser;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;
import vilij.propertymanager.PropertyManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Platform;

import static settings.AppPropertyTypes.*;
import static vilij.settings.PropertyTypes.*;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;

    private static final String SEPARATOR = "/";

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest() {
        // TODO for homework 1
        // call prompt to save here if it returns true then call this
        try {
            if (promptToSave()) {
                applicationTemplate.getUIComponent().clear();
                applicationTemplate.getDataComponent().clear();
                ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
            }
        }
        catch (IOException e){
            PropertyManager manager = applicationTemplate.manager;
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(SAVE_ERROR_MSG.name())+manager.getPropertyValue(SPECIFIED_FILE.name()+"."));
        }
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        // TODO for homework 1
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
        // TODO for homework 1
        PropertyManager manager = applicationTemplate.manager;
        Dialog cd = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        ((ConfirmationDialog)cd).setWidth(applicationTemplate.getUIComponent().getPrimaryWindow().getWidth()*((double)5/12));
        cd.show(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        ConfirmationDialog.Option response = ((ConfirmationDialog)cd).getSelectedOption();
        String dataFolderpath = manager.getPropertyValue(HW_PATH.name())+SEPARATOR+manager.getPropertyValue(DATA_VILIJ_PATH.name())+SEPARATOR+manager.getPropertyValue(RESOURCE_PATH.name())+SEPARATOR+manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        dataFilePath = Paths.get(dataFolderpath);
        if(response != null && !response.equals(ConfirmationDialog.Option.CANCEL)){
            if(response.equals(ConfirmationDialog.Option.YES)){
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
                fileChooser.setInitialDirectory(new File(dataFilePath.toString()));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(DATA_FILE_EXT.name())));
                File saved = fileChooser.showSaveDialog((ConfirmationDialog)cd);
                if(saved != null){
                    FileWriter fw = new FileWriter(saved);
                    fw.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
                    fw.close();
                    dataFilePath = saved.toPath();
                }
            }
            return !response.equals(ConfirmationDialog.Option.CANCEL);
        }
        return false;
    }
}
