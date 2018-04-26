package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH, CSS_PATH,

    /* string constants */
    ASTERISK_CHARACTER, NULL, CHOOSE_ALGORITHM,
    //DISPLAY,
    DATA_VISUALIZATION, PNG, DONE, CLASSIFICATION, EDIT, RUN_CONFIGURATION,
    RANDOM_CLASSIFIER, CLUSTERING, RANDOM_CLUSTERER,

    /* user interface icon file names */
    SCREENSHOT_ICON, RUN_ICON, COG_ICON, CONTINUE_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* messages */
    DATA_FORMAT_ERROR_2, SCREENSHOT_ERROR_TITLE, SCREENSHOT_ERROR_MSG, TOO_MUCH_DATA, MANY_LINES_1, MANY_LINES_2, ERROR_THIS_LINE, CHOOSE_CONFIGURATION,
    NO_CONFIG, META_1, META_2, META_3, META_4, THE_USER, ALGO_RUNNING,
    MAX_ITER, UPDATE_INTER, CONT, LABEL_NUM, SET_CONFIG,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE, EXIT_WHILE_RUNNING_WARNING,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    LOAD, PNG_EXT,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC
}
