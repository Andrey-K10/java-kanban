package controllers;

public class Managers {

    // Метод для получения реализации TaskManager
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    // Метод для получения реализации HistoryManager
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
