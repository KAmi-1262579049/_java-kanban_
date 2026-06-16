package http;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Тестовый класс HttpTaskServerHistoryAndPrioritizedTest
class HttpTaskServerHistoryAndPrioritizedTest {
    // Поле для хранения экземпляра менеджера задач
    private TaskManager manager;
    // Поле для хранения экземпляра HTTP-сервера
    private HttpTaskServer taskServer;
    // Поле для хранения HTTP-клиента, используемого при отправке запросов
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    // Тест для проверки получения истории просмотров задач
    @Test
    void shouldReturnHistory() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Task", "Description"));
        manager.getTaskById(task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task"));
    }

    // Тест для проверки получения списка задач по приоритету
    @Test
    void shouldReturnPrioritizedTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("Task 2", "Description", Duration.ofMinutes(30),
                LocalDateTime.of(2026, 1, 1, 12, 0)));
        manager.createTask(new Task("Task 1", "Description", Duration.ofMinutes(30),
                LocalDateTime.of(2026, 1, 1, 10, 0)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().indexOf("Task 1") < response.body().indexOf("Task 2"));
    }

    // Тест для проверки обработки неподдерживаемого метода для истории
    @Test
    void shouldReturnNotFoundForWrongHistoryMethod() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}
