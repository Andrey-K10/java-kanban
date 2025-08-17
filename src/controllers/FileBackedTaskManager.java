package controllers;

import model.*;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            if (!file.exists() || Files.size(file.toPath()) == 0) {
                return manager;
            }

            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) {
                return manager;
            }

            // Первый проход - создаем все задачи
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                Task task = fromString(line);
                if (task == null) continue;

                if (task.getId() >= manager.nextId) {
                    manager.nextId = task.getId() + 1;
                }

                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        break;
                }
            }

            // Второй проход - устанавливаем связи
            for (Subtask subtask : manager.subtasks.values()) {
                if (manager.epics.containsKey(subtask.getEpicId())) {
                    manager.epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
                    manager.addToPrioritized(subtask);
                }
            }

            // Восстанавливаем prioritizedTasks для обычных задач
            for (Task task : manager.tasks.values()) {
                manager.addToPrioritized(task);
            }

            // Обновляем статусы эпиков
            manager.epics.values().forEach(manager::updateEpicStatus);

        } catch (IOException e) {
            throw new ManagerSaveException("Error loading from file", e);
        }
        return manager;
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,duration,startTime,epic\n");

            // Сохраняем задачи
            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Error saving to file", e);
        }
    }

    private String toString(Task task) {
        String epicId = task instanceof Subtask ?
                String.valueOf(((Subtask) task).getEpicId()) : "";

        String duration = task.getDuration() != null ?
                String.valueOf(task.getDuration().toMinutes()) : "";

        String startTime = task.getStartTime() != null ?
                task.getStartTime().toString() : "";

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                escape(task.getName()),
                task.getStatus().name(),
                escape(task.getDescription()),
                duration,
                startTime,
                epicId
        );
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace(",", "\\,").replace("\n", "\\n");
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = unescape(parts[2]);
            Status status = Status.valueOf(parts[3]);
            String description = unescape(parts[4]);

            Duration duration = parts[5].isEmpty() ?
                    null : Duration.ofMinutes(Long.parseLong(parts[5]));

            LocalDateTime startTime = parts[6].isEmpty() ?
                    null : LocalDateTime.parse(parts[6]);

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status, duration, startTime);
                case EPIC:
                    return new Epic(id, name, description);
                case SUBTASK:
                    int epicId = parts[7].isEmpty() ? 0 : Integer.parseInt(parts[7]);
                    return new Subtask(id, name, description, status, duration, startTime, epicId);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static String unescape(String value) {
        return value.replace("\\,", ",").replace("\\n", "\n");
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