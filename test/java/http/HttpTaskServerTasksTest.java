package http;

import com.google.gson.Gson;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Тестовый класс HttpTaskServerTasksTest
class HttpTaskServerTasksTest {
    // Поле для менеджера задач
    private TaskManager manager;
    // Поле для HTTP-сервера задач
    private HttpTaskServer taskServer;
    // Поле для преобразования объектов в JSON и обратно
    private final Gson gson = HttpTaskServer.getGson();
    // Поле для HTTP-клиента, отправляющего запросы серверу
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

    // Тест для проверки создания задачи через HTTP API
    @Test
    void shouldCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Task", "Description", Duration.ofMinutes(30),
                LocalDateTime.of(2026, 1, 1, 10, 0));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Task", tasks.get(0).getName());
    }

    // Тест для проверки получения списка задач
    @Test
    void shouldReturnTasks() throws IOException, InterruptedException {
        manager.createTask(new Task("Task", "Description", Duration.ofMinutes(30),
                LocalDateTime.of(2026, 1, 1, 10, 0)));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertTrue(response.body().contains("Task"));
    }

    // Тест для проверки получения задачи по идентификатору
    @Test
    void shouldReturnTaskById() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Task", "Description"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task"));
    }

    // Тест для проверки ответа 404 при запросе несуществующей задачи
    @Test
    void shouldReturnNotFoundForUnknownTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    // Тест для проверки ответа 406 при пересечении задач по времени
    @Test
    void shouldReturnNotAcceptableForIntersectedTask() throws IOException, InterruptedException {
        manager.createTask(new Task("Task 1", "Description", Duration.ofMinutes(60),
                LocalDateTime.of(2026, 1, 1, 10, 0)));
        Task task = new Task("Task 2", "Description", Duration.ofMinutes(60),
                LocalDateTime.of(2026, 1, 1, 10, 30));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals(1, manager.getAllTasks().size());
    }

    // Тест для проверки удаления задачи через HTTP API
    @Test
    void shouldDeleteTask() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task("Task", "Description"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }
}
