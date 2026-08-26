import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A small command-line task list backed by a dynamically sized collection.
 */
public class Keef {
    private static final String DIVIDER = "____________________________________________________________";
    private static final Path STORAGE_PATH = findStoragePath();

    private static Path findStoragePath() {
        // Prefer locating the repository root from the code source location so the
        // storage path is stable regardless of the process working directory.
        try {
            Path codeLocation = Paths.get(Keef.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();
            Path p = codeLocation;
            if (Files.isRegularFile(p)) {
                p = p.getParent();
            }
            while (p != null) {
                if (Files.exists(p.resolve(".git"))) {
                    return p.resolve("data").resolve("keef.txt");
                }
                p = p.getParent();
            }
        } catch (Exception e) {
            // fall through to using working directory
        }

        // Fallback: use the current working directory
        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        return cwd.resolve("data").resolve("keef.txt");
    }

   public static void main(String[] args) {
        String banner =
                " _  __         __\n"
                + "| |/ /___  ___ / _|\n"
                + "| ' // _ \\/ _ \\ |_ \n"
                + "| . \\  __/  __/  _|\n"
                + "|_|\\_\\___|\\___|_|\n";
        System.out.println(DIVIDER);
        System.out.println(banner);
        System.out.println("Hello! I'm Keef.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);

        Scanner input = new Scanner(System.in);
        List<Task> tasks = loadTasks();
        while (input.hasNextLine()) {
            String command = input.nextLine();
            Command commandType = Command.fromInput(command);
            System.out.println(DIVIDER);

            if (commandType == Command.BYE) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(DIVIDER);
                break;
            }

            try {
                if (commandType == null && command.trim().isEmpty()) {
                    throw new KeefException("No command was entered.",
                            "Enter a command such as: todo read a book");
                }
                if (commandType == null) {
                    throw new KeefException("I don't recognise that command.",
                            "Use todo, deadline, event, list, mark, unmark, delete, or bye.");
                }

                switch (commandType) {
                case TODO -> addTodo(command, tasks);
                case DEADLINE -> addDeadline(command, tasks);
                case EVENT -> addEvent(command, tasks);
                case LIST -> printTasks(tasks);
                case MARK -> markTask(command, tasks);
                case UNMARK -> unmarkTask(command, tasks);
                case DELETE -> deleteTask(command, tasks);
                case BYE -> throw new IllegalStateException("The bye command is handled before dispatch.");
                }
            } catch (KeefException e) {
                System.out.println(e.getUserMessage());
            }
            System.out.println(DIVIDER);
        }
    }

    /** Adds the description in a todo command as an incomplete to-do task. */
    private static void addTodo(String command, List<Task> tasks) throws KeefException {
        String description = command.substring("todo".length()).trim();
        if (description.isEmpty()) {
            throw new KeefException("A to-do needs a description.", "Enter: todo read a book");
        }
        Task task = new Todo(description);
        tasks.add(task);
        saveTasks(tasks);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /** Adds the description and due text in a deadline command as an incomplete deadline. */
    private static void addDeadline(String command, List<Task> tasks) throws KeefException {
        String details = command.substring("deadline".length()).trim();
        int byMarkerIndex = findMarker(details, "/by");
        if (byMarkerIndex < 0) {
            throw new KeefException("A deadline needs a /by date or time.",
                    "Enter: deadline return book /by Sunday");
        }

        String description = details.substring(0, byMarkerIndex).trim();
        String by = details.substring(byMarkerIndex + "/by".length()).trim();
        if (description.isEmpty()) {
            throw new KeefException("The deadline description is missing.",
                    "Enter: deadline return book /by Sunday");
        }
        if (by.isEmpty()) {
            throw new KeefException("The deadline date or time is missing.",
                    "Add a value after /by, for example: deadline return book /by Sunday");
        }
        Task task = new Deadline(description, by);
        tasks.add(task);
        saveTasks(tasks);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /** Adds the description, start text, and end text in an event command. */
    private static void addEvent(String command, List<Task> tasks) throws KeefException {
        String details = command.substring("event".length()).trim();
        int fromMarkerIndex = findMarker(details, "/from");
        int toMarkerIndex = findMarker(details, "/to");
        if (fromMarkerIndex < 0) {
            throw new KeefException("An event needs a /from start time.",
                    "Enter: event project meeting /from Mon 2pm /to 4pm");
        }
        if (toMarkerIndex < 0) {
            throw new KeefException("An event needs a /to end time.",
                    "Enter: event project meeting /from Mon 2pm /to 4pm");
        }
        if (toMarkerIndex < fromMarkerIndex) {
            throw new KeefException("The /from time must come before the /to time.",
                    "Enter: event project meeting /from Mon 2pm /to 4pm");
        }

        String description = details.substring(0, fromMarkerIndex).trim();
        String from = details.substring(fromMarkerIndex + "/from".length(), toMarkerIndex).trim();
        String to = details.substring(toMarkerIndex + "/to".length()).trim();
        if (description.isEmpty()) {
            throw new KeefException("The event description is missing.",
                    "Enter: event project meeting /from Mon 2pm /to 4pm");
        }
        if (from.isEmpty()) {
            throw new KeefException("The event start time is missing.", "Add a value after /from.");
        }
        if (to.isEmpty()) {
            throw new KeefException("The event end time is missing.", "Add a value after /to.");
        }
        Task task = new Event(description, from, to);
        tasks.add(task);
        saveTasks(tasks);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Prints every task and its completion status.
     */
    private static void printTasks(List<Task> tasks) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Marks the one-based task number in a mark command as complete.
     */
    private static void markTask(String command, List<Task> tasks) throws KeefException {
        int taskNumber = readTaskNumber(command.substring("mark".length()).trim(), tasks.size(), "mark");
        int taskIndex = taskNumber - 1;
        Task task = tasks.get(taskIndex);
        task.markAsDone();
        saveTasks(tasks);
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + task);
    }

    /**
     * Marks the one-based task number in an unmark command as incomplete.
     */
    private static void unmarkTask(String command, List<Task> tasks) throws KeefException {
        int taskNumber = readTaskNumber(command.substring("unmark".length()).trim(), tasks.size(), "unmark");
        int taskIndex = taskNumber - 1;
        Task task = tasks.get(taskIndex);
        task.markAsNotDone();
        saveTasks(tasks);
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + task);
    }

    /**
     * Removes the task specified by the one-based task number in a delete command.
     * The collection closes the gap automatically, so list numbering stays consecutive.
     */
    private static void deleteTask(String command, List<Task> tasks) throws KeefException {
        int taskNumber = readTaskNumber(command.substring("delete".length()).trim(), tasks.size(), "delete");
        int taskIndex = taskNumber - 1;
        Task removedTask = tasks.remove(taskIndex);
        saveTasks(tasks);

        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + removedTask);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Loads tasks from disk in the simple line-based format used by saveTasks.
     * Returns an empty list when the storage file does not exist or cannot be
     * read. Malformed lines are skipped. If the file appears heavily corrupted
     * (many malformed lines), the file is moved to a .corrupt.* backup and an
     * empty list is returned.
     */
    private static List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        try {
            if (!Files.exists(STORAGE_PATH)) {
                return tasks;
            }

            List<String> lines = Files.readAllLines(STORAGE_PATH, StandardCharsets.UTF_8);
            int skipped = 0;
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s*\\|\\s*");
                String type = parts.length > 0 ? parts[0].trim() : "";
                String status = parts.length > 1 ? parts[1].trim() : "0";
                try {
                    switch (type) {
                        case "T": {
                            if (parts.length < 3) {
                                throw new IllegalArgumentException("missing description");
                            }
                            String desc = parts[2].trim();
                            Task t = new Todo(desc);
                            if ("1".equals(status)) {
                                t.markAsDone();
                            }
                            tasks.add(t);
                            break;
                        }
                        case "D": {
                            if (parts.length < 4) {
                                throw new IllegalArgumentException("missing fields");
                            }
                            String desc = parts[2].trim();
                            String by = parts[3].trim();
                            Task t = new Deadline(desc, by);
                            if ("1".equals(status)) {
                                t.markAsDone();
                            }
                            tasks.add(t);
                            break;
                        }
                        case "E": {
                            if (parts.length < 5) {
                                throw new IllegalArgumentException("missing fields");
                            }
                            String desc = parts[2].trim();
                            String from = parts[3].trim();
                            String to = parts[4].trim();
                            Task t = new Event(desc, from, to);
                            if ("1".equals(status)) {
                                t.markAsDone();
                            }
                            tasks.add(t);
                            break;
                        }
                        default:
                            System.err.println("Warning: Unknown task type in storage file: " + type);
                            skipped++;
                    }
                } catch (Exception ex) {
                    skipped++;
                    System.err.println("Warning: Skipping malformed storage line: \"" + line + "\" (" + ex.getMessage() + ")");
                }
            }

            // If a large fraction of lines were malformed, assume the file is corrupted.
            if (lines.size() > 0 && skipped >= Math.max(1, lines.size() / 2)) {
                try {
                    Path backup = STORAGE_PATH.resolveSibling(STORAGE_PATH.getFileName().toString() + ".corrupt." + System.currentTimeMillis());
                    Files.createDirectories(STORAGE_PATH.getParent());
                    Files.move(STORAGE_PATH, backup);
                    System.err.println("Storage file appeared corrupted; moved to " + backup + " and starting with empty task list.");
                    return new ArrayList<>();
                } catch (Exception e) {
                    System.err.println("Warning: Failed to backup corrupted storage file: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Unable to read storage file " + STORAGE_PATH + ": " + e.getMessage());
        } catch (SecurityException se) {
            System.err.println("Warning: No permission to read storage file " + STORAGE_PATH + ": " + se.getMessage());
        }

        return tasks;
    }

    /**
     * Saves all tasks to disk in a simple line-based format.
     * Uses an atomic write (when available) by writing to a temporary file and
     * moving it into place. Failures are reported to stderr but do not crash
     * the application.
     */
    private static void saveTasks(List<Task> tasks) {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(task.toStorageString());
        }

        Path parentPath = STORAGE_PATH.getParent();
        try {
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }
            // Write to a temp file in the same directory to allow an atomic move.
            Path tempFile = parentPath != null
                    ? parentPath.resolve(STORAGE_PATH.getFileName().toString() + ".tmp")
                    : Paths.get(STORAGE_PATH.toString() + ".tmp");
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, STORAGE_PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException amnse) {
                // Best-effort fallback when atomic move is not supported on this FS.
                Files.move(tempFile, STORAGE_PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | SecurityException e) {
            System.err.println("Error: Failed to save tasks to " + STORAGE_PATH + ": " + e.getMessage());
            // Try to clean up any temporary file left behind.
            try {
                Path tempFile = parentPath != null
                        ? parentPath.resolve(STORAGE_PATH.getFileName().toString() + ".tmp")
                        : Paths.get(STORAGE_PATH.toString() + ".tmp");
                if (Files.exists(tempFile)) {
                    Files.delete(tempFile);
                }
            } catch (Exception ignore) {
                // best-effort cleanup only
            }
        }
    }

    /**
     * Finds a command marker that is separated from the surrounding text by whitespace.
     *
     * @return the marker's first character index, or {@code -1} when it is absent or malformed
     */
    private static int findMarker(String details, String marker) {
        int markerIndex = details.indexOf(marker);
        if (markerIndex < 0) {
            return -1;
        }

        int markerEndIndex = markerIndex + marker.length();
        boolean hasWhitespaceBefore = markerIndex == 0 || Character.isWhitespace(details.charAt(markerIndex - 1));
        boolean hasWhitespaceAfter = markerEndIndex == details.length()
                || Character.isWhitespace(details.charAt(markerEndIndex));
        return hasWhitespaceBefore && hasWhitespaceAfter ? markerIndex : -1;
    }

    /**
     * Validates and reads a one-based task number.
     *
     * @return the valid task number
     */
    private static int readTaskNumber(String numberText, int taskCount, String commandName) throws KeefException {
        if (taskCount == 0) {
            throw new KeefException("There are no tasks to " + commandName + ".",
                    "Add a task first, for example: todo read a book");
        }

        if (numberText.isEmpty()) {
            throw new KeefException("A task number is required.", "Enter: " + commandName + " 1");
        }

        int taskNumber = 0;
        for (int i = 0; i < numberText.length(); i++) {
            char character = numberText.charAt(i);
            if (!Character.isDigit(character)) {
                throw new KeefException("The task number must contain digits only.",
                        "Enter: " + commandName + " 1");
            }
            if (taskNumber > (Integer.MAX_VALUE - (character - '0')) / 10) {
                throw new KeefException("That task number is not in the list.",
                        "Enter a number from 1 to " + taskCount + ".");
            }
            taskNumber = taskNumber * 10 + (character - '0');
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new KeefException("That task number is not in the list.",
                    "Enter a number from 1 to " + taskCount + ".");
        }
        return taskNumber;
    }

}
