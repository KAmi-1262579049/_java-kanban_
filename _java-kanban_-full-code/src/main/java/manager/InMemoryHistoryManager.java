package manager;

import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Класс реализует интерфейс HistoryManager для хранения истории просмотров задач
public class InMemoryHistoryManager implements HistoryManager {

    // Узел двусвязного списка
    private static class Node {
        private Task data;
        private Node next;
        private Node prev;

        public Node(Node prev, Task data, Node next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }

    // HashMap для быстрого доступа к узлам по id задачи
    private final Map<Integer, Node> historyMap = new HashMap<>();

    // Начало и конец двусвязного списка
    private Node head;
    private Node tail;

    // Метод добавления задачи в конец списка
    private void linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);

        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }

        historyMap.put(task.getId(), newNode);
    }

    // Метод удаления узла из списка
    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        Node prev = node.prev;
        Node next = node.next;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
        }
    }

    // Метод преобразования связного списка в ArrayList
    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();

        Node current = head;

        while (current != null) {
            tasks.add(current.data);
            current = current.next;
        }

        return tasks;
    }

    // Метод добавления задачи в историю
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        remove(task.getId());
        linkLast(task);
    }

    // Метод удаления задачи из истории
    @Override
    public void remove(int id) {
        Node node = historyMap.remove(id);

        if (node != null) {
            removeNode(node);
        }
    }

    // Метод получения истории просмотров
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
