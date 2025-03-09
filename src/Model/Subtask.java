package Model;

public class Subtask extends Task {
    private int epicId; // переменная с одним единственным эпиком

    // конструктор для подзадачи
    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    // геттеры и сеттеры
    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    // приводим к одной стркое все данные по подзадаче
    @Override
    public String toString() {
        return "Model.Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}


