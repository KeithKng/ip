# UI test plan

## Program

- Compile with Java 25: `javac -d out src/main/java/*.java`
- Run command: `java -cp out Keef`
- The expected-output blocks below contain a command's response body. The runner
  also records the surrounding divider lines in its console transcript.

## Test case: Mark and unmark tasks

### Aim

Verify that tasks can be marked done, returned to not done, and displayed with the
correct status markers.

#### Command

```text
read book
```

#### Expected output

```text
added: read book
```

#### Command

```text
return book
```

#### Expected output

```text
added: return book
```

#### Command

```text
buy bread
```

#### Expected output

```text
added: buy bread
```

#### Command

```text
mark 1
```

#### Expected output

```text
Nice! I've marked this task as done:
  [X] read book
```

#### Command

```text
mark 2
```

#### Expected output

```text
Nice! I've marked this task as done:
  [X] return book
```

#### Command

```text
unmark 2
```

#### Expected output

```text
OK, I've marked this task as not done yet:
  [ ] return book
```

#### Command

```text
list
```

#### Expected output

```text
Here are the tasks in your list:
1.[X] read book
2.[ ] return book
3.[ ] buy bread
```

#### Command

```text
bye
```

#### Expected output

```text
Bye. Hope to see you again soon!
```
