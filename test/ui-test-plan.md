# UI test plan

## Program

- Compile with Java 25: `javac -d out src/main/java/*.java`
- Run command: `java -cp out Keef`
- The expected-output blocks below contain a command's response body. The runner
  also records the surrounding divider lines in its console transcript.

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
