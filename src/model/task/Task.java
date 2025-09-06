package model.task;

import model.report.TaskVisitor;

import java.util.Date;

public class Task implements ITask {
    private int id;
    private String title;
    private String description;
    private ITaskState state;
    private Date creationDate;
    private TaskPriority priority;

    public Task(int id, String title, String description, ITaskState state, Date creationDate, TaskPriority priority) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setState(state);
        setCreationDate(creationDate);
        setPriority(priority);
    }


    @Override
    public int getId() {return id;}

    @Override
    public String getTitle() {return title;}

    @Override
    public String getDescription() {return description;}

    @Override
    public ITaskState getState() {return state;}

    public Date getCreationDate() {return creationDate;}

    public TaskPriority getPriority() {return priority;}

    public void setId(int id) {this.id = id;}

    public void setTitle(String title) {this.title = title;}

    public void setDescription(String description) {this.description = description;}
    public void setState(ITaskState state) {this.state = state;}

    public void setCreationDate(Date creationDate) {this.creationDate = creationDate;}

    public void setPriority(TaskPriority priority) {this.priority = priority;}

    @Override
    public void accept(TaskVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "id: "+ id + " | title: " + title + " | state: " + state.toString() + " | creation date:" + creationDate.toString() + " | priority: " + priority.toString() ;
    }

}
