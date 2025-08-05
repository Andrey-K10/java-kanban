package controllers;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBackedManager(File file) {
        return new FileBackedTaskManager(file);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}