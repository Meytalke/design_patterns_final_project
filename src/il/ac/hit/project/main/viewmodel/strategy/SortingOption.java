package il.ac.hit.project.main.viewmodel.strategy;

/**
 * Enumerates the available sorting options exposed to the UI.
 * Each option provides a human-readable display name.
 */
public enum SortingOption {

    /** Sort by the task's creation date (oldest to newest). */
    ID("Id"),

    /** Sort by the task's priority. */
    STATE("State"),

    /** Sort by the task's title (lexicographical). */
    TITLE("Title"),;

    private final String displayName;

    SortingOption(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns a human-readable label suitable for UI presentation.
     *
     * @return the display name of this sorting option
     */
    public String getDisplayName() {
        return displayName;
    }
}
