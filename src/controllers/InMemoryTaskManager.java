package controllers;

import model.*;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private int nextId = 1;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    // Методы для Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public int createTask(Task task) {
        Task newTask = new Task(
                task.getId() == 0 ? nextId++ : task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus()
        );
        tasks.put(newTask.getId(), newTask);
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task updatedTask = new Task(
                    task.getId(),
                    task.getName(),
                    task.getDescription(),
                    task.getStatus()
            );
            tasks.put(updatedTask.getId(), updatedTask);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    // Методы для Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public int createEpic(Epic epic) {
        Epic newEpic = new Epic(
                epic.getId() == 0 ? nextId++ : epic.getId(),
                epic.getName(),
                epic.getDescription()
        );
        epics.put(newEpic.getId(), newEpic);
        return newEpic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            Epic updatedEpic = new Epic(
                    epic.getId(),
                    epic.getName(),
                    epic.getDescription()
            );
            // Переносим подзадачи из старого эпика
            existingEpic.getSubtaskIds().forEach(updatedEpic::addSubtaskId);
            epics.put(updatedEpic.getId(), updatedEpic);
            updateEpicStatus(updatedEpic);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subtaskId -> {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            });
            historyManager.remove(id);
        }
    }

    // Методы для Subtask
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
        });
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Epic not found");
        }
        if (subtask.getEpicId() == subtask.getId()) {
            throw new IllegalArgumentException("Subtask cannot be its own epic");
        }

        Subtask newSubtask = new Subtask(
                subtask.getId() == 0 ? nextId++ : subtask.getId(),
                subtask.getName(),
                subtask.getDescription(),
                subtask.getStatus(),
                subtask.getEpicId()
        );
        subtasks.put(newSubtask.getId(), newSubtask);
        epics.get(newSubtask.getEpicId()).addSubtaskId(newSubtask.getId());
        updateEpicStatus(epics.get(newSubtask.getEpicId()));
        return newSubtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask existingSubtask = subtasks.get(subtask.getId());
            Subtask updatedSubtask = new Subtask(
                    subtask.getId(),
                    subtask.getName(),
                    subtask.getDescription(),
                    subtask.getStatus(),
                    existingSubtask.getEpicId()
            );
            subtasks.put(updatedSubtask.getId(), updatedSubtask);
            updateEpicStatus(epics.get(updatedSubtask.getEpicId()));
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
            historyManager.remove(id);
        }
    }

    // Вспомогательные методы
    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        if (!epics.containsKey(epicId)) return Collections.emptyList();

        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epics.get(epicId).getSubtaskIds()) {
            if (subtasks.containsKey(subtaskId)) {
                result.add(subtasks.get(subtaskId));
            }
        }
        return result;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        if (epic == null || epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (int subtaskId : epic.getSubtaskIds()) {
            Status status = subtasks.get(subtaskId).getStatus();
            if (status != Status.DONE) allDone = false;
            if (status != Status.NEW) allNew = false;
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}