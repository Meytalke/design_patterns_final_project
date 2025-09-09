package il.ac.hit.project.main.model.task;

import java.util.Objects;

/**
 * Terminal task state: "Completed".
 * <p>
 * Behavior:
 * - next(): returns this instance, since there is no forward transition beyond "Completed".
 * - previous(): returns the associated "In Progress" state to allow reverting if needed.
 * <p>
 * Design notes:
 * - Participates in a simple workflow (To Do -> In Progress -> Completed).
 * - Immutable and thread-safe: the single field is final and set via the constructor.
 */
public class CompletedState implements TaskState {

    /**
     * Reference to the prior state in the workflow ("In Progress").
     * Used to navigate back when {@link #previous()} is called.
     */
    private final TaskState inProgressState;

    /**
     * Creates a "Completed" state that can navigate back to the given "In Progress" state.
     *
     * @param inProgressState the state to return to on {@link #previous()}; expected to be non-null
     */
    public CompletedState(TaskState inProgressState) {
        this.inProgressState = inProgressState;
    }

    /**
     * Human-readable name of this state.
     *
     * @return "Completed"
     */
    @Override
    public String getDisplayName() {
        return "Completed";
    }

    /**
     * Attempts to move forward in the workflow from "Completed".
     * Since this is a terminal state, the instance returns itself.
     *
     * @return this same {@code CompletedState} instance
     */
    @Override
    public TaskState next() {return this;}

    /**
     * Moves the task back to the "In Progress" state.
     *
     * @return the associated "In Progress" state instance
     */
    @Override
    public TaskState previous() {return getInProgressState();}

    /**
     * Exposes the prior state reference ("In Progress").
     *
     * @return a non-null {@link TaskState} representing the "In Progress" state
     */
    public TaskState getInProgressState() {return  inProgressState;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompletedState that = (CompletedState) o;
        return Objects.equals(getDisplayName(), that.getDisplayName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName());
    }
}