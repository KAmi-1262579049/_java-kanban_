package task;

import java.time.Duration;
import java.time.LocalDateTime;

// Класс Subtask наследуется от класса Task
public class Subtask extends Task {
    private int epicId; // Идентификатор эпика, к которому относится подзадача

    // Конструктор создания подзадачи без времени начала и продолжительности
    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    // Конструктор создания подзадачи с продолжительностью и временем начала
    public Subtask(String name, String description, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    // Конструктор для восстановления подзадачи с уже существующим id
    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    // Конструктор для восстановления подзадачи со всеми параметрами
    public Subtask(int id, String name, String description, TaskStatus status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
        this.type = TaskType.SUBTASK;
    }

    // Геттер для получения id эпика
    public int getEpicId() {
        return epicId;
    }

    // Сеттер для изменения id эпика
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
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", epicId=" + epicId +
                '}';
    }
}
