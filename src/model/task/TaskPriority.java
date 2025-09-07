package model.task;

/**
 * Represents the priority level of a task.
 * <p>
 * Each priority provides a human-friendly display name for use in UIs, logs,
 * and reports while keeping the enum values concise and stable for code usage.
 */
public enum TaskPriority {

    /**
     * Lowest urgency; tasks can be scheduled after higher-priority work.
     */
    LOW("Low"),

    /**
     * Default urgency; tasks should be addressed in a reasonable timeframe.
     */
    MEDIUM("Medium"),

    /**
     * Highest urgency; tasks should be prioritized before others.
     */
    HIGH("High");

    /**
     * Human-readable label for this priority.
     */
    private final String displayName;

    /**
     * Constructs a priority value with its display label.
     *
     * @param displayName the user-facing name for this priority (e.g., "High")
     */
    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns a user-facing name suitable for UI and logs.
     *
     * @return the human-readable label of this priority
     */
    public String getDisplayName() {
        return displayName;
    }
}