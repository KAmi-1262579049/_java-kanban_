package http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Класс для всех HTTP-обработчиков
public class BaseHttpHandler {

    // Метод отправки успешного ответа с кодом 200
    protected void sendText(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, 200, text);
    }

    // Метод отправки ответа об успешном создании ресурса (код 201)
    protected void sendCreated(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 201, "");
    }

    // Метод отправки ошибки "ресурс не найден" (код 404)
    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
    }

    // Метод отправки ошибки пересечения задач по времени (код 406)
    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 406, "{\"error\":\"Task has time intersection\"}");
    }

    // Метод отправки внутренней ошибки сервера (код 500)
    protected void sendServerError(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
    }

    // Метод чтения тела HTTP-запроса
    protected String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Метод отправки HTTP-ответа клиенту
    protected void sendResponse(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    // Метод получения id из URL-пути
    protected Integer getId(String[] pathParts, int index) {
        if (pathParts.length <= index || pathParts[index].isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(pathParts[index]);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
