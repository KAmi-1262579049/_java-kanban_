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

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldLoadFromEmptyFile() throws IOException {
        File file = tempDir.resolve("empty.csv").toFile();
        assertTrue(file.createNewFile());

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveEmptyManagerAfterDeletingAllTasks() {
        File file = tempDir.resolve("empty-saved.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        manager.deleteAllTasks();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveSeveralTasks() throws IOException {
        File file = tempDir.resolve("tasks.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Task", "Description"));
        Epic epic = manager.createEpic(new Epic("Epic", "Epic description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Subtask description", epic.getId()));

        String content = Files.readString(file.toPath());

        assertTrue(content.contains(task.getId() + ",TASK,Task,NEW,Description,"));
        assertTrue(content.contains(epic.getId() + ",EPIC,Epic,NEW,Epic description,"));
        assertTrue(content.contains(subtask.getId() + ",SUBTASK,Subtask,NEW,Subtask description," + epic.getId()));
    }

    @Test
    void shouldLoadSeveralTasksWithSavedIdsAndEpicSubtasks() {
        File file = tempDir.resolve("loaded.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Task", "Description"));
        Epic epic = manager.createEpic(new Epic("Epic", "Epic description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Subtask description", epic.getId()));
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertNotNull(loadedManager.getTaskById(task.getId()));
        assertNotNull(loadedManager.getEpicById(epic.getId()));
        assertNotNull(loadedManager.getSubtaskById(subtask.getId()));
        assertEquals(1, loadedManager.getSubtasksByEpicId(epic.getId()).size());
        assertEquals(subtask.getId(), loadedManager.getSubtasksByEpicId(epic.getId()).get(0).getId());
        assertEquals(TaskStatus.DONE, loadedManager.getEpicById(epic.getId()).getStatus());
    }

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

    @Test
    void shouldSaveEpicUpdate() {
        File file = tempDir.resolve("update-epic.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Epic epic = manager.createEpic(new Epic("Old epic", "Old description"));

        manager.updateEpic(new Epic(epic.getId(), "New epic", "New description"));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEquals("New epic", loadedEpic.getName());
        assertEquals("New description", loadedEpic.getDescription());
    }

    @Test
    void shouldSaveDeletingAllSubtasks() {
        File file = tempDir.resolve("delete-subtasks.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        manager.createSubtask(new Subtask("Subtask 1", "Description", epic.getId()));
        manager.createSubtask(new Subtask("Subtask 2", "Description", epic.getId()));

        manager.deleteAllSubtasks();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
        assertTrue(loadedManager.getSubtasksByEpicId(epic.getId()).isEmpty());
        assertEquals(TaskStatus.NEW, loadedManager.getEpicById(epic.getId()).getStatus());
    }
}
