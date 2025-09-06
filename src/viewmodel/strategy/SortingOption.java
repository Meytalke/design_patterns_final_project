package viewmodel.strategy;

// This enum represents the different sorting options available in the UI.
public enum SortingOption {
    CREATION_DATE("Creation Date"),
    PRIORITY("Priority"),
    TITLE("Title"),;

    private final String displayName;

    SortingOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
