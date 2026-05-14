package main.java;

import manager.Managers;
import manager.TaskManager;
import task.Task;
import task.TaskStatus;
import task.Epic;
import task.Subtask;
import java.util.List;

// Главный класс приложения для тестирования трекера задач
public class Main {
    public static void main(String[] args) {
        System.out.println("Тестирование трекера задач!");
        System.out.println("============================\n");

        TaskManager taskManager = Managers.getDefault();

        // Тест 1: Создание обычных задач
        System.out.println("=== ТЕСТ 1: Создание обычных задач ===");
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task2 = new Task("Задача 2", "Описание задачи 2");

        Task createdTask1 = taskManager.createTask(task1);
        Task createdTask2 = taskManager.createTask(task2);
        System.out.println("Создана задача 1: " + createdTask1);
        System.out.println("Создана задача 2: " + createdTask2);

        // Тест 2: Создание эпиков и подзадач
        System.out.println("\n=== ТЕСТ 2: Создание эпиков и подзадач ===");
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic createdEpic1 = taskManager.createEpic(epic1);
        System.out.println("Создан эпик 1: " + createdEpic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", createdEpic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", createdEpic1.getId());

        Subtask createdSubtask1 = taskManager.createSubtask(subtask1);
        Subtask createdSubtask2 = taskManager.createSubtask(subtask2);
        System.out.println("Создана подзадача 1: " + createdSubtask1);
        System.out.println("Создана подзадача 2: " + createdSubtask2);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2"); // ИСПРАВЛЕНО: правильный конструктор
        Epic createdEpic2 = taskManager.createEpic(epic2);
        System.out.println("\nСоздан эпик 2: " + createdEpic2);

        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", createdEpic2.getId());
        Subtask createdSubtask3 = taskManager.createSubtask(subtask3);
        System.out.println("Создана подзадача 3: " + createdSubtask3);

        // Тест 3: Вывод всех задач
        System.out.println("\n=== ТЕСТ 3: Вывод всех задач ===");
        printAllTasks(taskManager);

        // Тест 4: Изменение статусов
        System.out.println("\n=== ТЕСТ 4: Изменение статусов ===");
        createdTask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(createdTask1);

        createdSubtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(createdSubtask1);

        createdSubtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(createdSubtask2);

        createdSubtask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(createdSubtask3);

        System.out.println("\nПосле изменения статусов:");
        printAllTasks(taskManager);

        // Тест 5: Работа с историей просмотров
        System.out.println("\n=== ТЕСТ 5: Работа с историей ===");
        System.out.println("Обращение к задачам для наполнения истории...");
        taskManager.getTaskById(createdTask1.getId());
        taskManager.getTaskById(createdTask2.getId());
        taskManager.getEpicById(createdEpic1.getId());
        taskManager.getSubtaskById(createdSubtask1.getId());
        taskManager.getEpicById(createdEpic2.getId());
        taskManager.getSubtaskById(createdSubtask3.getId());

        for (int i = 0; i < 5; i++) {
            taskManager.getTaskById(createdTask1.getId());
        }

        System.out.println("\nИстория просмотров (должно быть не более 10 элементов):");
        List<Task> history = taskManager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i));
        }
        System.out.println("Всего в истории: " + history.size() + " элементов");

        // Тест 6: Удаление задач
        System.out.println("\n=== ТЕСТ 6: Удаление задач ===");
        taskManager.deleteTaskById(createdTask1.getId());
        taskManager.deleteEpicById(createdEpic1.getId());

        System.out.println("\nПосле удаления задач:");
        printAllTasks(taskManager);

        System.out.println("\nИстория после удаления задач:");
        history = taskManager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ". " + history.get(i));
        }

        // Тест 7: Удаление всех задач
        System.out.println("\n=== ТЕСТ 7: Удаление всех задач ===");
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();

        System.out.println("\nПосле удаления всех задач:");
        System.out.println("Обычных задач: " + taskManager.getAllTasks().size());
        System.out.println("Эпиков: " + taskManager.getAllEpics().size());
        System.out.println("Подзадач: " + taskManager.getAllSubtasks().size());
        System.out.println("Задач в истории: " + taskManager.getHistory().size());
    }

    // Вспомогательный метод для вывода всех задач в удобном формате
    private static void printAllTasks(TaskManager manager) {
        System.out.println("\n=== Все задачи ===");
        List<Task> tasks = manager.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("Нет обычных задач");
        } else {
            for (Task task : tasks) {
                System.out.println(task);
            }
        }

        System.out.println("\n=== Все эпики ===");
        List<Epic> epics = manager.getAllEpics();
        if (epics.isEmpty()) {
            System.out.println("Нет эпиков");
        } else {
            for (Epic epic : epics) {
                System.out.println(epic);
                List<Subtask> epicSubtasks = manager.getSubtasksByEpicId(epic.getId());
                if (epicSubtasks.isEmpty()) {
                    System.out.println("  -> Нет подзадач");
                } else {
                    for (Subtask subtask : epicSubtasks) {
                        System.out.println("  -> " + subtask);
                    }
                }
            }
        }

        System.out.println("\n=== Все подзадачи ===");
        List<Subtask> subtasks = manager.getAllSubtasks();
        if (subtasks.isEmpty()) {
            System.out.println("Нет подзадач");
        } else {
            for (Subtask subtask : subtasks) {
                System.out.println(subtask);
            }
        }
    }
}
