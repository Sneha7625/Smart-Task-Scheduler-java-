package model;
import java.time.LocalDate;
import java.time.LocalTime;
public class Task implements Comparable<Task>
{
    private String title;
    private int priority;
    private LocalDate deadline;
    private LocalTime time;
    private String notes = "";
    private boolean done = false;

    public Task(String title, int priority, LocalDate deadline, LocalTime time)
    {
        this.title = title;
        this.priority = priority;
        this.deadline = deadline;
        this.time = time;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }
    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }


    public String getTitle()
    {
        return title;
    }
    public int getPriority()
    {
        return priority;
    }
    public LocalDate getDeadline()
    {
        return deadline;
    }
    public LocalTime getTime()
    {
        return time;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
    public void setPriority(int priority)
    {
        this.priority = priority;
    }
    public void setDeadline(LocalDate deadline)
    {
        this.deadline = deadline;
    }
    public void setTime(LocalTime time)
    {
        this.time = time;
    }

    @Override
    public int compareTo(Task other)
    {
        int dateCompare = this.deadline.compareTo(other.deadline);
        return (dateCompare != 0) ? dateCompare : Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString()
    {
        return title + " (Priority: " + switch (priority)
        {
            case 1 -> "High";
            case 2 -> "Medium";
            case 3 -> "Low";
            default -> "Unknown";
        } + ", Due: " + deadline + (time != null ? " " + time : "") + ")";

    }
}