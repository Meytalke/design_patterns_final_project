package il.ac.hit.project.main.model.task;

import java.util.Objects;

/**
 * Initial state of a task: "To Do".
 * <p>
 * Behavior:
 * - next(): transitions to {@code InProgressState}.
 * - previous(): returns {@code this} (there is no state before "To Do").
 * <p>
 * This class is immutable and thread-safe.
 */
public class ToDoState implements TaskState {

    /**
     * Cached reference to the next state in the workflow.
     * Constructed with {@code this} so the next state can navigate back if needed.
     */
    private final InProgressState inProgressState = new InProgressState(this);

    /**
     * Human-readable name of this state.
     *
     * @return "To Do"
     */
    @Override
    public String getDisplayName() {
        return "To Do";
    }

    /**
     * Advances the task to the next state.
     *
     * @return the corresponding {@code InProgressState} instance
     */
    @Override
    public TaskState next() {
        return getInProgressState();
    }

    @Override
    public String toString() {
        return "ToDoState";
    }

    /**
     * There is no state prior to "To Do".
     *
     * @return {@code this}
     */
    @Override
    public TaskState previous() {
        return this;
    }

    /**
     * Exposes the cached next state instance.
     *
     * @return a non-null {@code InProgressState}
     */
    public TaskState getInProgressState() {
        return inProgressState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDoState that = (ToDoState) o;
        return Objects.equals(getDisplayName(), that.getDisplayName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName());
    }
}