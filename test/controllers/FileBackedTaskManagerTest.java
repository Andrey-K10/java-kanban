package test;

import controllers.FileBackedTaskManager;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private Path tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = Files.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile.toFile());
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = new Task(0, "Task 1", "Desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 12, 0));
        manager.createTask(task);

        Epic epic = new Epic(0, "Epic 1", "Desc");
        manager.createEpic(epic);

        Subtask sub = new Subtask(0, "Subtask 1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 2, 10, 0), epic.getId());
        manager.createSubtask(sub);

        // Загружаем из файла
        FileBackedTaskManager reloaded = new FileBackedTaskManager(tempFile.toFile());

        List<Task> tasks = reloaded.getAllTasks();
        assertEquals(3, tasks.size());
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Task 1")));
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Epic 1")));
        assertTrue(tasks.stream().anyMatch(t -> t.getName().equals("Subtask 1")));
    }
}
