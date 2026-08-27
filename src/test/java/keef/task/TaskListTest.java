package keef.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests task-list mutation and date filtering behaviour.
 */
class TaskListTest {

    @Test
    void addGetRemoveAndSize_tasksManagedInInsertionOrder() {
        Task first = new Todo("first");
        Task second = new Todo("second");
        TaskList taskList = new TaskList();

        taskList.add(first);
        taskList.add(second);

        assertEquals(2, taskList.size());
        assertSame(first, taskList.get(0));
        assertSame(first, taskList.remove(0));
        assertEquals(1, taskList.size());
        assertSame(second, taskList.get(0));
    }

    @Test
    void constructorAndGetAll_inputCopiedAndReturnedListIsUnmodifiable() {
        Task first = new Todo("first");
        List<Task> initialTasks = new java.util.ArrayList<>(List.of(first));
        TaskList taskList = new TaskList(initialTasks);
        initialTasks.clear();

        assertEquals(List.of(first), taskList.getAll());
        assertThrows(UnsupportedOperationException.class, () -> taskList.getAll().add(new Todo("second")));
    }

    @Test
    void findTasksOnDate_matchingDeadlineAndEvent_returnedInListOrder() {
        Deadline deadline = new Deadline("submit report", "2026-09-01 17:00");
        Event event = new Event("conference", "2026-08-31", "2026-09-02");
        TaskList taskList = new TaskList(List.of(new Todo("unrelated"), deadline, event));

        assertEquals(List.of(deadline, event), taskList.findTasksOnDate(LocalDate.of(2026, 9, 1)));
    }

    @Test
    void findTasksOnDate_eventBoundaryDates_eventReturned() {
        Event event = new Event("conference", "2026-09-01", "2026-09-03");
        TaskList taskList = new TaskList(List.of(event));

        assertEquals(List.of(event), taskList.findTasksOnDate(LocalDate.of(2026, 9, 1)));
        assertEquals(List.of(event), taskList.findTasksOnDate(LocalDate.of(2026, 9, 3)));
    }

    @Test
    void findTasksOnDate_unparseableDatesOrNoMatches_emptyListReturned() {
        TaskList taskList = new TaskList(List.of(
                new Deadline("freeform deadline", "next Tuesday"),
                new Event("freeform event", "morning", "afternoon")));

        assertEquals(List.of(), taskList.findTasksOnDate(LocalDate.of(2026, 9, 1)));
    }

    @Test
    void find_caseInsensitiveMatch_returnedInListOrder() {
        Task first = new Todo("read book");
        Task second = new Todo("buy bread");
        Task third = new Deadline("return book", "2026-06-06");
        TaskList taskList = new TaskList(List.of(first, second, third));

        assertEquals(List.of(first, third), taskList.find("BOOK"));
    }

    @Test
    void find_noMatch_emptyListReturned() {
        TaskList taskList = new TaskList(List.of(new Todo("read book")));

        assertEquals(List.of(), taskList.find("laptop"));
    }
}
