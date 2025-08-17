package test;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import controllers.InMemoryTaskManager;  // Исправленный импорт
import controllers.TaskManager;         // Исправленный импорт

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateTask() {
        Task task = new Task(0, "Test Task", "Desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 12, 0));
        manager.createTask(task);

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getName());
    }

    @Test
    void shouldCreateEpicWithoutSubtasks() {
        Epic epic = new Epic(0, "Epic 1", "Desc");
        manager.createEpic(epic);

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertTrue(tasks.get(0) instanceof Epic);

        Epic createdEpic = (Epic) tasks.get(0);
        assertEquals(Duration.ZERO, createdEpic.getDuration());
        assertNull(createdEpic.getStartTime());
        assertNull(createdEpic.getEndTime());
    }

    @Test
    void shouldCalculateEpicTimeFromSubtasks() {
        Epic epic = new Epic(0, "Epic 1", "Desc");
        manager.createEpic(epic);

        Subtask s1 = new Subtask(0, "Subtask 1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 0), epic.getId());
        Subtask s2 = new Subtask(0, "Subtask 2", "Desc", Status.NEW,
                Duration.ofMinutes(90), LocalDateTime.of(2025, 1, 1, 12, 0), epic.getId());

        manager.createSubtask(s1);
        manager.createSubtask(s2);

        Epic updatedEpic = (Epic) manager.getAllTasks().stream()
                .filter(t -> t.getId() == epic.getId())
                .findFirst().orElseThrow();

        assertEquals(Duration.ofMinutes(120), updatedEpic.getDuration());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), updatedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 13, 30), updatedEpic.getEndTime());
    }

    @Test
    void shouldSortPrioritizedTasks() {
        Task t1 = new Task(0, "Task 1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 15, 0));
        Task t2 = new Task(0, "Task 2", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 0));

        manager.createTask(t1);
        manager.createTask(t2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(t2.getId(), prioritized.get(0).getId()); // t2 раньше
        assertEquals(t1.getId(), prioritized.get(1).getId()); // t1 позже
    }

    @Test
    void shouldNotAllowOverlappingTasks() {
        Task t1 = new Task(0, "Task 1", "Desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 12, 0));
        manager.createTask(t1);

        Task t2 = new Task(0, "Task 2", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 12, 30));

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(t2));
    }
}
