package controllers;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();
        return manager;
    }

    private void load() {
        try {
            if (!file.exists() || Files.size(file.toPath()) == 0) {
                return;
            }
            String content = Files.readString(file.toPath());
            if (content.isEmpty()) {
                return;
            }

            String[] lines = content.split("\n");
            // первая строка — заголовок
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                Task task = fromString(line);
                if (task != null) {
                    switch (task.getType()) {
                        case TASK:
                            createTask(task);
                            break;
                        case EPIC:
                            createEpic((Epic) task);
                            break;
                        case SUBTASK:
                            createSubtask((Subtask) task);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error reading file", e);
        }
    }

    protected void save() {
        try (Writer writer = new FileWriter(file)) {
            // duration и startTime
            writer.write("id,type,name,status,description,duration,startTime,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving to file", e);
        }
    }

    private static String toString(Task task) {
        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        String durationMinutes = task.getDuration() == null ? "" : String.valueOf(task.getDuration().toMinutes());
        String start = task.getStartTime() == null ? "" : task.getStartTime().toString();

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                escape(task.getName()),
                task.getStatus().name(),
                escape(task.getDescription()),
                durationMinutes,
                start,
                epicId
        );
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\n", " ").replace("\r", " ");
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",", -1); // сохраняем пустые хвосты
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts.length > 4 ? parts[4] : "";

        Duration duration = null;
        if (parts.length > 5 && !parts[5].isBlank()) {
            duration = Duration.ofMinutes(Long.parseLong(parts[5]));
        }
        LocalDateTime startTime = null;
        if (parts.length > 6 && !parts[6].isBlank()) {
            startTime = LocalDateTime.parse(parts[6]);
        }

        switch (type) {
            case TASK:
                return new Task(id, name, description, status, duration, startTime);
            case EPIC:
                // duration/startTime у эпика будут вычислены на основе подзадач
                return new Epic(id, name, description);
            case SUBTASK:
                int epicId = parts.length > 7 && !parts[7].isBlank() ? Integer.parseInt(parts[7]) : 0;
                return new Subtask(id, name, description, status, duration, startTime, epicId);
            default:
                return null;
        }
    }

        @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
