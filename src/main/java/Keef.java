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
        String[] tasks = new String[MAX_TASKS];
        boolean[] completedTasks = new boolean[MAX_TASKS];
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
                printTasks(tasks, completedTasks, taskCount);
            } else if (command.startsWith("mark ")) {
                markTask(command, tasks, completedTasks, taskCount);
            } else if (taskCount < MAX_TASKS) {
                tasks[taskCount] = command;
                taskCount++;
                System.out.println("added: " + command);
            } else {
                System.out.println("Sorry, the task list is full.");
            }
            System.out.println(DIVIDER);
        }
    }

    /**
     * Prints every task and its completion status.
     */
    private static void printTasks(String[] tasks, boolean[] completedTasks, int taskCount) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println((i + 1) + "." + getStatus(completedTasks[i]) + " " + tasks[i]);
        }
    }

    /**
     * Marks the one-based task number in a mark command as complete.
     */
    private static void markTask(String command, String[] tasks, boolean[] completedTasks, int taskCount) {
        try {
            int taskNumber = Integer.parseInt(command.substring("mark ".length()));
            if (taskNumber < 1 || taskNumber > taskCount) {
                System.out.println("Please enter a task number from 1 to " + taskCount + ".");
                return;
            }

            int taskIndex = taskNumber - 1;
            completedTasks[taskIndex] = true;
            System.out.println("Nice! I've marked this task as done:");
            System.out.println("  " + getStatus(completedTasks[taskIndex]) + " " + tasks[taskIndex]);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a task number after mark.");
        }
    }

    /**
     * Returns the display marker for a task's completion state.
     */
    private static String getStatus(boolean isCompleted) {
        return isCompleted ? "[X]" : "[ ]";
    }
}
