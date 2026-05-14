package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Тестовый класс для InMemoryTaskManager
class InMemoryTaskManagerTest {

    // Менеджер задач для тестирования
    private TaskManager taskManager;

    // Инициализация менеджера перед каждым тестом
    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    // Проверка создания и получения обычной задачи
    @Test
    void shouldCreateAndRetrieveTask() {
        Task task = new Task("Test Task", "Test Description");

        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask.getId());
        assertEquals("Test Task", createdTask.getName());
        assertEquals(TaskStatus.NEW, createdTask.getStatus());

        Task retrievedTask = taskManager.getTaskById(createdTask.getId());

        assertEquals(createdTask, retrievedTask);
    }

    // Проверка создания и получения эпика
    @Test
    void shouldCreateAndRetrieveEpic() {
        Epic epic = new Epic("Test Epic", "Test Description");

        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic.getId());
        assertEquals("Test Epic", createdEpic.getName());
        assertEquals(TaskStatus.NEW, createdEpic.getStatus());

        Epic retrievedEpic = taskManager.getEpicById(createdEpic.getId());

        assertEquals(createdEpic, retrievedEpic);
    }

    // Проверка создания подзадачи с корректным epicId
    @Test
    void shouldCreateSubtaskWithValidEpic() {
        Epic epic = taskManager.createEpic(
                new Epic("Epic", "Description")
        );

        Subtask subtask = new Subtask(
                "Subtask",
                "Description",
                epic.getId()
        );

        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask.getId());
        assertEquals(epic.getId(), createdSubtask.getEpicId());

        List<Subtask> epicSubtasks =
                taskManager.getSubtasksByEpicId(epic.getId());

        assertEquals(1, epicSubtasks.size());
        assertEquals(createdSubtask.getId(), epicSubtasks.get(0).getId());
    }

    // Проверка исключения при создании подзадачи с неверным epicId
    @Test
    void shouldThrowExceptionWhenEpicDoesNotExist() {
        Subtask subtask = new Subtask(
                "Subtask",
                "Description",
                999
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> taskManager.createSubtask(subtask)
        );
    }

    // Проверка пересчёта статуса эпика
    @Test
    void shouldCalculateEpicStatusCorrectly() {
        Epic epic = taskManager.createEpic(
                new Epic("Epic", "Description")
        );

        assertEquals(TaskStatus.NEW, epic.getStatus());

        Subtask subtask1 = taskManager.createSubtask(
                new Subtask(
                        "Subtask 1",
                        "Description",
                        epic.getId()
                )
        );

        Subtask subtask2 = taskManager.createSubtask(
                new Subtask(
                        "Subtask 2",
                        "Description",
                        epic.getId()
                )
        );

        assertEquals(
                TaskStatus.NEW,
                taskManager.getEpicById(epic.getId()).getStatus()
        );

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        assertEquals(
                TaskStatus.IN_PROGRESS,
                taskManager.getEpicById(epic.getId()).getStatus()
        );

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);

        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(
                TaskStatus.DONE,
                taskManager.getEpicById(epic.getId()).getStatus()
        );
    }

    // Проверка удаления задачи
    @Test
    void shouldDeleteTask() {
        Task task = taskManager.createTask(
                new Task("Task", "Description")
        );

        int taskId = task.getId();

        taskManager.deleteTaskById(taskId);

        assertNull(taskManager.getTaskById(taskId));
        assertEquals(0, taskManager.getAllTasks().size());
    }

    // Проверка удаления эпика вместе с подзадачами
    @Test
    void shouldDeleteEpicWithSubtasks() {
        Epic epic = taskManager.createEpic(
                new Epic("Epic", "Description")
        );

        Subtask subtask = taskManager.createSubtask(
                new Subtask(
                        "Subtask",
                        "Description",
                        epic.getId()
                )
        );

        taskManager.deleteEpicById(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()));
        assertNull(taskManager.getSubtaskById(subtask.getId()));

        assertEquals(0, taskManager.getAllEpics().size());
        assertEquals(0, taskManager.getAllSubtasks().size());
    }

    // Проверка получения всех задач
    @Test
    void shouldReturnAllTasks() {
        Task task1 = taskManager.createTask(
                new Task("Task 1", "Description")
        );

        Task task2 = taskManager.createTask(
                new Task("Task 2", "Description")
        );

        Epic epic = taskManager.createEpic(
                new Epic("Epic", "Description")
        );

        List<Task> tasks = taskManager.getAllTasks();
        List<Epic> epics = taskManager.getAllEpics();

        assertEquals(2, tasks.size());
        assertEquals(1, epics.size());

        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
        assertTrue(epics.contains(epic));
    }

    // Проверка истории просмотров
    @Test
    void shouldAddTasksToHistory() {
        Task task = taskManager.createTask(
                new Task("Task", "Description")
        );

        Epic epic = taskManager.createEpic(
                new Epic("Epic", "Description")
        );

        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(2, history.size());
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
    }

    // Проверка уникальности id
    @Test
    void shouldGenerateUniqueIds() {
        Task task = taskManager.createTask(
                new Task("Task", "Description")
        );

        Epic epic = taskManager.createEpic(
                new Epic("Epic", "Description")
        );

        assertNotEquals(task.getId(), epic.getId());

        assertNotNull(taskManager.getTaskById(task.getId()));
        assertNotNull(taskManager.getEpicById(epic.getId()));
    }

    // Проверка хранения задач по ссылке
    @Test
    void shouldStoreTaskByReference() {
        Task originalTask = new Task(
                "Original",
                "Description"
        );

        originalTask.setStatus(TaskStatus.IN_PROGRESS);

        Task createdTask = taskManager.createTask(originalTask);

        int taskId = createdTask.getId();

        Task taskFromManager = taskManager.getTaskById(taskId);

        taskFromManager.setStatus(TaskStatus.DONE);
        taskFromManager.setName("Changed");

        Task retrievedTask = taskManager.getTaskById(taskId);

        assertEquals(TaskStatus.DONE, retrievedTask.getStatus());
        assertEquals("Changed", retrievedTask.getName());
    }

    // Проверка обновления задачи
    @Test
    void shouldUpdateTask() {
        Task task = taskManager.createTask(
                new Task("Original", "Description")
        );

        Task updatedTask = new Task(
                task.getId(),
                "Updated",
                "New Description",
                TaskStatus.IN_PROGRESS
        );

        taskManager.updateTask(updatedTask);

        Task retrievedTask =
                taskManager.getTaskById(task.getId());

        assertEquals("Updated", retrievedTask.getName());
        assertEquals(
                "New Description",
                retrievedTask.getDescription()
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                retrievedTask.getStatus()
        );
    }

    // Проверка удаления всех задач
    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask(
                new Task("Task 1", "Description")
        );

        taskManager.createTask(
                new Task("Task 2", "Description")
        );

        assertEquals(2, taskManager.getAllTasks().size());

        taskManager.deleteAllTasks();

        assertEquals(0, taskManager.getAllTasks().size());
    }

    // Проверка удаления всех эпиков
    @Test
    void shouldDeleteAllEpics() {
        Epic epic1 = taskManager.createEpic(
                new Epic("Epic 1", "Description")
        );

        Epic epic2 = taskManager.createEpic(
                new Epic("Epic 2", "Description")
        );

        taskManager.createSubtask(
                new Subtask(
                        "Subtask 1",
                        "Description",
                        epic1.getId()
                )
        );

        taskManager.createSubtask(
                new Subtask(
                        "Subtask 2",
                        "Description",
                        epic2.getId()
                )
        );

        assertEquals(2, taskManager.getAllEpics().size());
        assertEquals(2, taskManager.getAllSubtasks().size());

        taskManager.deleteAllEpics();

        assertEquals(0, taskManager.getAllEpics().size());
        assertEquals(0, taskManager.getAllSubtasks().size());
    }

    // Проверка отсутствия дублей в истории
    @Test
    void shouldNotContainDuplicatesInHistory() {
        Task task = taskManager.createTask(
                new Task("Task", "Description")
        );

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(1, history.size());
    }
}
