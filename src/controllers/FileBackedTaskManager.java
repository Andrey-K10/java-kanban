package controllers;

import model.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String CSV_HEADER = "id,type,name,status,description,epic";

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.loadFromFile();
        return manager;
    }

    private void loadFromFile() {
        if (!file.exists()) return;

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                Task task = parseTaskFromString(lines[i]);
                if (task != null) {
                    restoreTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
    }

    private void restoreTask(Task task) {
        if (task instanceof Epic) {
            epics.put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            subtasks.put(task.getId(), (Subtask) task);
            Epic epic = epics.get(((Subtask) task).getEpicId());
            if (epic != null) epic.addSubtaskId(task.getId());
        } else {
            tasks.put(task.getId(), task);
        }

        if (task.getId() >= nextId) {
            nextId = task.getId() + 1;
        }
    }

    private Task parseTaskFromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK: return new Task(id, name, description, status);
            case EPIC:
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = fields.length > 5 ? Integer.parseInt(fields[5]) : 0;
                return new Subtask(id, name, description, status, epicId);
            default: return null;
        }
    }

    protected void save() {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println(CSV_HEADER);
            saveTasks(writer, new ArrayList<>(tasks.values()));
            saveTasks(writer, new ArrayList<>(epics.values()));
            saveTasks(writer, new ArrayList<>(subtasks.values()));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private void saveTasks(PrintWriter writer, List<? extends Task> tasks) {
        tasks.forEach(task -> writer.println(convertTaskToString(task)));
    }

    private String convertTaskToString(Task task) {
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return String.join(",",
                    String.valueOf(subtask.getId()),
                    TaskType.SUBTASK.name(),
                    subtask.getName(),
                    subtask.getStatus().name(),
                    subtask.getDescription(),
                    String.valueOf(subtask.getEpicId())
            );
        } else if (task instanceof Epic) {
            return String.join(",",
                    String.valueOf(task.getId()),
                    TaskType.EPIC.name(),
                    task.getName(),
                    task.getStatus().name(),
                    task.getDescription(),
                    ""
            );
        } else {
            return String.join(",",
                    String.valueOf(task.getId()),
                    TaskType.TASK.name(),
                    task.getName(),
                    task.getStatus().name(),
                    task.getDescription(),
                    ""
            );
        }
    }

    @Override public int createTask(Task task) { int id = super.createTask(task); save(); return id; }
    @Override public void updateTask(Task task) { super.updateTask(task); save(); }
    @Override public void deleteTaskById(int id) { super.deleteTaskById(id); save(); }
    @Override public void deleteAllTasks() { super.deleteAllTasks(); save(); }
    @Override public int createEpic(Epic epic) { int id = super.createEpic(epic); save(); return id; }
    @Override public void updateEpic(Epic epic) { super.updateEpic(epic); save(); }
    @Override public void deleteEpicById(int id) { super.deleteEpicById(id); save(); }
    @Override public void deleteAllEpics() { super.deleteAllEpics(); save(); }
    @Override public int createSubtask(Subtask subtask) { int id = super.createSubtask(subtask); save(); return id; }
    @Override public void updateSubtask(Subtask subtask) { super.updateSubtask(subtask); save(); }
    @Override public void deleteSubtaskById(int id) { super.deleteSubtaskById(id); save(); }
    @Override public void deleteAllSubtasks() { super.deleteAllSubtasks(); save(); }
}