/**
 * Represents a task and whether it has been completed.
 */
public class Task {
    protected final String description;
    private final String deadline;
    private boolean isDone;

    /**
     * Creates an incomplete task with the given description.
     *
     * @param description text describing the task
     */
    public Task(String description) {
        this.description = description;
        this.deadline = null;
        this.isDone = false;
    }

    /**
     * Creates an incomplete deadline task with the given description and due text.
     * This temporary representation will be replaced by a Deadline subclass in the
     * inheritance extension.
     *
     * @param description text describing the task
     * @param deadline date or time by which the task must be completed
     */
    public Task(String description, String deadline) {
        this.description = description;
        this.deadline = deadline;
        this.isDone = false;
    }

    /**
     * Marks this task as complete.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as incomplete.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns the status character used in a task-list display.
     *
     * @return {@code X} when complete, otherwise a space
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Returns this task in the format displayed by the command-line application.
     *
     * @return the task type marker, status marker, description, and deadline when present
     */
    @Override
    public String toString() {
        if (deadline == null) {
            return "[T][" + getStatusIcon() + "] " + description;
        }
        return "[D][" + getStatusIcon() + "] " + description + " (by: " + deadline + ")";
    }
}
