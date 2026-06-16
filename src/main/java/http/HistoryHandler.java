package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

// Класс HistoryHandler обрабатывает HTTP-запросы для получения истории просмотров задач
public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager; // Хранит экземпляр менеджера задач
    private final Gson gson; // Используется для преобразования объектов Java в JSON и обратно

    // Конструктор класса
    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    // Метод обрабатывает входящие HTTP-запросы
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod()) && "/history".equals(exchange.getRequestURI().getPath())) {
                sendText(exchange, gson.toJson(taskManager.getHistory()));
                return;
            }
            sendNotFound(exchange);
        } catch (RuntimeException exception) {
            sendServerError(exchange);
        }
    }
}
