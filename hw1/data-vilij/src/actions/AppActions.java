package actions;

import components.YesNoDialog;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;
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

    private static final String SEPARATOR = "/";

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        manager = applicationTemplate.manager;
        dataFilePath = null;
    }

    @Override
    public void handleNewRequest() {
        clearAll();
        AppUI appUI = (AppUI)applicationTemplate.getUIComponent();
        appUI.getTextArea().setVisible(true);
        appUI.getTextArea().setDisable(false);
        appUI.showToggleButton();

    }

    @Override
    public void handleSaveRequest() {
        try {
            if(dataFilePath != null)
                applicationTemplate.getDataComponent().saveData(dataFilePath);
            else
                saveFile();
        } catch (IOException e) {
            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
        }
    }

    @Override
    public void handleLoadRequest() {
        String dataResourcePath = SEPARATOR+manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        URL dataResourceURL = getClass().getResource(dataResourcePath);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(manager.getPropertyValue(LOAD.name()));
        fileChooser.setInitialDirectory(new File(dataResourceURL.getFile()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(ASTERISK_CHARACTER.name()) + manager.getPropertyValue(DATA_FILE_EXT.name())));
        File loaded = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(loaded != null){
            dataFilePath = loaded.toPath();
            applicationTemplate.getDataComponent().loadData(dataFilePath);
            ((AppUI)applicationTemplate.getUIComponent()).enableSaveButton(false);
        }
    }

    @Override
    public void handleExitRequest() {
        //if no alg is running and data doesnt need saving just exit
        if(!((AppUI)applicationTemplate.getUIComponent()).isAlgorithmRunning() && ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().isDisabled())
            Platform.exit();
        //if algorithm is running
        if(((AppUI)applicationTemplate.getUIComponent()).isAlgorithmRunning()){
            Dialog ynd = YesNoDialog.getDialog();
            ynd.show(manager.getPropertyValue(ALGO_RUNNING.name()), manager.getPropertyValue(EXIT_WHILE_RUNNING_WARNING.name()));
            YesNoDialog.Option response = ((YesNoDialog)ynd).getSelectedOption();
            if(response.equals(YesNoDialog.Option.YES))
                System.exit(0);
        }
        //if data is unsaved
        if(!((AppUI)applicationTemplate.getUIComponent()).getSaveButton().isDisabled()){
            Dialog cd = applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
            ((ConfirmationDialog)cd).setWidth(applicationTemplate.getUIComponent().getPrimaryWindow().getWidth()*((double)5/12));
            cd.show(manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
            ConfirmationDialog.Option response = ((ConfirmationDialog)cd).getSelectedOption();
            if(response != null && !response.equals(ConfirmationDialog.Option.CANCEL)){
                if(response.equals(ConfirmationDialog.Option.YES)){
                    try {
                        saveFile();
                        System.exit(0);
                    } catch (IOException e) {
                        applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(manager.getPropertyValue(SAVE_ERROR_TITLE.name()), manager.getPropertyValue(DATA_FORMAT_ERROR_2.name())+"\n"+e.getMessage());
                    }
                }
                else
                    System.exit(0);
            }
        }

    }

    @Override
    public void handlePrintRequest() {}

    public void handleScreenshotRequest() throws IOException {
        WritableImage image = ((AppUI)applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(), null);
        String dataResourcePath = SEPARATOR+manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        URL dataResourceURL = getClass().getResource(dataResourcePath);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
        fileChooser.setInitialDirectory(new File(dataResourceURL.getFile()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(PNG.name()), manager.getPropertyValue(ASTERISK_CHARACTER.name()) + ".png"));
        File snapshot = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(snapshot != null)
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), manager.getPropertyValue(PNG_EXT.name()), snapshot);
    }

    private void clearAll(){
        applicationTemplate.getUIComponent().clear();
        applicationTemplate.getDataComponent().clear();
        ((AppUI)applicationTemplate.getUIComponent()).getChart().getData().clear();
        dataFilePath = null;
    }

    private void saveFile() throws IOException{
        String dataResourcePath = SEPARATOR+manager.getPropertyValue(DATA_RESOURCE_PATH.name());
        URL dataResourceURL = getClass().getResource(dataResourcePath);
        try{
            ((AppData)applicationTemplate.getDataComponent()).checkString(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());
        }
        catch (IOException e){
            throw new IOException(e.getMessage());
        }
        if(dataFilePath == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));
            fileChooser.setInitialDirectory(new File(dataResourceURL.getFile()));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(manager.getPropertyValue(DATA_FILE_EXT_DESC.name()), manager.getPropertyValue(ASTERISK_CHARACTER.name()) + manager.getPropertyValue(DATA_FILE_EXT.name())));
            File saved = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (saved != null) {
                dataFilePath = saved.toPath();
                applicationTemplate.getDataComponent().saveData(dataFilePath);
                ((AppUI)applicationTemplate.getUIComponent()).enableSaveButton(false);
            }
        }
    }

    public Path getDataPath() {
        return dataFilePath;
    }
}
