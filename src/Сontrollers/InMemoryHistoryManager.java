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


                history.remove(task);

                history.addLast(copyTask(task));

        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history); // Возвращаем копию списка
    }


    private Task copyTask(Task task) {
        return new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus());
    }
}