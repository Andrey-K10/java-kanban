package Сontrollers;

import Model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10; // Максимальный размер истории
    private LinkedList<Task> history = new LinkedList<>(); // Список для хранения истории

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        // Удаляем задачу, если она уже есть в истории
        history.remove(task);

        // Добавляем задачу в конец списка
        history.addLast(task);

        // Если размер истории превышает максимальный, удаляем самый старый элемент
        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history); // Возвращаем копию списка
    }
}