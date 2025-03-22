package Сontrollers;

import Model.Task;
import Model.Epic;
import Model.Subtask;
import Model.Status;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    @Test
    void tasksShouldBeAddedAndFoundById() { // проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
        TaskManager taskManager = Managers.getDefault();

        Task task = new Task(1, "Task 1", "Description 1", Status.NEW);
        taskManager.createTask(task);

        Epic epic = new Epic(2, "Epic 1", "Description 1");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask(3, "Subtask 1", "Description 1", Status.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        assertEquals(task, taskManager.getTaskById(1), "Задача должна быть найдена по id.");
        assertEquals(epic, taskManager.getEpicById(2), "Эпик должен быть найден по id.");
        assertEquals(subtask, taskManager.getSubtaskById(3), "Подзадача должна быть найдена по id.");
    }

    @Test
    void taskShouldRemainUnchangedAfterAddingToManager() { // создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
        TaskManager taskManager = Managers.getDefault();

        Task task = new Task(1, "Task 1", "Description 1", Status.NEW);
        int taskId = taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(taskId);

        assertEquals(task.getId(), savedTask.getId(), "Id задачи не должен изменяться.");
        assertEquals(task.getName(), savedTask.getName(), "Название задачи не должно изменяться.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи не должно изменяться.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задачи не должен изменяться.");
    }
}