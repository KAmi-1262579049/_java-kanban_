package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;

// Класс TasksHandler для обработки HTTP-запросов к задачам
public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    // Поле для работы с задачами через менеджер
    private final TaskManager taskManager;
    // Поле для преобразования объектов в JSON и обратно
    private final Gson gson;

    // Конструктор класса
    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    // Метод для обработки HTTP-запросов
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            if ("GET".equals(method) && pathParts.length == 0) {
                sendText(exchange, gson.toJson(taskManager.getAllTasks()));
                return;
            }
            if ("GET".equals(method) && pathParts.length == 2) {
                sendText(exchange, gson.toJson(taskManager.getAllTasks()));
                return;
            }
            if ("GET".equals(method) && pathParts.length == 3) {
                Integer id = getId(pathParts, 2);
                Task task = id == null ? null : taskManager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange);
                    return;
                }
                sendText(exchange, gson.toJson(task));
                return;
            }
            if ("POST".equals(method) && pathParts.length == 2) {
                Task task = gson.fromJson(readBody(exchange), Task.class);
                if (task.getId() == 0) {
                    taskManager.createTask(task);
                } else if (taskManager.getAllTasks().stream().noneMatch(savedTask -> savedTask.getId() == task.getId())) {
                    sendNotFound(exchange);
                    return;
                } else {
                    taskManager.updateTask(task);
                }
                sendCreated(exchange);
                return;
            }
            if ("DELETE".equals(method) && pathParts.length == 3) {
                Integer id = getId(pathParts, 2);
                if (id == null || taskManager.getAllTasks().stream().noneMatch(savedTask -> savedTask.getId() == id)) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.deleteTaskById(id);
                sendText(exchange, "");
                return;
            }
            sendNotFound(exchange);
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("пересека")) {
                sendHasInteractions(exchange);
            } else {
                sendServerError(exchange);
            }
        } catch (RuntimeException exception) {
            sendServerError(exchange);
        }
    }
}
