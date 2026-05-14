package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Тестовый класс для проверки функциональности TaskManager
class TaskManagerTest {

    // Экземпляр менеджера задач для тестирования
    private TaskManager taskManager;

    // Инициализация менеджера перед каждым тестом
    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    // Проверка создания задачи и поиска по id
    @Test
    void shouldCreateAndFindTask() {
        Task task = taskManager.createTask(new Task("Task", "Description"));

        Task foundTask = taskManager.getTaskById(task.getId());

        assertNotNull(foundTask);
        assertEquals(task.getId(), foundTask.getId());
        assertEquals("Task", foundTask.getName());
    }

    // Проверка сохранения задачи в истории просмотров
    @Test
    void shouldAddTaskToHistory() {
        Task task = taskManager.createTask(new Task("Task", "Description"));

        taskManager.getTaskById(task.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }

    // Проверка отсутствия дубликатов в истории просмотров
    @Test
    void shouldNotKeepDuplicatesInHistory() {
        Task task = taskManager.createTask(new Task("Task", "Description"));

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }
}
