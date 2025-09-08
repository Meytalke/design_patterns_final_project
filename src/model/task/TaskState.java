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
    TaskState next();

    /**
     * Previous state in the workflow; return this if no backward transition.
     */
    TaskState previous();

    /**
     * Compares this TaskState to another object for equality.
     * Implementations should ensure this is based on a unique identifier
     * for the state (e.g., a type or name).
     */
    @Override
    boolean equals(Object o);

    /**
     * Returns a hash code value for the object, consistent with equals().
     */
    @Override
    int hashCode();
}