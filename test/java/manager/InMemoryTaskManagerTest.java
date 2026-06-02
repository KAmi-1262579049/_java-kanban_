package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Класс тестирования InMemoryTaskManager
class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    // Переопределение метода создания менеджера задач
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    // Тест проверки создания и получения обычной задачи
    @Test
    void shouldCreateAndRetrieveTask() {
        TaskManager taskManager = createTaskManager();
        Task createdTask = taskManager.createTask(new Task("Test Task", "Test Description"));

        assertEquals("Test Task", createdTask.getName());
        assertEquals(TaskStatus.NEW, createdTask.getStatus());
        assertEquals(createdTask, taskManager.getTaskById(createdTask.getId()));
    }

    // Тест проверки невозможности создания подзадачи для несуществующего эпика
    @Test
    void shouldThrowExceptionWhenEpicDoesNotExist() {
        TaskManager taskManager = createTaskManager();
        Subtask subtask = new Subtask("Subtask", "Description", 999);

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubtask(subtask));
    }

    // Тест проверки удаления эпика вместе со всеми его подзадачами
    @Test
    void shouldDeleteEpicWithSubtasks() {
        TaskManager taskManager = createTaskManager();
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));

        taskManager.deleteEpicById(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()));
        assertNull(taskManager.getSubtaskById(subtask.getId()));
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    // Тест проверки отсутствия дубликатов в истории просмотров
    @Test
    void shouldNotContainDuplicatesInHistory() {
        TaskManager taskManager = createTaskManager();
        Task task = taskManager.createTask(new Task("Task", "Description"));

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size());
    }
}
