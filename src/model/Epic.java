package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }
}