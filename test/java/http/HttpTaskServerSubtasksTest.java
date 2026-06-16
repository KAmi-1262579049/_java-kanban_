package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Класс HttpTaskServerSubtasksTest для тестирования HTTP API подзадач
class HttpTaskServerSubtasksTest {
    // Поле для хранения экземпляра менеджера задач
    private TaskManager manager;
    // Поле для хранения HTTP-сервера задач
    private HttpTaskServer taskServer;
    // Поле для хранения тестового эпика
    private Epic epic;
    // Поле для хранения объекта Gson для сериализации и десериализации JSON
    private final Gson gson = HttpTaskServer.getGson();
    // Поле для хранения HTTP-клиента
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        epic = manager.createEpic(new Epic("Epic", "Description"));
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    // Тест для проверки создания подзадачи через HTTP API
    @Test
    void shouldCreateSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask", "Description", epic.getId(), Duration.ofMinutes(30),
                LocalDateTime.of(2026, 1, 1, 10, 0));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllSubtasks().size());
        assertEquals(epic.getId(), manager.getAllSubtasks().get(0).getEpicId());
    }

    // Тест для проверки получения списка подзадач
    @Test
    void shouldReturnSubtasks() throws IOException, InterruptedException {
        manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask"));
    }

    // Тест для проверки получения подзадачи по идентификатору
    @Test
    void shouldReturnSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask"));
    }

    // Тест для проверки возврата ошибки 404 при запросе несуществующей подзадачи
    @Test
    void shouldReturnNotFoundForUnknownSubtask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    // Тест для проверки возврата ошибки 406 при пересечении подзадач по времени
    @Test
    void shouldReturnNotAcceptableForIntersectedSubtask() throws IOException, InterruptedException {
        manager.createSubtask(new Subtask("Subtask 1", "Description", epic.getId(), Duration.ofMinutes(60),
                LocalDateTime.of(2026, 1, 1, 10, 0)));
        Subtask subtask = new Subtask("Subtask 2", "Description", epic.getId(), Duration.ofMinutes(60),
                LocalDateTime.of(2026, 1, 1, 10, 30));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals(1, manager.getAllSubtasks().size());
    }

    // Тест для проверки удаления подзадачи по идентификатору
    @Test
    void shouldDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}
