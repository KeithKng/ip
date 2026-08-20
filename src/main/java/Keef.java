import java.util.Scanner;

/**
 * A small command-line task list that stores up to {@value #MAX_TASKS} tasks.
 */
public class Keef {
    private static final int MAX_TASKS = 100;
    private static final String DIVIDER = "____________________________________________________________";

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
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;
        while (input.hasNextLine()) {
            String command = input.nextLine();
            System.out.println(DIVIDER);

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(DIVIDER);
                break;
            }

            try {
                if (command.equals("list")) {
                    printTasks(tasks, taskCount);
                } else if (command.equals("mark") || command.startsWith("mark ")) {
                    markTask(command, tasks, taskCount);
                } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                    unmarkTask(command, tasks, taskCount);
                } else if (command.equals("delete") || command.startsWith("delete ")) {
                    taskCount = deleteTask(command, tasks, taskCount);
                } else if (command.equals("todo") || command.startsWith("todo ")) {
                    taskCount = addTodo(command, tasks, taskCount);
                } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                    taskCount = addDeadline(command, tasks, taskCount);
                } else if (command.equals("event") || command.startsWith("event ")) {
                    taskCount = addEvent(command, tasks, taskCount);
                } else if (command.trim().isEmpty()) {
                    throw new KeefException("No command was entered.",
                            "Enter a command such as: todo read a book");
                } else {
                    throw new KeefException("I don't recognise that command.",
                            "Use todo, deadline, event, list, mark, unmark, delete, or bye.");
                }
            } catch (KeefException e) {
                System.out.println(e.getUserMessage());
            }
            System.out.println(DIVIDER);
        }
    }

    /**
     * Adds the description in a todo command as an incomplete to-do task.
     *
     * @return the updated number of stored tasks
     */
    private static int addTodo(String command, Task[] tasks, int taskCount) throws KeefException {
        String description = command.substring("todo".length()).trim();
        if (description.isEmpty()) {
            throw new KeefException("A to-do needs a description.", "Enter: todo read a book");
        }
        if (taskCount >= MAX_TASKS) {
            throw new KeefException("The task list is full.", "Restart Keef to begin a new task list.");
        }

        tasks[taskCount] = new Todo(description);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + tasks[taskCount]);
        taskCount++;
        System.out.println("Now you have " + taskCount + " tasks in the list.");
        return taskCount;
    }

    /**
     * Adds the description and due text in a deadline command as an incomplete deadline.
     *
     * @return the updated number of stored tasks
     */
    private static int addDeadline(String command, Task[] tasks, int taskCount) throws KeefException {
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
        if (taskCount >= MAX_TASKS) {
            throw new KeefException("The task list is full.", "Restart Keef to begin a new task list.");
        }

        tasks[taskCount] = new Deadline(description, by);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + tasks[taskCount]);
        taskCount++;
        System.out.println("Now you have " + taskCount + " tasks in the list.");
        return taskCount;
    }

    /**
     * Adds the description, start text, and end text in an event command.
     *
     * @return the updated number of stored tasks
     */
    private static int addEvent(String command, Task[] tasks, int taskCount) throws KeefException {
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
        if (taskCount >= MAX_TASKS) {
            throw new KeefException("The task list is full.", "Restart Keef to begin a new task list.");
        }

        tasks[taskCount] = new Event(description, from, to);
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + tasks[taskCount]);
        taskCount++;
        System.out.println("Now you have " + taskCount + " tasks in the list.");
        return taskCount;
    }

    /**
     * Prints every task and its completion status.
     */
    private static void printTasks(Task[] tasks, int taskCount) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println((i + 1) + "." + tasks[i]);
        }
    }

    /**
     * Marks the one-based task number in a mark command as complete.
     */
    private static void markTask(String command, Task[] tasks, int taskCount) throws KeefException {
        int taskNumber = readTaskNumber(command.substring("mark".length()).trim(), taskCount, "mark");
        int taskIndex = taskNumber - 1;
        tasks[taskIndex].markAsDone();
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + tasks[taskIndex]);
    }

    /**
     * Marks the one-based task number in an unmark command as incomplete.
     */
    private static void unmarkTask(String command, Task[] tasks, int taskCount) throws KeefException {
        int taskNumber = readTaskNumber(command.substring("unmark".length()).trim(), taskCount, "unmark");
        int taskIndex = taskNumber - 1;
        tasks[taskIndex].markAsNotDone();
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + tasks[taskIndex]);
    }

    /**
     * Removes the task specified by the one-based task number in a delete command.
     * Remaining tasks are shifted left so that list numbering stays consecutive.
     *
     * @return the updated number of stored tasks
     */
    private static int deleteTask(String command, Task[] tasks, int taskCount) throws KeefException {
        int taskNumber = readTaskNumber(command.substring("delete".length()).trim(), taskCount, "delete");
        int taskIndex = taskNumber - 1;
        Task removedTask = tasks[taskIndex];
        for (int i = taskIndex; i < taskCount - 1; i++) {
            tasks[i] = tasks[i + 1];
        }
        tasks[taskCount - 1] = null;
        taskCount--;

        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + removedTask);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
        return taskCount;
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
            taskNumber = taskNumber * 10 + (character - '0');
            if (taskNumber > MAX_TASKS) {
                break;
            }
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new KeefException("That task number is not in the list.",
                    "Enter a number from 1 to " + taskCount + ".");
        }
        return taskNumber;
    }

}
