package manager;

import task.Task;

import java.util.List;

// Интерфейс для управления историей просмотров задач
public interface HistoryManager {

    // Метод добавления задачи в историю
    void add(Task task);

    // Метод удаления задачи из истории по id
    void remove(int id);

    // Метод получения истории просмотров
    List<Task> getHistory();
}
