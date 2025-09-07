package model.task;

/**
 * Represents a task state with forward/backward transitions.
 */
public interface TaskState {

    /**
     * Human-readable name for UI/logs.
     */
    String getDisplayName();

    /**
     * Next state in the workflow; return this if no forward transition.
     */
    ITaskState next();

    /**
     * Previous state in the workflow; return this if no backward transition.
     */
    ITaskState previous();
}