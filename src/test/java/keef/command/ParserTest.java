package keef.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import keef.exception.KeefException;

/**
 * Tests validation and data extraction performed by {@link Parser}.
 */
class ParserTest {

    @Test
    void parse_commandAndTrimmedArguments_commandAndArgumentsReturned() throws KeefException {
        Parser.ParsedCommand parsed = Parser.parse("  deadline   submit report /by 2026-09-01  ");

        assertEquals(Command.DEADLINE, parsed.getCommand());
        assertEquals("submit report /by 2026-09-01", parsed.getArguments());
    }

    @Test
    void parse_noArgumentCommand_commandReturnedWithEmptyArguments() throws KeefException {
        Parser.ParsedCommand parsed = Parser.parse("list");

        assertEquals(Command.LIST, parsed.getCommand());
        assertEquals("", parsed.getArguments());
    }

    @Test
    void parse_nullEmptyAndUnknownInput_exceptionThrown() {
        assertThrows(KeefException.class, () -> Parser.parse(null));
        assertThrows(KeefException.class, () -> Parser.parse("   "));
        assertThrows(KeefException.class, () -> Parser.parse("listing"));
    }

    @Test
    void parseTodoDescription_presentDescription_descriptionReturned() throws KeefException {
        assertEquals("read a book", Parser.parseTodoDescription("read a book"));
    }

    @Test
    void parseTodoDescription_emptyDescription_exceptionThrown() {
        assertThrows(KeefException.class, () -> Parser.parseTodoDescription(""));
    }

    @Test
    void parseDeadlineDetails_validDetails_trimmedFieldsReturned() throws KeefException {
        Parser.DeadlineDetails details = Parser.parseDeadlineDetails("  submit report  /by  2026-09-01 ");

        assertEquals("submit report", details.getDescription());
        assertEquals("2026-09-01", details.getBy());
    }

    @Test
    void parseDeadlineDetails_missingOrMalformedParts_exceptionThrown() {
        assertThrows(KeefException.class, () -> Parser.parseDeadlineDetails("submit report"));
        assertThrows(KeefException.class, () -> Parser.parseDeadlineDetails("/by 2026-09-01"));
        assertThrows(KeefException.class, () -> Parser.parseDeadlineDetails("submit report /by"));
        assertThrows(KeefException.class, () -> Parser.parseDeadlineDetails("submit /bydate"));
    }

    @Test
    void parseEventDetails_validDetails_trimmedFieldsReturned() throws KeefException {
        Parser.EventDetails details = Parser.parseEventDetails(
                "  project meeting  /from  2026-09-01 09:00  /to  2026-09-01 10:00 ");

        assertEquals("project meeting", details.getDescription());
        assertEquals("2026-09-01 09:00", details.getFrom());
        assertEquals("2026-09-01 10:00", details.getTo());
    }

    @Test
    void parseEventDetails_missingMalformedOrOutOfOrderParts_exceptionThrown() {
        assertThrows(KeefException.class, () -> Parser.parseEventDetails("meeting /to noon"));
        assertThrows(KeefException.class, () -> Parser.parseEventDetails("meeting /from 9am"));
        assertThrows(KeefException.class, () -> Parser.parseEventDetails("meeting /to noon /from 9am"));
        assertThrows(KeefException.class, () -> Parser.parseEventDetails("/from 9am /to noon"));
        assertThrows(KeefException.class, () -> Parser.parseEventDetails("meeting /from /to noon"));
        assertThrows(KeefException.class, () -> Parser.parseEventDetails("meeting /from 9am /to"));
        assertThrows(KeefException.class, () -> Parser.parseEventDetails("meeting /fromtime 9am /to noon"));
    }

    @Test
    void parseOnDate_supportedFormats_datesReturned() throws KeefException {
        assertEquals(LocalDate.of(2026, 9, 1), Parser.parseOnDate("2026-09-01"));
        assertEquals(LocalDate.of(2026, 9, 1), Parser.parseOnDate("1/9/2026"));
        assertEquals(LocalDate.of(2026, 9, 1), Parser.parseOnDate("Sep 1 2026"));
        assertEquals(LocalDate.of(2026, 9, 1), Parser.parseOnDate("1 Sep 2026"));
    }

    @Test
    void parseOnDate_emptyOrInvalidDate_exceptionThrown() {
        assertThrows(KeefException.class, () -> Parser.parseOnDate(""));
        assertThrows(KeefException.class, () -> Parser.parseOnDate("tomorrow"));
        assertThrows(KeefException.class, () -> Parser.parseOnDate("2026-02-30"));
    }

    @Test
    void parseTaskNumber_validBoundaryNumbers_numbersReturned() throws KeefException {
        assertEquals(1, Parser.parseTaskNumber("1", 3, "mark"));
        assertEquals(3, Parser.parseTaskNumber("3", 3, "mark"));
    }

    @Test
    void parseTaskNumber_missingMalformedOutOfRangeOrOverflowNumber_exceptionThrown() {
        assertThrows(KeefException.class, () -> Parser.parseTaskNumber("1", 0, "mark"));
        assertThrows(KeefException.class, () -> Parser.parseTaskNumber("", 3, "mark"));
        assertThrows(KeefException.class, () -> Parser.parseTaskNumber("1.0", 3, "mark"));
        assertThrows(KeefException.class, () -> Parser.parseTaskNumber("0", 3, "mark"));
        assertThrows(KeefException.class, () -> Parser.parseTaskNumber("4", 3, "mark"));
        assertThrows(KeefException.class, () -> Parser.parseTaskNumber("999999999999999999999", 3, "mark"));
    }
}
