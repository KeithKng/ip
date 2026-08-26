import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Parses user-entered command text into structured command data.
 */
public final class Parser {
    private static final List<DateTimeFormatter> ONDATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    );

    private Parser() {
        // utility class
    }

    /**
     * Parses a full command line into command type and argument text.
     *
     * @param input full command line
     * @return parsed command
     * @throws KeefException when the command is empty or unknown
     */
    public static ParsedCommand parse(String input) throws KeefException {
        if (input == null || input.trim().isEmpty()) {
            throw new KeefException("No command was entered.",
                    "Enter a command such as: todo read a book");
        }

        String trimmed = input.trim();
        Command command = Command.fromInput(trimmed);
        if (command == null) {
            throw new KeefException("I don't recognise that command.",
                    "Use todo, deadline, event, list, ondate, mark, unmark, delete, or bye.");
        }

        String keyword = command.getKeyword();
        String arguments = trimmed.equals(keyword) ? "" : trimmed.substring(keyword.length()).trim();
        return new ParsedCommand(command, arguments);
    }

    /**
     * Validates and extracts a to-do description.
     *
     * @param arguments text after the todo keyword
     * @return description text
     * @throws KeefException when description is missing
     */
    public static String parseTodoDescription(String arguments) throws KeefException {
        if (arguments.isEmpty()) {
            throw new KeefException("A to-do needs a description.", "Enter: todo read a book");
        }
        return arguments;
    }

    /**
     * Validates and extracts deadline details.
     *
     * @param arguments text after the deadline keyword
     * @return parsed deadline details
     * @throws KeefException when description or /by value is missing
     */
    public static DeadlineDetails parseDeadlineDetails(String arguments) throws KeefException {
        int byMarkerIndex = findMarker(arguments, "/by");
        if (byMarkerIndex < 0) {
            throw new KeefException("A deadline needs a /by date or time.",
                    "Enter: deadline return book /by Sunday");
        }

        String description = arguments.substring(0, byMarkerIndex).trim();
        String by = arguments.substring(byMarkerIndex + "/by".length()).trim();
        if (description.isEmpty()) {
            throw new KeefException("The deadline description is missing.",
                    "Enter: deadline return book /by Sunday");
        }
        if (by.isEmpty()) {
            throw new KeefException("The deadline date or time is missing.",
                    "Add a value after /by, for example: deadline return book /by Sunday");
        }
        return new DeadlineDetails(description, by);
    }

    /**
     * Validates and extracts event details.
     *
     * @param arguments text after the event keyword
     * @return parsed event details
     * @throws KeefException when required fields are missing or malformed
     */
    public static EventDetails parseEventDetails(String arguments) throws KeefException {
        int fromMarkerIndex = findMarker(arguments, "/from");
        int toMarkerIndex = findMarker(arguments, "/to");
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

        String description = arguments.substring(0, fromMarkerIndex).trim();
        String from = arguments.substring(fromMarkerIndex + "/from".length(), toMarkerIndex).trim();
        String to = arguments.substring(toMarkerIndex + "/to".length()).trim();
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
        return new EventDetails(description, from, to);
    }

    /**
     * Validates and parses an ondate argument into a date.
     *
     * @param arguments text after the ondate keyword
     * @return parsed date
     * @throws KeefException when date is missing or invalid
     */
    public static LocalDate parseOnDate(String arguments) throws KeefException {
        if (arguments.isEmpty()) {
            throw new KeefException("A date is required.", "Enter: ondate 2019-12-02");
        }

        for (DateTimeFormatter formatter : ONDATE_FORMATTERS) {
            try {
                return LocalDate.parse(arguments, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }
        throw new KeefException("The date must be in yyyy-mm-dd format.", "Enter: ondate 2019-12-02");
    }

    /**
     * Validates and parses a one-based task number.
     *
     * @param numberText user-entered number text
     * @param taskCount number of tasks in the list
     * @param commandName command name used in recovery messages
     * @return parsed one-based task number
     * @throws KeefException when number is missing, malformed, or out of range
     */
    public static int parseTaskNumber(String numberText, int taskCount, String commandName) throws KeefException {
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
     * Immutable parsed command data.
     */
    public static final class ParsedCommand {
        private final Command command;
        private final String arguments;

        private ParsedCommand(Command command, String arguments) {
            this.command = command;
            this.arguments = arguments;
        }

        public Command getCommand() {
            return command;
        }

        public String getArguments() {
            return arguments;
        }
    }

    /**
     * Immutable deadline input data.
     */
    public static final class DeadlineDetails {
        private final String description;
        private final String by;

        private DeadlineDetails(String description, String by) {
            this.description = description;
            this.by = by;
        }

        public String getDescription() {
            return description;
        }

        public String getBy() {
            return by;
        }
    }

    /**
     * Immutable event input data.
     */
    public static final class EventDetails {
        private final String description;
        private final String from;
        private final String to;

        private EventDetails(String description, String from, String to) {
            this.description = description;
            this.from = from;
            this.to = to;
        }

        public String getDescription() {
            return description;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }
}
