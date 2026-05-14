package task;

import java.util.Objects;

// Класс, представляющий задачу
public class Task {
    protected int id; // Поле для хранения уникального идентификатора задачи
    protected String name; // Поле для хранения названия задачи
    protected String description; // Поле для хранения подробного описания задачи
    protected TaskStatus status; // Поле для хранения текущего статуса задачи
    protected TaskType type; // Поле для хранения типа задачи

    // Конструктор для создания новой задачи без указания идентификатора
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.type = TaskType.TASK;
    }

    // Конструктор для создания задачи с известным идентификатором
    public Task(int id, String name, String description, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.type = TaskType.TASK;
    }

    // Геттер для поля id
    public int getId() {
        return id;
    }

    // Сеттер для поля id
    public void setId(int id) {
        this.id = id;
    }

    // Геттер для поля name
    public String getName() {
        return name;
    }

    // Сеттер для поля name
    public void setName(String name) {
        this.name = name;
    }

    // Геттер для поля description
    public String getDescription() {
        return description;
    }

    // Сеттер для поля description
    public void setDescription(String description) {
        this.description = description;
    }

    // Геттер для поля status
    public TaskStatus getStatus() {
        return status;
    }

    // Сеттер для поля status
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    // Геттер для поля type
    public TaskType getType() {
        return type;
    }

    // Сеттер для поля type
    public void setType(TaskType type) {
        this.type = type;
    }

    // Переопределение метода equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    // Переопределение метода hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Переопределение метода toString()
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                '}';
    }
}
