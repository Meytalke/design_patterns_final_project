package il.ac.hit.project.main.model.task;

import java.util.Objects;

/**
 * Intermediate task state: "In Progress".
 * <p>
 * Behavior:
 * - next(): transitions to the completed state.
 * - previous(): transitions back to the provided "To Do" state.
 * <p>
 * Design notes:
 * - Part of a simple workflow state machine (To Do -> In Progress -> Completed).
 * - Instances are immutable and thread-safe: all fields are final and constructed once.
 */
public class InProgressState implements TaskState {

    /**
     * Reference to the previous state in the workflow ("To Do").
     * Supplied by the creator to allow navigating back.
     */
    private final TaskState toDoState;

    /**
     * Cached reference to the next state in the workflow ("Completed").
     * Constructed with a back-reference to this state for reverse navigation.
     */
    private final TaskState completedState = new CompletedState(this);

    /**
     * Creates an "In Progress" state that can navigate back to the given "To Do" state.
     *
     * @param toDoState the state to return to when {@link #previous()} is called; must be non-null
     */
    public InProgressState(TaskState toDoState) {
        this.toDoState = toDoState;
    }

    @Override
    public String toString() {
        return "InProgressState";
    }

    /**
     * Human-readable name of this state.
     *
     * @return "In Progress"
     */
    @Override
    public String getDisplayName() {
        return "In Progress";
    }

    /**
     * Advances the task to the next state in the workflow.
     *
     * @return the cached completed state instance
     */
    @Override
    public TaskState next() {return getCompletedState();}

    /**
     * Moves the task back to the previous state in the workflow.
     *
     * @return the associated "To Do" state instance
     */
    @Override
    public TaskState previous() {return getToDoState();}

    /**
     * Exposes the previous state reference ("To Do").
     *
     * @return a non-null {@link TaskState} representing the "To Do" state
     */
    public TaskState getToDoState() {return toDoState;}

    /**
     * Exposes the next state reference ("Completed").
     *
     * @return a non-null {@link TaskState} representing the "Completed" state
     */
    public TaskState getCompletedState() {return completedState;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InProgressState that = (InProgressState) o;
        return Objects.equals(getDisplayName(), that.getDisplayName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName());
    }
}