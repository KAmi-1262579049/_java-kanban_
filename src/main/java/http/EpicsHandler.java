package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import task.Epic;

import java.io.IOException;

// Класс EpicsHandler обработчик HTTP-запросов для работы с эпиками
public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager; // Хранит экземпляр менеджера задач
    private final Gson gson; // Используется для преобразования объектов Java в JSON и обратно

    // Конструктор класса
    public EpicsHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    // Метод обрабатывает HTTP-запросы для работы с эпиками
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String[] pathParts = exchange.getRequestURI().getPath().split("/");
            if ("GET".equals(method) && pathParts.length == 2) {
                sendText(exchange, gson.toJson(taskManager.getAllEpics()));
                return;
            }
            if ("GET".equals(method) && pathParts.length == 3) {
                Integer id = getId(pathParts, 2);
                Epic epic = id == null ? null : taskManager.getEpicById(id);
                if (epic == null) {
                    sendNotFound(exchange);
                    return;
                }
                sendText(exchange, gson.toJson(epic));
                return;
            }
            if ("GET".equals(method) && pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
                Integer id = getId(pathParts, 2);
                if (id == null || taskManager.getAllEpics().stream().noneMatch(epic -> epic.getId() == id)) {
                    sendNotFound(exchange);
                    return;
                }
                sendText(exchange, gson.toJson(taskManager.getSubtasksByEpicId(id)));
                return;
            }
            if ("POST".equals(method) && pathParts.length == 2) {
                Epic epic = gson.fromJson(readBody(exchange), Epic.class);
                if (epic.getId() == 0) {
                    taskManager.createEpic(epic);
                } else if (taskManager.getAllEpics().stream().noneMatch(savedEpic -> savedEpic.getId() == epic.getId())) {
                    sendNotFound(exchange);
                    return;
                } else {
                    taskManager.updateEpic(epic);
                }
                sendCreated(exchange);
                return;
            }
            if ("DELETE".equals(method) && pathParts.length == 3) {
                Integer id = getId(pathParts, 2);
                if (id == null || taskManager.getAllEpics().stream().noneMatch(epic -> epic.getId() == id)) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.deleteEpicById(id);
                sendText(exchange, "");
                return;
            }
            sendNotFound(exchange);
        } catch (RuntimeException exception) {
            sendServerError(exchange);
        }
    }
}
