# UI test plan

## Program

- Compile with Java 25: `javac -d out src/main/java/*.java`
- Run command: `java -cp out Keef`
- The expected-output blocks below contain a command's response body. The runner
  also records the surrounding divider lines in its console transcript.

## Test case: Report input errors

### Aim

Verify that the chatbot gives an error and a correction for every invalid command
form supported by the current version.

#### Command

```text
todo
```

#### Expected output

```text
Error: A to-do needs a description.
Try: Enter: todo read a book
```

#### Command

```text
blah
```

#### Expected output

```text
Error: I don't recognise that command.
Try: Use todo, deadline, event, list, mark, unmark, or bye.
```

#### Command

```text
bye
```

#### Expected output

```text
Bye. Hope to see you again soon!
```

## Test case: Add, mark, and unmark to-do tasks

### Aim

Verify that the `todo` command adds an incomplete to-do task and that to-do tasks
can be marked done, returned to not done, and displayed with the correct type and
status markers.

#### Command

```text
todo read book
```

#### Expected output

```text
Got it. I've added this task:
  [T][ ] read book
Now you have 1 tasks in the list.
```

#### Command

```text
todo return book
```

#### Expected output

```text
Got it. I've added this task:
  [T][ ] return book
Now you have 2 tasks in the list.
```

#### Command

```text
todo buy bread
```

#### Expected output

```text
Got it. I've added this task:
  [T][ ] buy bread
Now you have 3 tasks in the list.
```

#### Command

```text
mark 1
```

#### Expected output

```text
Nice! I've marked this task as done:
  [T][X] read book
```

#### Command

```text
mark 2
```

#### Expected output

```text
Nice! I've marked this task as done:
  [T][X] return book
```

#### Command

```text
unmark 2
```

#### Expected output

```text
OK, I've marked this task as not done yet:
  [T][ ] return book
```

#### Command

```text
list
```

#### Expected output

```text
Here are the tasks in your list:
1.[T][X] read book
2.[T][ ] return book
3.[T][ ] buy bread
```

#### Command

```text
bye
```

#### Expected output

```text
Bye. Hope to see you again soon!
```

## Test case: Add and list events

### Aim

Verify that the `event` command stores its description, `/from` text, and `/to` text
without interpreting the dates or times.

#### Command

```text
event project meeting /from Mon 2pm /to 4pm
```

#### Expected output

```text
Got it. I've added this task:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 1 tasks in the list.
```

#### Command

```text
event orientation week /from 4/10/2019 /to 11/10/2019
```

#### Expected output

```text
Got it. I've added this task:
  [E][ ] orientation week (from: 4/10/2019 to: 11/10/2019)
Now you have 2 tasks in the list.
```

#### Command

```text
list
```

#### Expected output

```text
Here are the tasks in your list:
1.[E][ ] project meeting (from: Mon 2pm to: 4pm)
2.[E][ ] orientation week (from: 4/10/2019 to: 11/10/2019)
```

#### Command

```text
bye
```

#### Expected output

```text
Bye. Hope to see you again soon!
```

## Test case: Add and list deadlines

### Aim

Verify that the `deadline` command stores its description and `/by` text without
interpreting the date or time.

#### Command

```text
deadline return book /by Sunday
```

#### Expected output

```text
Got it. I've added this task:
  [D][ ] return book (by: Sunday)
Now you have 1 tasks in the list.
```

#### Command

```text
deadline do homework /by no idea :-p
```

#### Expected output

```text
Got it. I've added this task:
  [D][ ] do homework (by: no idea :-p)
Now you have 2 tasks in the list.
```

#### Command

```text
list
```

#### Expected output

```text
Here are the tasks in your list:
1.[D][ ] return book (by: Sunday)
2.[D][ ] do homework (by: no idea :-p)
```

#### Command

```text
bye
```

#### Expected output

```text
Bye. Hope to see you again soon!
```
