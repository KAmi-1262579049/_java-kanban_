package manager;

import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;
import task.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    // Заголовок CSV-файла
    private static final String HEADER = "id,type,name,status,description,epic,duration,startTime";
    // Файл, в котором будут храниться данные менеджера
    private final File file;

    // Конструктор менеджера
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Метод сохранения данных в файл
    private void save() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(HEADER).append("\n");
            getAllTasks().forEach(task -> builder.append(toString(task)).append("\n"));
            getAllEpics().forEach(epic -> builder.append(toString(epic)).append("\n"));
            getAllSubtasks().forEach(subtask -> builder.append(toString(subtask)).append("\n"));
            Files.writeString(file.toPath(), builder.toString());
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка сохранения файла.", exception);
        }
    }

    // Преобразование задачи в строку CSV
    private String toString(Task task) {
        String epicId = "";
        switch (task.getType()) {
            case SUBTASK:
                epicId = String.valueOf(((Subtask) task).getEpicId());
                break;
            case TASK:
            case EPIC:
                break;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи.");
        }
        String startTime = task.getStartTime() == null ? "" : task.getStartTime().toString();
        return String.format("%d,%s,%s,%s,%s,%s,%d,%s",
                task.getId(), task.getType(), task.getName(), task.getStatus(), task.getDescription(), epicId,
                task.getDuration().toMinutes(), startTime);
    }

    // Создание объекта задачи из строки CSV
    private static Task fromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        Duration duration = fields.length > 6 && !fields[6].isBlank()
                ? Duration.ofMinutes(Long.parseLong(fields[6]))
                : Duration.ZERO;
        LocalDateTime startTime = fields.length > 7 && !fields[7].isBlank()
                ? LocalDateTime.parse(fields[7])
                : null;

        switch (type) {
            case TASK:
                return new Task(id, name, description, status, duration, startTime);
            case EPIC:
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                return new Subtask(id, name, description, status, epicId, duration, startTime);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи.");
        }
    }

    // Статический метод загрузки менеджера из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            if (!file.exists() || Files.size(file.toPath()) == 0) {
                return manager;
            }
            List<String> lines = Files.readAllLines(file.toPath());
            List<Subtask> loadedSubtasks = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }
                Task task = fromString(line);
                switch (task.getType()) {
                    case TASK:
                        manager.addTaskWithId(task);
                        break;
                    case EPIC:
                        manager.addEpicWithId((Epic) task);
                        break;
                    case SUBTASK:
                        loadedSubtasks.add((Subtask) task);
                        break;
                    default:
                        throw new IllegalArgumentException("Неизвестный тип задачи.");
                }
            }
            loadedSubtasks.forEach(manager::addSubtaskWithId);
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка загрузки файла.", exception);
        }
        return manager;
    }

    // Создание задачи
    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    // Создание эпика
    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    // Создание подзадачи
    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    // Обновление задачи
    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    // Обновление эпика
    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    // Обновление подзадачи
    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    // Удаление задачи по идентификатору
    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    // Удаление эпика по идентификатору
    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    // Удаление подзадачи по идентификатору
    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    // Удаление всех задач
    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    // Удаление всех эпиков
    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    // Удаление всех подзадач
    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    // Тестовый сценарий
    public static void main(String[] args) {
        File file = new File("tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task = manager.createTask(new Task("Task", "Description"));
        Epic epic = manager.createEpic(new Epic("Epic", "Epic description"));
        manager.createSubtask(new Subtask("Subtask", "Subtask description", epic.getId()));
        FileBackedTaskManager loadedManager = loadFromFile(file);
        System.out.println(loadedManager.getTaskById(task.getId()));
        System.out.println(loadedManager.getAllEpics());
        System.out.println(loadedManager.getAllSubtasks());
    }
}
