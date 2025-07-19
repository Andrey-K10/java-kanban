package Сontrollers;

import Model.*;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void addShouldKeepOnlyLastTaskInstance() {
        Task task = new Task(1, "Task", "Description", Status.NEW);

        // Добавляем задачу дважды с разными статусами
        historyManager.add(task);
        task.setStatus(Status.IN_PROGRESS);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Должна остаться только последняя версия");
        assertEquals(Status.IN_PROGRESS, history.get(0).getStatus());
    }

    @Test
    void removeShouldDeleteTaskFromHistory() {
        Task task = new Task(1, "Task", "Description", Status.NEW);
        historyManager.add(task);

        historyManager.remove(1);

        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void getHistoryShouldReturnTasksInOrder() {
        Task task1 = new Task(1, "Task1", "Desc1", Status.NEW);
        Task task2 = new Task(2, "Task2", "Desc2", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void historyShouldPreserveTaskState() {
        Task task = new Task(1, "Task", "Description", Status.NEW);
        historyManager.add(task);

        // Модифицируем оригинальную задачу
        task.setStatus(Status.DONE);
        task.setName("Modified");

        Task savedTask = historyManager.getHistory().get(0);
        assertEquals(Status.NEW, savedTask.getStatus());
        assertEquals("Task", savedTask.getName());
    }
}