package controllers;

import model.*;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void shouldSaveAndLoadTasks() {
        // Создаем эпик сначала, так как подзадачи зависят от него
        Epic epic = new Epic(1, "Test Epic", "Description");
        manager.createEpic(epic);

        // Создаем обычную задачу
        Task task = new Task(2, "Test Task", "Description", Status.NEW);
        manager.createTask(task);

        // Создаем подзадачу
        Subtask subtask = new Subtask(3, "Test Subtask", "Description", Status.NEW, 1);
        manager.createSubtask(subtask);

        // Загружаем из файла и проверяем
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}