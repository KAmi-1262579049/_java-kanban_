package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

// Класс HttpTaskServer для запуска и работы HTTP-сервера приложения
public class HttpTaskServer {
    // Константа для хранения номера порта сервера
    public static final int PORT = 8080;
    // Поле для преобразования объектов в JSON и обратно
    private static final Gson GSON = new Gson();
    // Поле для хранения экземпляра HTTP-сервера
    private final HttpServer server;

    // Конструктор класса для создания сервера с менеджером по умолчанию
    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    // Конструктор класса для создания сервера с переданным менеджером задач
    public HttpTaskServer(TaskManager taskManager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(taskManager, GSON));
        server.createContext("/subtasks", new SubtasksHandler(taskManager, GSON));
        server.createContext("/epics", new EpicsHandler(taskManager, GSON));
        server.createContext("/history", new HistoryHandler(taskManager, GSON));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, GSON));
    }

    // Метод для получения объекта Gson
    public static Gson getGson() {
        return GSON;
    }

    // Метод для запуска HTTP-сервера
    public void start() {
        server.start();
    }

    // Метод для остановки HTTP-сервера
    public void stop() {
        server.stop(0);
    }

    // Метод для запуска приложения
    public static void main(String[] args) throws IOException {
        new HttpTaskServer().start();
    }
}
