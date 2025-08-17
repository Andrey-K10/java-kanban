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
    protected final HistoryManager historyManager;

    // Хранилище задач по приоритету startTime
    private final Comparator<Task> priorityComparator = (a, b) -> {
        if (a.getStartTime() == null && b.getStartTime() == null) {
            return Integer.compare(a.getId(), b.getId());
        }
        if (a.getStartTime() == null) return 1;
        if (b.getStartTime() == null) return -1;
        int cmp = a.getStartTime().compareTo(b.getStartTime());
        if (cmp != 0) return cmp;
        return Integer.compare(a.getId(), b.getId());
    };
    private final NavigableSet<Task> prioritized = new TreeSet<>(priorityComparator);

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

        @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        // убираем из истории
        tasks.keySet().forEach(historyManager::remove);
        // убираем из приоритета
        prioritized.removeIf(t -> t.getType() == TaskType.TASK);
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
                task.getStatus(),
                task.getDuration(),
                task.getStartTime()
        );
        // проверка пересечений
        checkOverlaps(newTask);
        tasks.put(newTask.getId(), newTask);
        addToPrioritized(newTask);
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task updatedTask = new Task(
                    task.getId(),
                    task.getName(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getDuration(),
                    task.getStartTime()
            );
            prioritized.removeIf(t -> t.getId() == updatedTask.getId());
            checkOverlaps(updatedTask);
            tasks.put(updatedTask.getId(), updatedTask);
            addToPrioritized(updatedTask);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            prioritized.removeIf(t -> t.getId() == id);
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
        // удалить все сабтаски и их из приоритета
        subtasks.keySet().forEach(historyManager::remove);
        prioritized.removeIf(t -> t.getType() == TaskType.SUBTASK);
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
        // duration/start/end пересчитаются при появлении подзадач
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
            existingEpic.getSubtaskIds().forEach(updatedEpic::addSubtaskId);
            epics.put(updatedEpic.getId(), updatedEpic);
            updateEpicStatus(updatedEpic); // в т.ч. время
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subtaskId -> {
                subtasks.remove(subtaskId);
                prioritized.removeIf(t -> t.getId() == subtaskId);
                historyManager.remove(subtaskId);
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
        prioritized.removeIf(t -> t.getType() == TaskType.SUBTASK);
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
                subtask.getDuration(),
                subtask.getStartTime(),
                subtask.getEpicId()
        );
        // проверка пересечений
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
            Subtask existingSubtask = subtasks.get(subtask.getId());
            Subtask updatedSubtask = new Subtask(
                    subtask.getId(),
                    subtask.getName(),
                    subtask.getDescription(),
                    subtask.getStatus(),
                    subtask.getDuration(),
                    subtask.getStartTime(),
                    existingSubtask.getEpicId()
            );
            prioritized.removeIf(t -> t.getId() == updatedSubtask.getId());
            checkOverlaps(updatedSubtask);
            subtasks.put(updatedSubtask.getId(), updatedSubtask);
            updateEpicStatus(epics.get(updatedSubtask.getEpicId()));
            addToPrioritized(updatedSubtask);
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
            prioritized.removeIf(t -> t.getId() == id);
            historyManager.remove(id);
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
        if (epic == null) return;

               if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            boolean allDone = true;
            boolean allNew = true;

            for (int subtaskId : epic.getSubtaskIds()) {
                Status status = subtasks.get(subtaskId).getStatus();
                if (status != Status.DONE) allDone = false;
                if (status != Status.NEW) allNew = false;
            }

            if (allDone) epic.setStatus(Status.DONE);
            else if (allNew) epic.setStatus(Status.NEW);
            else epic.setStatus(Status.IN_PROGRESS);
        }

        List<Subtask> subs = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (subs.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        long totalMinutes = subs.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();

        LocalDateTime minStart = subs.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime maxEnd = subs.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setDuration(Duration.ofMinutes(totalMinutes));
        epic.setStartTime(minStart);
        epic.setEndTime(maxEnd);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritized);
    }

    private void addToPrioritized(Task t) {
        if (t.getStartTime() != null && t.getType() != TaskType.EPIC) {
            prioritized.add(t);
        }
    }

        private boolean isOverlapping(Task a, Task b) {
        if (a == null || b == null) return false;
        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;
        // Пересечение отрезков: aStart < bEnd && bStart < aEnd
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

        private void checkOverlaps(Task candidate) {
        if (candidate.getStartTime() == null || candidate.getEndTime() == null) return;
        boolean intersects = prioritized.stream()
                .filter(t -> t.getId() != candidate.getId())
                .anyMatch(t -> isOverlapping(candidate, t));
        if (intersects) {
            throw new IllegalArgumentException("Задача пересекается по времени: " + candidate);
        }
    }
}
