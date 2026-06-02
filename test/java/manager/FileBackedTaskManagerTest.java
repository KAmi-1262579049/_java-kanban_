package manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// Тестовый класс для FileBackedTaskManager
class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @TempDir
    Path tempDir;

    // Создание экземпляра менеджера для базовых тестов
    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(tempDir.resolve("tasks.csv").toFile());
    }

    // Тест загрузки пустого файла
    @Test
    void shouldLoadFromEmptyFile() throws IOException {
        File file = tempDir.resolve("empty.csv").toFile();
        assertTrue(file.createNewFile());

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    // Тест сохранения задач с временными полями
    @Test
    void shouldSaveSeveralTasksWithTimeFields() throws IOException {
        File file = tempDir.resolve("tasks-with-time.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task = manager.createTask(new Task(
                "Task", "Description", Duration.ofMinutes(15), LocalDateTime.of(2026, 1, 1, 10, 0)));
        Epic epic = manager.createEpic(new Epic("Epic", "Epic description"));
        Subtask subtask = manager.createSubtask(new Subtask(
                "Subtask", "Subtask description", epic.getId(), Duration.ofMinutes(30), LocalDateTime.of(2026, 1, 1, 11, 0)));

        String content = Files.readString(file.toPath());

        assertTrue(content.contains("duration,startTime"));
        assertTrue(content.contains(task.getId() + ",TASK,Task,NEW,Description,,15,2026-01-01T10:00"));
        assertTrue(content.contains(subtask.getId() + ",SUBTASK,Subtask,NEW,Subtask description," + epic.getId()
                + ",30,2026-01-01T11:00"));
    }

    // Тест восстановления задач из файла
    @Test
    void shouldLoadSeveralTasksWithSavedIdsEpicSubtasksAndTimeFields() {
        File file = tempDir.resolve("loaded.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task = manager.createTask(new Task(
                "Task", "Description", Duration.ofMinutes(20), LocalDateTime.of(2026, 1, 1, 9, 0)));
        Epic epic = manager.createEpic(new Epic("Epic", "Epic description"));
        Subtask subtask = manager.createSubtask(new Subtask(
                "Subtask", "Subtask description", epic.getId(), Duration.ofMinutes(35), LocalDateTime.of(2026, 1, 1, 10, 0)));
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertNotNull(loadedManager.getTaskById(task.getId()));
        assertNotNull(loadedManager.getEpicById(epic.getId()));
        assertNotNull(loadedManager.getSubtaskById(subtask.getId()));
        assertEquals(1, loadedManager.getSubtasksByEpicId(epic.getId()).size());
        assertEquals(TaskStatus.DONE, loadedManager.getEpicById(epic.getId()).getStatus());
        assertEquals(Duration.ofMinutes(35), loadedManager.getEpicById(epic.getId()).getDuration());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 35), loadedManager.getEpicById(epic.getId()).getEndTime());
    }

    // Тест корректного продолжения счётчика id после загрузки
    @Test
    void shouldContinueIdCounterAfterLoadingFromFile() {
        File file = tempDir.resolve("next-id.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task firstTask = manager.createTask(new Task("Task", "Description"));
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task nextTask = loadedManager.createTask(new Task("Next task", "Description"));

        int maxLoadedId = Math.max(firstTask.getId(), Math.max(epic.getId(), subtask.getId()));
        assertEquals(maxLoadedId + 1, nextTask.getId());
    }

    // Тест выброса исключения при невозможности записи файла
    @Test
    void shouldThrowManagerSaveExceptionWhenFileCannotBeWritten() {
        File directoryInsteadOfFile = tempDir.toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(directoryInsteadOfFile);

        assertThrows(ManagerSaveException.class, () -> manager.createTask(new Task("Task", "Description")));
    }

    // Тест успешной записи в файл без исключений
    @Test
    void shouldNotThrowWhenFileCanBeWritten() {
        File file = tempDir.resolve("normal.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        assertDoesNotThrow(() -> manager.createTask(new Task("Task", "Description")));
    }
}
