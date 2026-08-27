package keef;

import java.time.LocalDate;
import java.util.List;

import keef.command.Command;
import keef.command.Parser;
import keef.exception.KeefException;
import keef.storage.Storage;
import keef.task.Deadline;
import keef.task.Event;
import keef.task.Task;
import keef.task.TaskList;
import keef.task.Todo;
import keef.ui.Ui;

/**
 * Main entry point and coordinator for the Keef task-list application.
 */
public class Keef {
    private final Storage storage;
    private TaskList tasks;
    private final Ui ui;

    /**
     * Creates a Keef application wired to the given storage file path.
     *
     * @param filePath storage path, relative or absolute
     */
    public Keef(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            tasks = new TaskList(storage.load());
        } catch (KeefException e) {
            ui.showLoadingError(e.getMessage());
            tasks = new TaskList();
        }
    }

    /**
     * Runs the interactive command loop.
     */
    public void run() {
        ui.showWelcome();
        while (ui.hasNextCommand()) {
            String fullCommand = ui.readCommand();
            ui.showLine();
            try {
                Parser.ParsedCommand parsedCommand = Parser.parse(fullCommand);
                if (parsedCommand.getCommand() == Command.BYE) {
                    ui.showGoodbye();
                    break;
                }
                execute(parsedCommand);
            } catch (KeefException e) {
                ui.showError(e.getUserMessage());
            } finally {
                ui.showLine();
            }
        }
    }

    private void execute(Parser.ParsedCommand parsedCommand) throws KeefException {
        switch (parsedCommand.getCommand()) {
        case TODO -> addTodo(parsedCommand.getArguments());
        case DEADLINE -> addDeadline(parsedCommand.getArguments());
        case EVENT -> addEvent(parsedCommand.getArguments());
        case LIST -> ui.showTasks(tasks);
        case ONDATE -> showTasksOnDate(parsedCommand.getArguments());
        case MARK -> markTask(parsedCommand.getArguments());
        case UNMARK -> unmarkTask(parsedCommand.getArguments());
        case DELETE -> deleteTask(parsedCommand.getArguments());
        case FIND -> findTasks(parsedCommand.getArguments());
        case BYE -> throw new IllegalStateException("The bye command is handled before dispatch.");
        }
    }

    private void addTodo(String arguments) throws KeefException {
        String description = Parser.parseTodoDescription(arguments);
        Task task = new Todo(description);
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    private void addDeadline(String arguments) throws KeefException {
        Parser.DeadlineDetails details = Parser.parseDeadlineDetails(arguments);
        Task task = new Deadline(details.getDescription(), details.getBy());
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    private void addEvent(String arguments) throws KeefException {
        Parser.EventDetails details = Parser.parseEventDetails(arguments);
        Task task = new Event(details.getDescription(), details.getFrom(), details.getTo());
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    private void showTasksOnDate(String arguments) throws KeefException {
        LocalDate targetDate = Parser.parseOnDate(arguments);
        List<Task> matchingTasks = tasks.findTasksOnDate(targetDate);
        ui.showTasksOnDate(targetDate, matchingTasks);
    }

    private void markTask(String arguments) throws KeefException {
        int taskNumber = Parser.parseTaskNumber(arguments, tasks.size(), "mark");
        Task task = tasks.get(taskNumber - 1);
        task.markAsDone();
        storage.save(tasks);
        ui.showTaskMarkedDone(task);
    }

    private void unmarkTask(String arguments) throws KeefException {
        int taskNumber = Parser.parseTaskNumber(arguments, tasks.size(), "unmark");
        Task task = tasks.get(taskNumber - 1);
        task.markAsNotDone();
        storage.save(tasks);
        ui.showTaskMarkedNotDone(task);
    }

    private void deleteTask(String arguments) throws KeefException {
        int taskNumber = Parser.parseTaskNumber(arguments, tasks.size(), "delete");
        Task removedTask = tasks.remove(taskNumber - 1);
        storage.save(tasks);
        ui.showTaskDeleted(removedTask, tasks.size());
    }

    private void findTasks(String arguments) throws KeefException {
        String keyword = Parser.parseFindKeyword(arguments);
        List<Task> matchingTasks = tasks.find(keyword);
        ui.showMatchingTasks(matchingTasks);
    }

    public static void main(String[] args) {
        new Keef("data\\keef.txt").run();
    }
}
