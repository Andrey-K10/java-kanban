package controllers;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    // Методы для задач
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    int createTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(int id);

    // Методы для эпиков
    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    int createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpicById(int id);

    // Методы для подзадач
    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtaskById(int id);

    int createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    // Дополнительные методы
    List<Subtask> getSubtasksByEpicId(int epicId);

    void updateEpicStatus(Epic epic);

    List<Task> getHistory();

    // НОВЫЙ метод — список задач по приоритету (startTime)
    List<Task> getPrioritizedTasks();
}