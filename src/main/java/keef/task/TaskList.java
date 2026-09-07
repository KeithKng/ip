package keef.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Wraps the mutable list of tasks and exposes task-list operations.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list from the supplied tasks.
     *
     * @param tasks tasks to copy into this list
     */
    public TaskList(Task... tasks) {
        assert tasks != null : "TaskList should always be built from a task array.";
        this.tasks = new ArrayList<>(Arrays.asList(tasks));
        assert !this.tasks.contains(null) : "TaskList must not contain null tasks.";
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task task to add
     */
    public void add(Task task) {
        assert task != null : "TaskList only stores real tasks.";
        tasks.add(task);
    }

    /**
     * Removes and returns a task at the given zero-based index.
     *
     * @param index index of the task to remove
     * @return removed task
     */
    public Task remove(int index) {
        assert index >= 0 && index < tasks.size() : "Task removal index should already be validated.";
        return tasks.remove(index);
    }

    /**
     * Returns a task at the given zero-based index.
     *
     * @param index index of task
     * @return task at index
     */
    public Task get(int index) {
        assert index >= 0 && index < tasks.size() : "Task lookup index should already be validated.";
        return tasks.get(index);
    }

    /**
     * Returns the number of tasks currently in the list.
     *
     * @return task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns a read-only view of all tasks.
     *
     * @return unmodifiable list of tasks
     */
    public List<Task> getAll() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Finds tasks that occur on the specified date.
     *
     * @param targetDate target date
     * @return matching tasks in current list order
     */
    public List<Task> findTasksOnDate(LocalDate targetDate) {
        assert targetDate != null : "Date filtering requires a target date.";
        List<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task instanceof Deadline deadline) {
                LocalDate deadlineDate = deadline.getByDate();
                if (deadlineDate != null && deadlineDate.equals(targetDate)) {
                    matches.add(task);
                }
            } else if (task instanceof Event event && event.occursOn(targetDate)) {
                matches.add(task);
            }
        }
        return matches;
    }

    /**
     * Finds tasks whose description contains the given keyword, ignoring case.
     *
     * @param keyword keyword to search for
     * @return matching tasks in current list order
     */
    public List<Task> find(String keyword) {
        assert keyword != null : "Keyword search requires a keyword.";
        List<Task> matches = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        for (Task task : tasks) {
            if (task.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword)) {
                matches.add(task);
            }
        }
        return matches;
    }
}
