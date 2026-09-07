package keef.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        this.tasks = new ArrayList<>(Arrays.asList(tasks));
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns a task at the given zero-based index.
     *
     * @param index index of the task to remove
     * @return removed task
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns a task at the given zero-based index.
     *
     * @param index index of task
     * @return task at index
     */
    public Task get(int index) {
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
        return tasks.stream()
                .filter(task -> matchesDate(task, targetDate))
                .toList();
    }

    /**
     * Finds tasks whose description contains the given keyword, ignoring case.
     *
     * @param keyword keyword to search for
     * @return matching tasks in current list order
     */
    public List<Task> find(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return tasks.stream()
                .filter(task -> task.getDescription().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    private static boolean matchesDate(Task task, LocalDate targetDate) {
        if (task instanceof Deadline deadline) {
            LocalDate dueDate = deadline.getByDate();
            return dueDate != null && dueDate.equals(targetDate);
        }
        return task instanceof Event event && event.occursOn(targetDate);
    }
}
