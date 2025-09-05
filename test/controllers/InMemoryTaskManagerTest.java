package test;

import controllers.InMemoryTaskManager;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    @DisplayName("Создание задачи: должна успешно создаваться и добавляться в менеджер")
    void shouldCreateTask() {
        Task task = new Task(0, "Test Task", "Desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 12, 0));
        manager.createTask(task);

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getName());
    }

    @Test
    @DisplayName("Проверка пересечения задач: должна выбрасываться ошибка при наложении временных интервалов")
    void shouldNotAllowOverlappingTasks() {
        Task t1 = new Task(0, "Task 1", "Desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 12, 0));
        manager.createTask(t1);

        Task t2 = new Task(0, "Task 2", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 12, 30));

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(t2));
    }
}
