package manager;

import task.Task;
import task.TaskStatus;
import task.Epic;
import task.Subtask;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Класс менеджера задач с хранением в оперативной памяти
public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks; // Хранилище обычных задач
    private final Map<Integer, Epic> epics; // Хранилище эпиков
    private final Map<Integer, Subtask> subtasks; // Хранилище подзадач
    private final HistoryManager historyManager; // Менеджер истории просмотров задач
    private int nextId; // Счетчик для генерации уникальных идентификаторов задач

    // Конструктор класса
    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.nextId = 1;
    }

    // Приватный метод для генерации нового уникального id
    private int generateId() {
        return nextId++;
    }

    // Получение списка всех обычных задач
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Удаление всех обычных задач
    @Override
    public void deleteAllTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    // Получение задачи по ее id
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    // Создание новой обычной задачи
    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task;
    }

    // Обновление существующей задачи
    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }
        tasks.put(task.getId(), task);
    }

    // Удаление задачи по id
    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    // Получение списка всех эпиков
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Удаление всех эпиков и их подзадач
    @Override
    public void deleteAllEpics() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();

        for (Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }
        epics.clear();
    }

    // Получение эпика по id
    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    // Создание нового эпика
    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не может быть null");
        }
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic;
    }


    // Удаление эпика по id
    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }

        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }

        epics.remove(id);
        historyManager.remove(id);
    }

    // Получение списка всех подзадач
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }


    // Получение подзадачи по id
    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Создание новой подзадачи
    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не может быть null");
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id=" + subtask.getEpicId() + " не найден");
        }

        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic.getId());
        return subtask;
    }

    // Обновление существующей подзадачи
    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return;
        }

        int epicId = subtask.getEpicId();

        if (!epics.containsKey(epicId)) {
            return;
        }

        if (subtask.getEpicId() == subtask.getId()) {
            return;
        }

        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epicId);
    }

    // Получение всех подзадач определенного эпика
    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    // Получение истории просмотров задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }

            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
