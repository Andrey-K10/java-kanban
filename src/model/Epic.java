package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds; // id всех подзадач в эпике

    // конструктор эпика
    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    //геттеры и сеттеры(add)
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    // удаление ид задачи из списка
    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    // приводим к одной строке все данные эпика
    @Override
    public String toString() {
        return "Model.Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}

