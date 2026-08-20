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

            if (command.equals("list")) {
                printTasks(tasks, taskCount);
            } else if (command.startsWith("mark ")) {
                markTask(command, tasks, taskCount);
            } else if (command.startsWith("unmark ")) {
                unmarkTask(command, tasks, taskCount);
            } else if (command.equals("todo") || command.startsWith("todo ")) {
                taskCount = addTodo(command, tasks, taskCount);
            } else if (command.startsWith("deadline ")) {
                taskCount = addDeadline(command, tasks, taskCount);
            } else if (command.startsWith("event ")) {
                taskCount = addEvent(command, tasks, taskCount);
            } else {
                System.out.println("I don't recognise that command. Try todo, deadline, event, list, mark, unmark, or bye.");
            }
            System.out.println(DIVIDER);
        }
    }

    /**
     * Adds the description in a todo command as an incomplete to-do task.
     *
     * @return the updated number of stored tasks
     */
    private static int addTodo(String command, Task[] tasks, int taskCount) {
        String description = command.substring("todo".length()).trim();
        if (description.isEmpty()) {
            System.out.println("A to-do needs a description. For example: todo read a book");
            return taskCount;
        }
        if (taskCount >= MAX_TASKS) {
            System.out.println("Sorry, the task list is full.");
            return taskCount;
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
    private static int addDeadline(String command, Task[] tasks, int taskCount) {
        String details = command.substring("deadline ".length()).trim();
        int byMarkerIndex = details.indexOf(" /by ");
        if (byMarkerIndex < 0) {
            System.out.println("Please specify a deadline using /by.");
            return taskCount;
        }

        String description = details.substring(0, byMarkerIndex).trim();
        String by = details.substring(byMarkerIndex + " /by ".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            System.out.println("Please provide both a description and a deadline after /by.");
            return taskCount;
        }
        if (taskCount >= MAX_TASKS) {
            System.out.println("Sorry, the task list is full.");
            return taskCount;
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
    private static int addEvent(String command, Task[] tasks, int taskCount) {
        String details = command.substring("event ".length()).trim();
        int fromMarkerIndex = details.indexOf(" /from ");
        int toMarkerIndex = details.indexOf(" /to ");
        if (fromMarkerIndex < 0 || toMarkerIndex < 0 || toMarkerIndex < fromMarkerIndex) {
            System.out.println("Please specify an event using /from and /to.");
            return taskCount;
        }

        String description = details.substring(0, fromMarkerIndex).trim();
        String from = details.substring(fromMarkerIndex + " /from ".length(), toMarkerIndex).trim();
        String to = details.substring(toMarkerIndex + " /to ".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            System.out.println("Please provide an event description, start, and end time.");
            return taskCount;
        }
        if (taskCount >= MAX_TASKS) {
            System.out.println("Sorry, the task list is full.");
            return taskCount;
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
    private static void markTask(String command, Task[] tasks, int taskCount) {
        try {
            int taskNumber = Integer.parseInt(command.substring("mark ".length()));
            if (taskNumber < 1 || taskNumber > taskCount) {
                System.out.println("Please enter a task number from 1 to " + taskCount + ".");
                return;
            }

            int taskIndex = taskNumber - 1;
            tasks[taskIndex].markAsDone();
            System.out.println("Nice! I've marked this task as done:");
            System.out.println("  " + tasks[taskIndex]);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a task number after mark.");
        }
    }

    /**
     * Marks the one-based task number in an unmark command as incomplete.
     */
    private static void unmarkTask(String command, Task[] tasks, int taskCount) {
        try {
            int taskNumber = Integer.parseInt(command.substring("unmark ".length()));
            if (taskNumber < 1 || taskNumber > taskCount) {
                System.out.println("Please enter a task number from 1 to " + taskCount + ".");
                return;
            }

            int taskIndex = taskNumber - 1;
            tasks[taskIndex].markAsNotDone();
            System.out.println("OK, I've marked this task as not done yet:");
            System.out.println("  " + tasks[taskIndex]);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a task number after unmark.");
        }
    }

}
