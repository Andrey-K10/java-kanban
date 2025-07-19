package Сontrollers;

import Model.*;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void createTaskShouldGenerateIdIfZero() {
        Task task = new Task(0, "Task", "Desc", Status.NEW);
        int id = taskManager.createTask(task);

        assertNotEquals(0, id);
        assertEquals(id, task.getId());
    }

    @Test
    void deleteEpicShouldRemoveItsSubtasks() {
        Epic epic = new Epic(1, "Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(2, "Subtask", "Desc", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        taskManager.deleteEpicById(epic.getId());

        assertNull(taskManager.getSubtaskById(subtask.getId()));
        assertNull(taskManager.getEpicById(epic.getId()));
    }

    @Test
    void updateSubtaskShouldAffectEpicStatus() {
        Epic epic = new Epic(1, "Epic", "Desc");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(2, "Subtask", "Desc", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        subtask.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask);

        assertEquals(Status.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void shouldNotAllowSubtaskToBeItsOwnEpic() {
        assertThrows(IllegalArgumentException.class, () -> {
            Subtask subtask = new Subtask(1, "Subtask", "Desc", Status.NEW, 1);
            taskManager.createSubtask(subtask);
        });
    }

    @Test
    void taskModificationAfterAddingShouldNotAffectManager() {
        Task task = new Task(1, "Original", "Desc", Status.NEW);
        taskManager.createTask(task);

        task.setName("Modified");
        task.setStatus(Status.DONE);

        Task savedTask = taskManager.getTaskById(1);
        assertEquals("Original", savedTask.getName());
        assertEquals(Status.NEW, savedTask.getStatus());
    }
}