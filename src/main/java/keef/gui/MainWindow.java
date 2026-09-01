package keef.gui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import keef.command.Parser;
import keef.exception.KeefException;
import keef.storage.Storage;
import keef.task.Task;
import keef.task.TaskList;

/**
 * Controls the tutorial-style conversation window for Keef.
 */
public class MainWindow extends AnchorPane {
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;

    private final Image userImage = new Image(MainWindow.class.getResourceAsStream("/images/DaUser.png"));
    private final Image keefImage = new Image(MainWindow.class.getResourceAsStream("/images/DaDuke.png"));
    private final Storage storage = new Storage("data\\keef.txt");
    private TaskList tasks;

    /**
     * Initializes the task list and keeps new messages visible.
     */
    @FXML
    public void initialize() {
        tasks = loadTasks();
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Handles commands entered through the tutorial UI.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }
        dialogContainer.getChildren().addAll(DialogBox.getUserDialog(input, userImage),
                DialogBox.getKeefDialog(process(input), keefImage));
        userInput.clear();
    }

    private TaskList loadTasks() {
        try {
            return new TaskList(storage.load());
        } catch (KeefException exception) {
            return new TaskList();
        }
    }

    private String process(String input) {
        try {
            Parser.ParsedCommand parsedCommand = Parser.parse(input);
            return switch (parsedCommand.getCommand()) {
                case TODO -> addTodo(parsedCommand.getArguments());
                case DEADLINE -> addDeadline(parsedCommand.getArguments());
                case EVENT -> addEvent(parsedCommand.getArguments());
                case LIST -> formatTasks("Here are the tasks in your list:", tasks.getAll());
                case ONDATE -> showTasksOnDate(parsedCommand.getArguments());
                case MARK -> markTask(parsedCommand.getArguments());
                case UNMARK -> unmarkTask(parsedCommand.getArguments());
                case DELETE -> deleteTask(parsedCommand.getArguments());
                case FIND -> findTasks(parsedCommand.getArguments());
                case BYE -> closeWindow();
            };
        } catch (KeefException exception) {
            return exception.getUserMessage();
        }
    }

    private String addTodo(String arguments) throws KeefException {
        Task task = new keef.task.Todo(Parser.parseTodoDescription(arguments));
        tasks.add(task);
        storage.save(tasks);
        return taskAdded(task);
    }

    private String addDeadline(String arguments) throws KeefException {
        Parser.DeadlineDetails details = Parser.parseDeadlineDetails(arguments);
        Task task = new keef.task.Deadline(details.getDescription(), details.getBy());
        tasks.add(task);
        storage.save(tasks);
        return taskAdded(task);
    }

    private String addEvent(String arguments) throws KeefException {
        Parser.EventDetails details = Parser.parseEventDetails(arguments);
        Task task = new keef.task.Event(details.getDescription(), details.getFrom(), details.getTo());
        tasks.add(task);
        storage.save(tasks);
        return taskAdded(task);
    }

    private String taskAdded(Task task) {
        return "Got it. I've added this task:\n  " + task
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    private String showTasksOnDate(String arguments) throws KeefException {
        LocalDate date = Parser.parseOnDate(arguments);
        return formatTasks("Here are the tasks on " + date.format(DISPLAY_DATE_FORMAT) + ":",
                tasks.findTasksOnDate(date), "No tasks are scheduled for "
                        + date.format(DISPLAY_DATE_FORMAT) + ".");
    }

    private String markTask(String arguments) throws KeefException {
        Task task = tasks.get(Parser.parseTaskNumber(arguments, tasks.size(), "mark") - 1);
        task.markAsDone();
        storage.save(tasks);
        return "Nice! I've marked this task as done:\n  " + task;
    }

    private String unmarkTask(String arguments) throws KeefException {
        Task task = tasks.get(Parser.parseTaskNumber(arguments, tasks.size(), "unmark") - 1);
        task.markAsNotDone();
        storage.save(tasks);
        return "OK, I've marked this task as not done yet:\n  " + task;
    }

    private String deleteTask(String arguments) throws KeefException {
        Task task = tasks.remove(Parser.parseTaskNumber(arguments, tasks.size(), "delete") - 1);
        storage.save(tasks);
        return "Noted. I've removed this task:\n  " + task
                + "\nNow you have " + tasks.size() + " tasks in the list.";
    }

    private String findTasks(String arguments) throws KeefException {
        return formatTasks("Here are the matching tasks in your list:",
                tasks.find(Parser.parseFindKeyword(arguments)));
    }

    private String formatTasks(String heading, List<Task> taskList) {
        return formatTasks(heading, taskList, "Your task list is empty.");
    }

    private String formatTasks(String heading, List<Task> taskList, String emptyMessage) {
        if (taskList.isEmpty()) {
            return emptyMessage;
        }
        StringBuilder response = new StringBuilder(heading).append('\n');
        for (int index = 0; index < taskList.size(); index++) {
            response.append(index + 1).append('.').append(taskList.get(index)).append('\n');
        }
        return response.toString().trim();
    }

    private String closeWindow() {
        Window window = getScene().getWindow();
        window.hide();
        return "Bye. Hope to see you again soon!";
    }
}
