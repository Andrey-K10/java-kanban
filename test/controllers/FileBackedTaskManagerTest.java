package controllers;

import model.*;
import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertAll(
                () -> assertTrue(loaded.getAllTasks().isEmpty()),
                () -> assertTrue(loaded.getAllEpics().isEmpty()),
                () -> assertTrue(loaded.getAllSubtasks().isEmpty())
        );
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = new Task("Test", "Description", Status.NEW);
        int taskId = manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loaded.getTaskById(taskId);

        assertAll(
                () -> assertEquals(task.getName(), loadedTask.getName()),
                () -> assertEquals(task.getDescription(), loadedTask.getDescription()),
                () -> assertEquals(task.getStatus(), loadedTask.getStatus())
        );
    }

    @Test
    void shouldSaveAndLoadEpicWithSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Sub", "Desc", Status.DONE, epicId);
        manager.createSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll(
                () -> assertEquals(1, loaded.getAllEpics().size()),
                () -> assertEquals(1, loaded.getAllSubtasks().size()),
                () -> assertEquals(epicId, loaded.getSubtaskById(epicId + 1).getEpicId())
        );
    }

    @Test
    void shouldHandleFileErrors() {
        File readOnlyFile = new File("/proc/immutable");
        assertThrows(ManagerSaveException.class,
                () -> new FileBackedTaskManager(readOnlyFile).save());
    }
}