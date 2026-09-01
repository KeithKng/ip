package keef.gui;

import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import keef.command.Command;
import keef.command.Parser;
import keef.exception.KeefException;
import keef.storage.Storage;
import keef.task.Task;
import keef.task.TaskList;

/**
 * Provides a JavaFX interface for entering Keef commands and viewing responses.
 */
public class KeefGui extends Application {
    private final Storage storage = new Storage("data\\keef.txt");
    private TaskList tasks;
    private TextArea conversation;

    /**
     * Creates the application window and its command controls.
     *
     * @param stage primary JavaFX stage.
     */
    @Override
    public void start(Stage stage) {
        tasks = loadTasks();
        conversation = new TextArea("Hello! I'm Keef.\nType a command below.\n");
        conversation.setEditable(false);
        conversation.setWrapText(true);

        TextField commandInput = new TextField();
        commandInput.setPromptText("Enter a command, e.g. list");
        Button sendButton = new Button("Send");
        sendButton.setOnAction(event -> submit(commandInput));
        commandInput.setOnAction(event -> submit(commandInput));

        HBox inputBar = new HBox(8, new Label("Command:"), commandInput, sendButton);
        inputBar.setPadding(new Insets(10));
        BorderPane root = new BorderPane(conversation, null, null, inputBar, null);
        root.setPadding(new Insets(10));
        stage.setTitle("Keef");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }

    private TaskList loadTasks() {
        try {
            return new TaskList(storage.load().toArray(Task[]::new));
        } catch (KeefException exception) {
            return new TaskList();
        }
    }

    private void submit(TextField commandInput) {
        String input = commandInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }
        conversation.appendText("\n> " + input + "\n" + process(input) + "\n");
        commandInput.clear();
    }

    private String process(String input) {
        try {
            Parser.ParsedCommand parsed = Parser.parse(input);
            if (parsed.getCommand() == Command.LIST) {
                return formatTasks(tasks.getAll());
            }
            return "The GUI currently supports viewing tasks; use the CLI for task changes.\n"
                    + "Try: list or bye";
        } catch (KeefException exception) {
            return exception.getUserMessage();
        }
    }

    private String formatTasks(List<Task> taskList) {
        if (taskList.isEmpty()) {
            return "Your task list is empty.";
        }
        StringBuilder response = new StringBuilder("Here are the tasks in your list:\n");
        for (int index = 0; index < taskList.size(); index++) {
            response.append(index + 1).append('.').append(taskList.get(index)).append('\n');
        }
        return response.toString().trim();
    }
}
