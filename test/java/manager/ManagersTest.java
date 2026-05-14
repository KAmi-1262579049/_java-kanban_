package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Task;
import task.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Тестовый класс для проверки работы Managers
class ManagersTest {

    // Проверка, что getDefault() возвращает готовый TaskManager
    @Test
    void shouldReturnInitializedTaskManager() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager,
                "Метод getDefault() должен возвращать инициализированный объект");

        assertDoesNotThrow(() -> {
            taskManager.getAllTasks();
            taskManager.getAllEpics();
            taskManager.getAllSubtasks();
        }, "Методы TaskManager не должны выбрасывать исключения");
    }

    // Проверка, что getDefaultHistory() возвращает готовый HistoryManager
    @Test
    void shouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager,
                "Метод getDefaultHistory() должен возвращать инициализированный объект");

        assertDoesNotThrow(historyManager::getHistory,
                "Методы HistoryManager не должны выбрасывать исключения");
    }

    // Проверка базовой работы TaskManager
    @Test
    void shouldManageTasksCorrectly() {
        TaskManager taskManager = Managers.getDefault();

        Task taskToCreate = new Task("Test Task", "Description");

        Task createdTask = taskManager.createTask(taskToCreate);

        assertNotNull(createdTask.getId(),
                "После создания задача должна получить id");

        Task retrievedTask = taskManager.getTaskById(createdTask.getId());

        assertEquals(createdTask, retrievedTask,
                "Созданная и полученная задача должны совпадать");

        assertNotNull(taskManager.getHistory(),
                "История просмотров не должна быть null");
    }

    // Проверка создания обычной задачи
    @Test
    void shouldCreateSimpleTask() {
        TaskManager taskManager = Managers.getDefault();

        Task task = new Task("Обычная задача", "Описание обычной задачи");

        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask.getId());

        assertEquals("Обычная задача", createdTask.getName());

        assertEquals(TaskStatus.NEW, createdTask.getStatus());

        assertEquals(1, taskManager.getAllTasks().size());

        assertTrue(taskManager.getAllTasks().contains(createdTask));
    }

    // Проверка создания эпика
    @Test
    void shouldCreateEpic() {
        TaskManager taskManager = Managers.getDefault();

        Epic epic = new Epic("Эпик", "Описание эпика");

        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic.getId());

        assertEquals("Эпик", createdEpic.getName());

        assertEquals(TaskStatus.NEW, createdEpic.getStatus());
    }
}
