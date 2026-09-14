# Aster User Guide

Aster is a star-themed task manager with a JavaFX desktop interface. Enter a command
in the input field and press Enter. Tasks are saved automatically between runs.

## Commands

| Command | Example | Purpose |
| --- | --- | --- |
| `todo` | `todo read a book` | Adds a task without a date. |
| `deadline` | `deadline submit report /by 2026-09-30` | Adds a task due by a date or time. |
| `event` | `event team meeting /from 2026-09-20 10:00 /to 2026-09-20 11:00` | Adds a scheduled event. |
| `list` | `list` | Shows all tasks. |
| `ondate` | `ondate 2026-09-20` | Shows tasks on a date. |
| `mark` / `unmark` | `mark 1` | Changes completion status. |
| `delete` | `delete 1` | Removes a task. |
| `find` | `find report` | Finds tasks containing a keyword. |
| `tag` | `tag 1 #urgent` | Adds a tag to a task. |
| `bye` | `bye` | Closes the application. |

Task numbers are the numbers shown by `list`. Dates accept ISO format (`yyyy-mm-dd`)
and several common alternatives. Invalid or incomplete commands produce a helpful
error message without stopping the application.

## Storage

Tasks are stored in `data/aster.txt`. The file is created and updated automatically;
it should not be edited while Aster is running.
