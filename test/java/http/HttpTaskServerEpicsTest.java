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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Класс HttpTaskServerEpicsTest для тестирования HTTP API при работе с эпиками
class HttpTaskServerEpicsTest {
    // Поле для хранения экземпляра менеджера задач
    private TaskManager manager;
    // Поле для хранения экземпляра HTTP-сервера
    private HttpTaskServer taskServer;
    // Поле для преобразования объектов Java в JSON и обратно
    private final Gson gson = HttpTaskServer.getGson();
    // Поле для отправки HTTP-запросов к серверу
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

    // Тест для проверки создания эпика через HTTP API
    @Test
    void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllEpics().size());
    }

    // Тест для проверки получения списка эпиков через HTTP API
    @Test
    void shouldReturnEpics() throws IOException, InterruptedException {
        manager.createEpic(new Epic("Epic", "Description"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Epic"));
    }

    // Тест для проверки получения списка подзадач конкретного эпика
    @Test
    void shouldReturnEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask"));
    }

    // Тест для проверки возврата ошибки 404 при запросе несуществующего эпика
    @Test
    void shouldReturnNotFoundForUnknownEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    // Тест для проверки удаления эпика через HTTP API
    @Test
    void shouldDeleteEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllEpics().isEmpty());
    }
}
