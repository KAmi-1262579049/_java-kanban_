package manager;

import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks; // Хранилище обычных задач
    private final Map<Integer, Epic> epics; // Хранилище эпиков
    private final Map<Integer, Subtask> subtasks; // Хранилище подзадач
    private final TreeSet<Task> prioritizedTasks; // Хранилище всех задач и подзадач в порядке их приоритета
    private final HistoryManager historyManager; // Менеджер истории просмотров задач
    private int nextId; // Счетчик для генерации уникальных идентификаторов задач

    // Конструктор класса
    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.prioritizedTasks = new TreeSet<>(Comparator
                .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Task::getId));
        this.historyManager = Managers.getDefaultHistory();
        this.nextId = 1;
    }

    // Генерирует новый уникальный id для задачи, эпика или подзадачи
    private int generateId() {
        return nextId++;
    }

    // Добавляет задачу с уже существующим id
    protected void addTaskWithId(Task task) {
        validateNoTimeIntersection(task);
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        updateNextId(task.getId());
    }

    // Добавляет эпик с уже существующим id
    protected void addEpicWithId(Epic epic) {
        epics.put(epic.getId(), epic);
        updateNextId(epic.getId());
    }

    // Добавляет подзадачу с уже существующим id
    protected void addSubtaskWithId(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id=" + subtask.getEpicId() + " не найден");
        }
        validateNoTimeIntersection(subtask);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        addToPrioritized(subtask);
        updateEpicStatusAndTime(epic.getId());
        updateNextId(subtask.getId());
    }

    // Обновляет счётчик nextId
    protected void updateNextId(int id) {
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    // Возвращает список всех обычных задач
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Удаляет все обычные задачи
    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().forEach(this::removeFromPrioritized);
        tasks.clear();
    }

    // Возвращает задачу по id
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    // Создаёт новую обычную задачу
    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не может быть null");
        }
        validateNoTimeIntersection(task);
        task.setId(generateId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    // Создаёт новую обычную задачу
    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return;
        }
        removeFromPrioritized(tasks.get(task.getId()));
        try {
            validateNoTimeIntersection(task);
            tasks.put(task.getId(), task);
            addToPrioritized(task);
        } catch (RuntimeException exception) {
            addToPrioritized(tasks.get(task.getId()));
            throw exception;
        }
    }

    // Удаляет задачу по id
    @Override
    public void deleteTaskById(int id) {
        Task removedTask = tasks.remove(id);
        if (removedTask != null) {
            removeFromPrioritized(removedTask);
            historyManager.remove(id);
        }
    }

    // Возвращает список всех эпиков
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Удаляет все эпики и все связанные с ними подзадачи
    @Override
    public void deleteAllEpics() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(this::removeFromPrioritized);
        subtasks.clear();
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
    }

    // Возвращает эпик по id
    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    // Создаёт новый эпик
    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не может быть null");
        }
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    // Обновляет название и описание эпика
    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return;
        }
        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    // Удаляет эпик по id и все его подзадачи
    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        epic.getSubtaskIds().forEach(subtaskId -> {
            Subtask removedSubtask = subtasks.remove(subtaskId);
            removeFromPrioritized(removedSubtask);
            historyManager.remove(subtaskId);
        });
        epics.remove(id);
        historyManager.remove(id);
    }

    // Возвращает список всех подзадач
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Удаляет все подзадачи
    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(this::removeFromPrioritized);
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtaskIds();
            updateEpicStatusAndTime(epic.getId());
        });
    }

    // Возвращает подзадачу по id
    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Создаёт новую подзадачу
    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не может быть null");
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id=" + subtask.getEpicId() + " не найден");
        }
        validateNoTimeIntersection(subtask);
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        addToPrioritized(subtask);
        updateEpicStatusAndTime(epic.getId());
        return subtask;
    }

    // Обновляет существующую подзадачу
    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId()) || !epics.containsKey(subtask.getEpicId())
                || subtask.getEpicId() == subtask.getId()) {
            return;
        }
        Subtask oldSubtask = subtasks.get(subtask.getId());
        removeFromPrioritized(oldSubtask);
        try {
            validateNoTimeIntersection(subtask);
            subtasks.put(subtask.getId(), subtask);
            addToPrioritized(subtask);
            updateEpicStatusAndTime(subtask.getEpicId());
        } catch (RuntimeException exception) {
            addToPrioritized(oldSubtask);
            throw exception;
        }
    }

    // Возвращает список подзадач конкретного эпика
    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(subtask -> subtask != null)
                .collect(Collectors.toList());
    }

    // Возвращает историю просмотренных задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Возвращает задачи и подзадачи
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Обновляет статус эпика
    protected void updateEpicStatus(int epicId) {
        updateEpicStatusAndTime(epicId);
    }

    // Пересчитывает статус, продолжительность, время начала и время окончания эпика
    protected void updateEpicStatusAndTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        List<Subtask> epicSubtasks = getSubtasksByEpicId(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setCalculatedTime(Duration.ZERO, null, null);
            return;
        }

        boolean allNew = epicSubtasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.NEW);
        boolean allDone = epicSubtasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

        Duration duration = epicSubtasks.stream()
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
        LocalDateTime startTime = epicSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(time -> time != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime endTime = epicSubtasks.stream()
                .map(Subtask::getEndTime)
                .filter(time -> time != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        epic.setCalculatedTime(duration, startTime, endTime);
    }

    // Проверяет, пересекаются ли две задачи по времени
    protected boolean isTasksTimeIntersect(Task firstTask, Task secondTask) {
        if (firstTask.getStartTime() == null || secondTask.getStartTime() == null
                || firstTask.getEndTime() == null || secondTask.getEndTime() == null) {
            return false;
        }
        return firstTask.getStartTime().isBefore(secondTask.getEndTime())
                && secondTask.getStartTime().isBefore(firstTask.getEndTime());
    }

    // Проверяет, пересекается ли переданная задача с любой другой задачей или подзадачей
    protected boolean hasTimeIntersection(Task task) {
        return getPrioritizedTasks().stream()
                .anyMatch(savedTask -> savedTask.getId() != task.getId() && isTasksTimeIntersect(savedTask, task));
    }

    // Вызывает проверку пересечений
    private void validateNoTimeIntersection(Task task) {
        if (hasTimeIntersection(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
        }
    }

    // Добавляет задачу или подзадачу в приоритетный список
    private void addToPrioritized(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    // Удаляет задачу или подзадачу из приоритетного списка
    private void removeFromPrioritized(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    // Удаляет подзадачу по id
    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return;
        }
        removeFromPrioritized(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatusAndTime(epic.getId());
        }
        historyManager.remove(id);
    }
}
