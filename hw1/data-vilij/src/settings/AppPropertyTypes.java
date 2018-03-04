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
    ASTERISK_CHARACTER, DISPLAY, DATA_VISUALIZATION, READ_ONLY, PNG,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    DATA_FORMAT_ERROR_2, SCREENSHOT_ERROR_TITLE, SCREENSHOT_ERROR_MSG, TOO_MUCH_DATA, MANY_LINES_1, MANY_LINES_2, ERROR_THIS_LINE,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA
}
