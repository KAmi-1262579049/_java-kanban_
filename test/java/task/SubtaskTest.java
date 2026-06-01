package task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Тестовый класс для проверки функциональности класса Task
class TaskTest {

    // Тест проверяет, что задачи с одинаковым id считаются равными
    @Test
    void shouldBeEqualWhenIdsAreSame() {
        Task task1 = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task(1, "Task 2", "Description 2", TaskStatus.DONE);

        assertEquals(task1, task2);
    }

    // Тест проверяет корректность сравнения задач после изменения id
    @Test
    void shouldRemainEqualAfterIdChange() {
        Task originalTask = new Task("Original", "Description");
        originalTask.setId(1);

        Task copiedTask = new Task(1, "Copy", "Description", TaskStatus.NEW);

        assertEquals(originalTask, copiedTask);
    }
}
