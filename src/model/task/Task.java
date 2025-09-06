package model.task;

import model.report.TaskVisitor;

import java.util.Date;

/**
 * Mutable implementation of {@link ITask} representing a single task item.
 * <p>
 * Stores basic metadata (id, title, description), workflow {@link TaskState},
 * creation timestamp, and {@link TaskPriority}. It also supports the Visitor pattern
 * via {@link #accept(TaskVisitor)} to decouple reporting/export logic from the model.
 *
 * <h3>Nullability and validation</h3>
 * Unless otherwise noted, the constructor and setters are expected to receive non-null
 * values for {@code title}, {@code state}, {@code creationDate}, and {@code priority}.
 * {@code description} may be {@code null}. No validation is performed internally; passing
 * unexpected values may lead to runtime exceptions in other methods.
 */
public class Task implements ITask {

    /**
     * Unique identifier of the task within the persistence context.
     */
    private int id;

    /**
     * Short, human-readable title of the task.
     */
    private String title;

    /**
     * Optional, longer description; may be {@code null}.
     */
    private String description;

    /**
     * Current workflow state; expected to be non-null.
     */
    private TaskState state;

    /**
     * Timestamp of when the task was created; expected to be non-null.
     * Note: stored and exposed without defensive copies.
     */
    private Date creationDate;

    /**
     * Priority classification; expected to be non-null.
     */
    private TaskPriority priority;

    /**
     * Constructs a new task with the provided values.
     * <p>
     * No validation or defensive copying is performed. The provided {@code creationDate}
     * reference is stored directly.
     *
     * @param id            unique identifier
     * @param title         task title (expected non-null)
     * @param description   optional description (maybe {@code null})
     * @param state         workflow state (expected non-null)
     * @param creationDate  creation timestamp (expected non-null; no defensive copy)
     * @param priority      priority classification (expected non-null)
     */
    public Task(int id, String title, String description, TaskState state, Date creationDate, TaskPriority priority) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setState(state);
        setCreationDate(creationDate);
        setPriority(priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {return id;}

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {return title;}

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {return description;}

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskState getState() {return state;}

    /**
     * Accepts a visitor to perform an operation on this task instance.
     *
     * @param visitor the visitor to accept (expected non-null)
     */
    @Override
    public void accept(TaskVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns a human-readable representation containing the id, title, state,
     * creation date, and priority. The exact format is not guaranteed to be stable
     * across versions and is primarily intended for logging/debugging.
     */
    @Override
    public String toString() {
        return "ID: "+ id + " | Title: " + title + " | State: " + state.toString() + " | Creation Date:" + creationDate.toString() + " | priority: " + priority.toString() ;
    }

    /**
     * Returns the creation timestamp.
     * <p>
     * Note: returns the internal {@link Date} reference; callers should not mutate it.
     *
     * @return the creation date (non-null as per usage expectations)
     */
    public Date getCreationDate() {return creationDate;}

    /**
     * Returns the priority classification.
     *
     * @return the priority (non-null as per usage expectations)
     */
    public TaskPriority getPriority() {return priority;}

    /**
     * Sets the unique identifier.
     *
     * @param id the new id
     */
    public void setId(int id) {this.id = id;}

    /**
     * Sets the task title.
     *
     * @param title the title (expected non-null)
     */
    public void setTitle(String title) {this.title = title;}

    /**
     * Sets the optional task description.
     *
     * @param description the description (maybe {@code null})
     */
    public void setDescription(String description) {this.description = description;}

    /**
     * Sets the workflow state.
     *
     * @param state the new state (expected non-null)
     */
    public void setState(TaskState state) {this.state = state;}

    /**
     * Sets the creation timestamp.
     * <p>
     * Note: the reference is stored directly; no defensive copy is made.
     *
     * @param creationDate the creation date (expected non-null)
     */
    public void setCreationDate(Date creationDate) {this.creationDate = creationDate;}

    /**
     * Sets the priority classification.
     *
     * @param priority the priority (expected non-null)
     */
    public void setPriority(TaskPriority priority) {this.priority = priority;}
}