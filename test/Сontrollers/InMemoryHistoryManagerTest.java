package Сontrollers;

import Model.Task;
import Model.Status;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void historyShouldPreserveTaskState() { // убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(1, "Task 1", "Description 1", Status.NEW);
        historyManager.add(task);

        task.setStatus(Status.DONE); //

        Task savedTask = historyManager.getHistory().get(0);
        assertEquals(Status.NEW, savedTask.getStatus(), "История должна сохранять предыдущее состояние задачи.");
    }
}