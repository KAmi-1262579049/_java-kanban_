package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

// Класс PrioritizedHandler для обработки запросов на получение задач по приоритету
public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    // Поле для работы с задачами
    private final TaskManager taskManager;
    // Поле для преобразования объектов в JSON
    private final Gson gson;

    // Конструктор класса
    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    // Метод для обработки HTTP-запросов
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod()) && "/prioritized".equals(exchange.getRequestURI().getPath())) {
                sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
                return;
            }
            sendNotFound(exchange);
        } catch (RuntimeException exception) {
            sendServerError(exchange);
        }
    }
}
