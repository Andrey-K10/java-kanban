package Сontrollers;

import Model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task); // Добавление задачи в историю
    List<Task> getHistory(); // Получение истории просмотров
}