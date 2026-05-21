package manager;

import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;
import task.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

// Менеджер задач с сохранением в файл
public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    // Конструктор менеджера
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Метод сохранения состояния менеджера в файл
    private void save() {
        try {
            StringBuilder builder = new StringBuilder();

            builder.append("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                builder.append(toString(task)).append("\n");
            }

            for (Epic epic : getAllEpics()) {
                builder.append(toString(epic)).append("\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                builder.append(toString(subtask)).append("\n");
            }

            Files.writeString(file.toPath(), builder.toString());
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка сохранения файла.", exception);
        }
    }

    // Преобразование задачи в строку
    private String toString(Task task) {
        String epicId = "";

        if (task instanceof Subtask subtask) {
            epicId = String.valueOf(subtask.getEpicId());
        }

        return String.format(
                "%d,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                epicId
        );
    }

    // Создание задачи из строки
    private static Task fromString(String value) {
        String[] fields = value.split(",");

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);

            case EPIC:
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                return epic;

            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                return new Subtask(id, name, description, status, epicId);

            default:
                throw new IllegalArgumentException("Неизвестный тип задачи.");
        }
    }

    // Загрузка менеджера из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            List<String> lines = Files.readAllLines(file.toPath());

            for (int i = 1; i < lines.size(); i++) {
                Task task = fromString(lines.get(i));

                switch (task.getType()) {
                    case TASK:
                        manager.createTask(task);
                        break;

                    case EPIC:
                        manager.createEpic((Epic) task);
                        break;

                    case SUBTASK:
                        manager.createSubtask((Subtask) task);
                        break;

                    default:
                        break;
                }
            }
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

    // Тестовый сценарий
    public static void main(String[] args) {
        File file = new File("tasks.csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = manager.createTask(new Task("Task", "Description"));

        Epic epic = manager.createEpic(new Epic("Epic", "Epic description"));

        manager.createSubtask(
                new Subtask("Subtask", "Subtask description", epic.getId())
        );

        FileBackedTaskManager loadedManager = loadFromFile(file);

        System.out.println(loadedManager.getAllTasks());
        System.out.println(loadedManager.getAllEpics());
        System.out.println(loadedManager.getAllSubtasks());
    }
}

