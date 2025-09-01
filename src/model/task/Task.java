package model.task;

import model.report.TaskVisitor;

public class Task implements ITask {
    private int id;
    private String title;
    private String description;
    //Convert TaskState to IState type with concrete instance from ToDoState or InProgressState etc...
    private TaskState state;

    public Task(int id, String title, String description, TaskState state) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setState(state);
    }

    @Override
    public int getId() {return id;}

    @Override
    public String getTitle() {return title;}

    @Override
    public String getDescription() {return description;}

    @Override
    public TaskState getState() {return state;}

    public void setId(int id) {this.id = id;}
    public void setTitle(String title) {this.title = title;}
    public void setDescription(String description) {this.description = description;}
    public void setState(TaskState state) {this.state = state;}

    @Override
    public void accept(TaskVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "id: "+ id + " | title: " + title + " | state: " + state.toString();
    }

}
