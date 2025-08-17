package controllers;

import model.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected int nextId = 1;
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );

    @Override
    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(epics.values());
        allTasks.addAll(subtasks.values());
        return allTasks;
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().forEach(prioritizedTasks::remove);
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
        Task newTask = new Task(nextId++, task.getName(), task.getDescription(),
                task.getStatus(), task.getDuration(), task.getStartTime());
        checkOverlaps(newTask);
        tasks.put(newTask.getId(), newTask);
        addToPrioritized(newTask);
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task updatedTask = new Task(task.getId(), task.getName(), task.getDescription(),
                    task.getStatus(), task.getDuration(), task.getStartTime());
            prioritizedTasks.removeIf(t -> t.getId() == task.getId());
            checkOverlaps(updatedTask);
            tasks.put(updatedTask.getId(), updatedTask);
            addToPrioritized(updatedTask);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            prioritizedTasks.remove(removed);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(prioritizedTasks::remove);
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
        Epic newEpic = new Epic(nextId++, epic.getName(), epic.getDescription());
        epics.put(newEpic.getId(), newEpic);
        return newEpic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existing = epics.get(epic.getId());
            Epic updated = new Epic(epic.getId(), epic.getName(), epic.getDescription());
            existing.getSubtaskIds().forEach(updated::addSubtaskId);
            epics.put(updated.getId(), updated);
            updateEpicStatus(updated);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subtaskId -> {
                prioritizedTasks.removeIf(t -> t.getId() == subtaskId);
                historyManager.remove(subtaskId);
                subtasks.remove(subtaskId);
            });
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(prioritizedTasks::remove);
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
        if (subtask.getEpicId() == 0 || !epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Epic not found: " + subtask.getEpicId());
        }

        Subtask newSubtask = new Subtask(nextId++, subtask.getName(), subtask.getDescription(),
                subtask.getStatus(), subtask.getDuration(),
                subtask.getStartTime(), subtask.getEpicId());

        checkOverlaps(newSubtask);
        subtasks.put(newSubtask.getId(), newSubtask);
        epics.get(newSubtask.getEpicId()).addSubtaskId(newSubtask.getId());
        updateEpicStatus(epics.get(newSubtask.getEpicId()));
        addToPrioritized(newSubtask);
        return newSubtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask existing = subtasks.get(subtask.getId());
            Subtask updated = new Subtask(subtask.getId(), subtask.getName(),
                    subtask.getDescription(), subtask.getStatus(),
                    subtask.getDuration(), subtask.getStartTime(),
                    existing.getEpicId());

            prioritizedTasks.remove(existing);
            checkOverlaps(updated);
            subtasks.put(updated.getId(), updated);
            updateEpicStatus(epics.get(updated.getEpicId()));
            addToPrioritized(updated);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        if (!epics.containsKey(epicId)) return Collections.emptyList();
        return epics.get(epicId).getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;

        List<Subtask> subs = getSubtasksByEpicId(epic.getId());
        if (subs.isEmpty()) {
            epic.setStatus(Status.NEW);
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        boolean allNew = subs.stream().allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = subs.stream().allMatch(s -> s.getStatus() == Status.DONE);

        Duration totalDuration = subs.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        LocalDateTime earliestStart = subs.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestEnd = subs.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }

        epic.setDuration(totalDuration);
        epic.setStartTime(earliestStart);
        epic.setEndTime(latestEnd);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void addToPrioritized(Task task) {
        if (task.getStartTime() != null && task.getType() != TaskType.EPIC) {
            prioritizedTasks.add(task);
        }
    }

    protected boolean isOverlapping(Task a, Task b) {
        if (a.getStartTime() == null || b.getStartTime() == null) return false;
        return !a.getEndTime().isBefore(b.getStartTime()) &&
                !a.getStartTime().isAfter(b.getEndTime());
    }

    protected void checkOverlaps(Task newTask) {
        if (newTask.getStartTime() == null) return;

        boolean hasOverlap = prioritizedTasks.stream()
                .anyMatch(existing -> existing.getId() != newTask.getId()
                        && isOverlapping(newTask, existing));

        if (hasOverlap) {
            throw new IllegalArgumentException(
                    "Task overlaps with existing task");
        }
    }
}