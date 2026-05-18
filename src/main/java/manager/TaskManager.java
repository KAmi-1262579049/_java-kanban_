package manager;

import task.Epic;
import task.Subtask;
import task.Task;
import java.util.List;

// Интерфейс менеджера задач
public interface TaskManager {

    // Методы для обычных задач

    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    Task createTask(Task task);

    void updateTask(Task task);

    void deleteTaskById(int id);

    // Методы для эпиков

    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    Epic createEpic(Epic epic);

    void deleteEpicById(int id);

    // Методы для подзадач

    List<Subtask> getAllSubtasks();

    Subtask getSubtaskById(int id);

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    // Дополнительные методы

    List<Subtask> getSubtasksByEpicId(int epicId);

    List<Task> getHistory();
}
