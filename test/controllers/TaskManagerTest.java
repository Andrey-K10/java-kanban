package test;

import controllers.TaskManager;
import model.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    @Test
    abstract void shouldCreateTask();

    @Test
    void shouldReturnEmptyListWhenNoTasks() {
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void shouldCalculateEpicTimeFromSubtasks() {
        Epic epic = new Epic(0, "Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask sub1 = new Subtask(0, "Subtask 1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 0), epicId);
        Subtask sub2 = new Subtask(0, "Subtask 2", "Desc", Status.NEW,
                Duration.ofMinutes(90), LocalDateTime.of(2025, 1, 1, 12, 0), epicId);

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        Epic updatedEpic = manager.getEpicById(epicId);

        assertEquals(Duration.ofMinutes(120), updatedEpic.getDuration());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), updatedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 13, 30), updatedEpic.getEndTime());
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

    @Test
    void shouldPrioritizeTasksByStartTime() {
        Task t1 = new Task(0, "Task 1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 15, 0));
        Task t2 = new Task(0, "Task 2", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 10, 0));

        manager.createTask(t1);
        manager.createTask(t2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(t2.getId(), prioritized.get(0).getId());
        assertEquals(t1.getId(), prioritized.get(1).getId());
    }
}