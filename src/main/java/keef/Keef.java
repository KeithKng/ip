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

    /**
     * Dispatches a parsed command to its handler.
     *
     * @param parsedCommand command and arguments to execute
     * @throws KeefException when the command handler reports a user-facing error
     */
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
            default -> throw new IllegalStateException("Unknown command.");
        }
    }

    /**
     * Creates a to-do task from the given arguments, saves it, and reports it to the user.
     *
     * @param arguments text after the todo keyword
     * @throws KeefException when the description is missing
     */
    private void addTodo(String arguments) throws KeefException {
        String description = Parser.parseTodoDescription(arguments);
        Task task = new Todo(description);
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    /**
     * Creates a deadline task from the given arguments, saves it, and reports it to the user.
     *
     * @param arguments text after the deadline keyword
     * @throws KeefException when the description or /by value is missing
     */
    private void addDeadline(String arguments) throws KeefException {
        Parser.DeadlineDetails details = Parser.parseDeadlineDetails(arguments);
        Task task = new Deadline(details.getDescription(), details.getBy());
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    /**
     * Creates an event task from the given arguments, saves it, and reports it to the user.
     *
     * @param arguments text after the event keyword
     * @throws KeefException when required fields are missing or malformed
     */
    private void addEvent(String arguments) throws KeefException {
        Parser.EventDetails details = Parser.parseEventDetails(arguments);
        Task task = new Event(details.getDescription(), details.getFrom(), details.getTo());
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    /**
     * Shows tasks that occur on the date given in the arguments.
     *
     * @param arguments text after the ondate keyword
     * @throws KeefException when the date is missing or invalid
     */
    private void showTasksOnDate(String arguments) throws KeefException {
        LocalDate targetDate = Parser.parseOnDate(arguments);
        List<Task> matchingTasks = tasks.findTasksOnDate(targetDate);
        ui.showTasksOnDate(targetDate, matchingTasks);
    }

    /**
     * Marks the task identified in the arguments as done, saves it, and reports it to the user.
     *
     * @param arguments text after the mark keyword
     * @throws KeefException when the task number is missing, malformed, or out of range
     */
    private void markTask(String arguments) throws KeefException {
        int taskNumber = Parser.parseTaskNumber(arguments, tasks.size(), "mark");
        Task task = tasks.get(taskNumber - 1);
        task.markAsDone();
        storage.save(tasks);
        ui.showTaskMarkedDone(task);
    }

    /**
     * Marks the task identified in the arguments as not done, saves it, and reports it to the user.
     *
     * @param arguments text after the unmark keyword
     * @throws KeefException when the task number is missing, malformed, or out of range
     */
    private void unmarkTask(String arguments) throws KeefException {
        int taskNumber = Parser.parseTaskNumber(arguments, tasks.size(), "unmark");
        Task task = tasks.get(taskNumber - 1);
        task.markAsNotDone();
        storage.save(tasks);
        ui.showTaskMarkedNotDone(task);
    }

    /**
     * Removes the task identified in the arguments, saves the list, and reports it to the user.
     *
     * @param arguments text after the delete keyword
     * @throws KeefException when the task number is missing, malformed, or out of range
     */
    private void deleteTask(String arguments) throws KeefException {
        int taskNumber = Parser.parseTaskNumber(arguments, tasks.size(), "delete");
        Task removedTask = tasks.remove(taskNumber - 1);
        storage.save(tasks);
        ui.showTaskDeleted(removedTask, tasks.size());
    }

    /**
     * Finds tasks whose description matches the keyword in the arguments and reports them to the user.
     *
     * @param arguments text after the find keyword
     * @throws KeefException when the keyword is missing
     */
    private void findTasks(String arguments) throws KeefException {
        String keyword = Parser.parseFindKeyword(arguments);
        List<Task> matchingTasks = tasks.find(keyword);
        ui.showMatchingTasks(matchingTasks);
    }

    /**
     * Launches the Keef application using the default storage file.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        new Keef("data\\keef.txt").run();
    }
}
