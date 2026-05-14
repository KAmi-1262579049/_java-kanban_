package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;
import task.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Тестовый класс для проверки HistoryManager
class HistoryManagerTest {

    // Экземпляр менеджера истории
    private HistoryManager historyManager;

    // Тестовая задача
    private Task testTask;

    // Выполняется перед каждым тестом
    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        testTask = new Task(1, "Task 1", "Description", TaskStatus.NEW);
    }

    // Проверка добавления задачи в историю
    @Test
    void testAddToHistory() {
        historyManager.add(testTask);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(testTask, history.get(0));
    }

    // Проверка добавления null в историю
    @Test
    void testAddNullTask() {
        historyManager.add(null);

        assertEquals(0, historyManager.getHistory().size());
    }

    // Проверка отсутствия дубликатов в истории
    @Test
    void testHistoryShouldNotContainDuplicates() {
        historyManager.add(testTask);
        historyManager.add(testTask);
        historyManager.add(testTask);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(testTask, history.get(0));
    }

    // Проверка удаления задачи из истории
    @Test
    void testRemoveFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    // Проверка удаления задачи из начала истории
    @Test
    void testRemoveFirstTaskFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    // Проверка удаления задачи из конца истории
    @Test
    void testRemoveLastTaskFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    // Проверка удаления задачи из середины истории
    @Test
    void testRemoveTaskFromMiddleOfHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);
        Task task3 = new Task(3, "Task 3", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }
}
