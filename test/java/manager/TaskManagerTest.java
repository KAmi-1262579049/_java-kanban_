package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Абстрактный базовый класс тестов для всех реализаций TaskManager
abstract class TaskManagerTest<T extends TaskManager> {
    // Абстрактный метод создания конкретной реализации менеджера
    protected abstract T createTaskManager();

    // Тест создания и поиска задачи
    @Test
    void shouldCreateAndFindTask() {
        T manager = createTaskManager();
        Task task = manager.createTask(new Task("Task", "Description"));

        Task foundTask = manager.getTaskById(task.getId());

        assertNotNull(foundTask);
        assertEquals(task.getId(), foundTask.getId());
        assertEquals("Task", foundTask.getName());
    }

    // Тест создания подзадачи и связи с эпиком
    @Test
    void shouldCreateSubtaskAndLinkItWithEpic() {
        T manager = createTaskManager();
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));

        List<Subtask> epicSubtasks = manager.getSubtasksByEpicId(epic.getId());

        assertEquals(1, epicSubtasks.size());
        assertEquals(subtask.getId(), epicSubtasks.get(0).getId());
        assertEquals(epic.getId(), epicSubtasks.get(0).getEpicId());
    }

    // Тест сортировки задач по приоритету
    @Test
    void shouldReturnTasksInPriorityOrder() {
        T manager = createTaskManager();
        Task laterTask = manager.createTask(new Task(
                "Later", "Description", Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 12, 0)));
        Task taskWithoutStart = manager.createTask(new Task("Without start", "Description"));
        Task earlierTask = manager.createTask(new Task(
                "Earlier", "Description", Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 10, 0)));

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size());
        assertEquals(earlierTask.getId(), prioritizedTasks.get(0).getId());
        assertEquals(laterTask.getId(), prioritizedTasks.get(1).getId());
        assertFalse(prioritizedTasks.contains(taskWithoutStart));
    }

    // Тест выбрасывания исключения при пересечении задач
    @Test
    void shouldThrowExceptionWhenTaskTimeIntersects() {
        T manager = createTaskManager();
        manager.createTask(new Task(
                "Task 1", "Description", Duration.ofMinutes(60), LocalDateTime.of(2026, 1, 1, 10, 0)));

        Task intersectedTask = new Task(
                "Task 2", "Description", Duration.ofMinutes(60), LocalDateTime.of(2026, 1, 1, 10, 30));

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(intersectedTask));
    }

    // Тест успешного создания задачи без пересечения
    @Test
    void shouldAllowTaskWhenTimeDoesNotIntersect() {
        T manager = createTaskManager();
        manager.createTask(new Task(
                "Task 1", "Description", Duration.ofMinutes(60), LocalDateTime.of(2026, 1, 1, 10, 0)));

        assertDoesNotThrow(() -> manager.createTask(new Task(
                "Task 2", "Description", Duration.ofMinutes(60), LocalDateTime.of(2026, 1, 1, 11, 0))));
    }

    // Тест статуса эпика, если все подзадачи NEW
    @Test
    void shouldCalculateEpicStatusWhenAllSubtasksAreNew() {
        T manager = createTaskManager();
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        manager.createSubtask(new Subtask("Subtask 1", "Description", epic.getId()));
        manager.createSubtask(new Subtask("Subtask 2", "Description", epic.getId()));

        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    // Тест статуса эпика, если все подзадачи DONE
    @Test
    void shouldCalculateEpicStatusWhenAllSubtasksAreDone() {
        T manager = createTaskManager();
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description", epic.getId()));
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    // Тест статуса эпика при наличии NEW и DONE
    @Test
    void shouldCalculateEpicStatusWhenSubtasksAreNewAndDone() {
        T manager = createTaskManager();
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        manager.createSubtask(new Subtask("Subtask 1", "Description", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description", epic.getId()));
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    // Тест статуса эпика при наличии IN_PROGRESS
    @Test
    void shouldCalculateEpicStatusWhenSubtaskIsInProgress() {
        T manager = createTaskManager();
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    // Тест расчёта времени эпика по подзадачам
    @Test
    void shouldCalculateEpicTimeFromSubtasks() {
        T manager = createTaskManager();
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        manager.createSubtask(new Subtask(
                "Subtask 1", "Description", epic.getId(), Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 10, 0)));
        manager.createSubtask(new Subtask(
                "Subtask 2", "Description", epic.getId(), Duration.ofMinutes(45), LocalDateTime.of(2026, 1, 1, 12, 0)));

        Epic savedEpic = manager.getEpicById(epic.getId());

        assertEquals(Duration.ofMinutes(75), savedEpic.getDuration());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), savedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 45), savedEpic.getEndTime());
    }
}
