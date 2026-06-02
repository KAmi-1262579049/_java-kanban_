package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected int id; // Уникальный идентификатор задачи
    protected String name; // Название задачи
    protected String description; // Описание задачи
    protected TaskStatus status; // Текущий статус задачи
    protected TaskType type; // Тип задачи
    protected Duration duration; // Продолжительность выполнения задачи
    protected LocalDateTime startTime; // Время начала выполнения задачи

    // Конструктор без указания времени начала и продолжительности
    public Task(String name, String description) {
        this(name, description, Duration.ZERO, null);
    }

    // Основной конструктор для создания новой задачи
    public Task(String name, String description, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.type = TaskType.TASK;
        this.duration = duration == null ? Duration.ZERO : duration;
        this.startTime = startTime;
    }

    // Конструктор с id и статусом
    public Task(int id, String name, String description, TaskStatus status) {
        this(id, name, description, status, Duration.ZERO, null);
    }

    // Полный конструктор со всеми параметрами
    public Task(int id, String name, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.type = TaskType.TASK;
        this.duration = duration == null ? Duration.ZERO : duration;
        this.startTime = startTime;
    }

    // Возвращает id задачи
    public int getId() {
        return id;
    }

    // Устанавливает id задачи
    public void setId(int id) {
        this.id = id;
    }

    // Возвращает название задачи
    public String getName() {
        return name;
    }

    // Изменяет название задачи
    public void setName(String name) {
        this.name = name;
    }

    // Возвращает описание задачи
    public String getDescription() {
        return description;
    }

    // Изменяет описание задачи
    public void setDescription(String description) {
        this.description = description;
    }

    // Возвращает статус задачи
    public TaskStatus getStatus() {
        return status;
    }

    // Изменяет статус задачи
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    // Возвращает тип задачи
    public TaskType getType() {
        return type;
    }

    // Изменяет тип задачи
    public void setType(TaskType type) {
        this.type = type;
    }

    // Возвращает продолжительность задачи
    public Duration getDuration() {
        return duration;
    }

    // Устанавливает продолжительность задачи
    public void setDuration(Duration duration) {
        this.duration = duration == null ? Duration.ZERO : duration;
    }

    // Возвращает время начала задачи
    public LocalDateTime getStartTime() {
        return startTime;
    }

    // Устанавливает время начала задачи
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    // Возвращает время окончания задачи
    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    // Переопределение метода сравнения объектов
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    // Переопределение hashCode
    @Override
    public int hashCode() { return Objects.hash(id); }

    // Переопределение метода toString()
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}
