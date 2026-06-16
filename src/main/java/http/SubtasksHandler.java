package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Subtask;

import java.io.IOException;

// Класс SubtasksHandler для обработки HTTP-запросов, связанных с подзадачами
public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    // Поле для работы с подзадачами через менеджер задач
    private final TaskManager taskManager;
    // Поле для преобразования объектов в JSON и обратно
    private final Gson gson;

    // Конструктор класса
    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    // Метод для обработки входящих HTTP-запросов
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            if ("GET".equals(method) && pathParts.length == 2) {
                sendText(exchange, gson.toJson(taskManager.getAllSubtasks()));
                return;
            }
            if ("GET".equals(method) && pathParts.length == 3) {
                Integer id = getId(pathParts, 2);
                Subtask subtask = id == null ? null : taskManager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange);
                    return;
                }
                sendText(exchange, gson.toJson(subtask));
                return;
            }
            if ("POST".equals(method) && pathParts.length == 2) {
                Subtask subtask = gson.fromJson(readBody(exchange), Subtask.class);
                if (taskManager.getAllEpics().stream().noneMatch(epic -> epic.getId() == subtask.getEpicId())) {
                    sendNotFound(exchange);
                    return;
                }
                if (subtask.getId() == 0) {
                    taskManager.createSubtask(subtask);
                } else if (taskManager.getAllSubtasks().stream().noneMatch(savedSubtask -> savedSubtask.getId() == subtask.getId())) {
                    sendNotFound(exchange);
                    return;
                } else {
                    taskManager.updateSubtask(subtask);
                }
                sendCreated(exchange);
                return;
            }
            if ("DELETE".equals(method) && pathParts.length == 3) {
                Integer id = getId(pathParts, 2);
                if (id == null || taskManager.getAllSubtasks().stream().noneMatch(subtask -> subtask.getId() == id)) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.deleteSubtaskById(id);
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
