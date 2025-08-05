package test;  // Тесты в отдельном пакете

import controllers.FileBackedTaskManager;
import model.*;
import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = File.createTempFile("tasks", ".csv");
    }

    @Test
    void shouldSaveAndLoadTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        // Тестируем через публичные методы
        Task task = new Task("Test", "Description", Status.NEW);
        manager.createTask(task);  // Автоматически вызывает save()

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, loaded.getAllTasks().size());
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }
}