package keef.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import keef.Keef;
import keef.exception.KeefException;
import keef.task.Deadline;
import keef.task.Event;
import keef.task.Task;
import keef.task.TaskList;
import keef.task.Todo;

/**
 * Persists tasks to disk and reconstructs them from storage.
 */
public class Storage {
    private final Path storagePath;

    /**
     * Creates storage backed by the given path.
     *
     * @param filePath absolute path or path relative to repository root (fallback: working directory)
     */
    public Storage(String filePath) {
        this.storagePath = resolveStoragePath(filePath);
    }

    /**
     * Loads tasks from storage.
     *
     * @return list of loaded tasks
     * @throws KeefException when the storage file cannot be read
     */
    public List<Task> load() throws KeefException {
        List<Task> tasks = new ArrayList<>();
        if (!Files.exists(storagePath)) {
            return tasks;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(storagePath, StandardCharsets.UTF_8);
        } catch (IOException | SecurityException e) {
            throw new KeefException("Unable to read storage file: " + storagePath,
                    "Check that the file and directory can be read.");
        }

        int skipped = 0;
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            try {
                Task parsedTask = parseStorageLine(line);
                if (parsedTask == null) {
                    skipped++;
                } else {
                    tasks.add(parsedTask);
                }
            } catch (IllegalArgumentException e) {
                skipped++;
                System.err.println("Warning: Skipping malformed storage line: \"" + line + "\" (" + e.getMessage() + ")");
            }
        }

        if (lines.size() > 0 && skipped >= Math.max(1, lines.size() / 2)) {
            backupCorruptedStorage();
            return new ArrayList<>();
        }
        return tasks;
    }

    /**
     * Saves tasks to storage.
     *
     * @param taskList tasks to persist
     * @throws KeefException when saving fails
     */
    public void save(TaskList taskList) throws KeefException {
        List<String> lines = new ArrayList<>();
        for (Task task : taskList.getAll()) {
            lines.add(task.toStorageString());
        }

        Path parentPath = storagePath.getParent();
        Path tempFile = parentPath != null
                ? parentPath.resolve(storagePath.getFileName().toString() + ".tmp")
                : Paths.get(storagePath.toString() + ".tmp");

        try {
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }
            Files.write(tempFile, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, storagePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile, storagePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | SecurityException e) {
            cleanupTempFile(tempFile);
            throw new KeefException("I couldn't save your tasks.",
                    "Check that the data folder is writable and try again.");
        }
    }

    private static Path resolveStoragePath(String filePath) {
        Path configuredPath = Paths.get(filePath);
        if (configuredPath.isAbsolute()) {
            return configuredPath;
        }

        Path repositoryRoot = findRepositoryRoot();
        if (repositoryRoot != null) {
            return repositoryRoot.resolve(configuredPath);
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().resolve(configuredPath);
    }

    private static Path findRepositoryRoot() {
        try {
            Path codeLocation = Paths.get(Keef.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();
            Path cursor = Files.isRegularFile(codeLocation) ? codeLocation.getParent() : codeLocation;
            while (cursor != null) {
                if (Files.exists(cursor.resolve(".git"))) {
                    return cursor;
                }
                cursor = cursor.getParent();
            }
        } catch (Exception ignored) {
            // fall through to working-directory fallback
        }
        return null;
    }

    private Task parseStorageLine(String line) {
        String[] parts = line.split("\\s*\\|\\s*");
        String type = parts.length > 0 ? parts[0].trim() : "";
        String status = parts.length > 1 ? parts[1].trim() : "0";
        boolean isDone = "1".equals(status);

        Task task;
        switch (type) {
        case "T":
            if (parts.length < 3) {
                throw new IllegalArgumentException("missing description");
            }
            task = new Todo(parts[2].trim());
            break;
        case "D":
            if (parts.length < 4) {
                throw new IllegalArgumentException("missing fields");
            }
            task = new Deadline(parts[2].trim(), parts[3].trim());
            break;
        case "E":
            if (parts.length < 5) {
                throw new IllegalArgumentException("missing fields");
            }
            task = new Event(parts[2].trim(), parts[3].trim(), parts[4].trim());
            break;
        default:
            System.err.println("Warning: Unknown task type in storage file: " + type);
            return null;
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    private void backupCorruptedStorage() {
        try {
            Path backupPath = storagePath.resolveSibling(
                    storagePath.getFileName().toString() + ".corrupt." + System.currentTimeMillis());
            Path parent = storagePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.move(storagePath, backupPath);
            System.err.println("Storage file appeared corrupted; moved to " + backupPath + " and starting with empty task list.");
        } catch (IOException | SecurityException e) {
            System.err.println("Warning: Failed to backup corrupted storage file: " + e.getMessage());
        }
    }

    private void cleanupTempFile(Path tempFile) {
        try {
            if (Files.exists(tempFile)) {
                Files.delete(tempFile);
            }
        } catch (IOException | SecurityException ignored) {
            // best-effort cleanup only
        }
    }
}
