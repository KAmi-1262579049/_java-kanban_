package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds; // Список идентификаторов подзадач, принадлежащих эпику
    private LocalDateTime endTime; // Время окончания эпика (рассчитывается по подзадачам)

    // Конструктор для создания нового эпика без id
    public Epic(String name, String description) {
        super(name, description);
        this.subtaskIds = new ArrayList<>();
        this.type = TaskType.EPIC;
        this.status = TaskStatus.NEW;
    }

    // Конструктор для создания эпика с известным id
    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
        this.subtaskIds = new ArrayList<>();
        this.type = TaskType.EPIC;
    }

    // Метод получения списка id подзадач
    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    // Метод добавления id подзадачи
    public void addSubtaskId(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    // Метод удаления id подзадачи
    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    // Метод удаления всех подзадач эпика
    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    // Метод вычисления времени продолжительности эпика
    public void setCalculatedTime(Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
        this.duration = duration == null ? Duration.ZERO : duration;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Метод установки продолжительности
    @Override
    public void setDuration(Duration duration) {
        throw new UnsupportedOperationException("Продолжительность эпика рассчитывается по подзадачам");
    }

    // Метод установки времени начала
    @Override
    public void setStartTime(LocalDateTime startTime) {
        throw new UnsupportedOperationException("Время начала эпика рассчитывается по подзадачам");
    }

    // Метод получения времени окончания эпика
    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Переопределение метода toString()
    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
