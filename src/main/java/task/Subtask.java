package task;

// Класс Subtask, представляющий подзадачу в системе трекера задач
public class Subtask extends Task {
    private int epicId; // Поле для хранения идентификатора эпика

    // Конструктор для создания новой подзадачи без указания id
    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    // Конструктор для создания подзадачи с известным id
    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    // Геттер для поля epicId
    public int getEpicId() {
        return epicId;
    }

    // Сеттер для поля epicId
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    // Переопределение метода toString()
    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", epicId=" + epicId +
                '}';
    }
}
