package task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Тестовый класс для проверки корректности работы класса Subtask
class SubtaskTest {

    // Тест проверяет, что подзадачи с одинаковым id считаются равными
    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask(
                1,
                "Subtask 1",
                "Description 1",
                TaskStatus.NEW,
                100
        );

        Subtask subtask2 = new Subtask(
                1,
                "Subtask 2",
                "Description 2",
                TaskStatus.DONE,
                200
        );

        assertEquals(
                subtask1,
                subtask2,
                "Подзадачи с одинаковым id должны быть равны"
        );

        assertEquals(
                subtask1.hashCode(),
                subtask2.hashCode(),
                "Хэш-коды подзадач с одинаковым id должны совпадать"
        );
    }

    // Тест проверяет, что подзадача не равна обычной задаче с тем же id
    @Test
    void testSubtaskNotEqualToTaskWithSameId() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW);

        Subtask subtask = new Subtask(
                1,
                "Subtask",
                "Description",
                TaskStatus.NEW,
                100
        );

        assertNotEquals(task, subtask);
        assertNotEquals(subtask, task);
    }

    // Тест проверяет техническую возможность установить epicId равным id подзадачи
    @Test
    void testSubtaskCanSetEpicIdToItsOwnId() {
        Subtask subtask = new Subtask("Subtask", "Description", 1);

        subtask.setId(1);
        subtask.setEpicId(subtask.getId());

        assertEquals(subtask.getId(), subtask.getEpicId());
    }

    // Тест проверяет, что Subtask действительно наследуется от Task
    @Test
    void testSubtaskInheritance() {
        Subtask subtask = new Subtask("Test", "Description", 1);

        assertTrue(subtask instanceof Task);
        assertEquals(TaskType.SUBTASK, subtask.getType());
    }

    // Тест проверяет конструктор Subtask с указанием всех параметров
    @Test
    void testSubtaskConstructorWithId() {
        Subtask subtask = new Subtask(
                1,
                "Subtask",
                "Description",
                TaskStatus.IN_PROGRESS,
                10
        );

        assertEquals(1, subtask.getId());
        assertEquals("Subtask", subtask.getName());
        assertEquals("Description", subtask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, subtask.getStatus());
        assertEquals(10, subtask.getEpicId());
        assertEquals(TaskType.SUBTASK, subtask.getType());
    }

    // Тест проверяет конструктор Subtask без указания id
    @Test
    void testSubtaskConstructorWithoutId() {
        Subtask subtask = new Subtask("Subtask", "Description", 10);

        assertEquals(0, subtask.getId());
        assertEquals("Subtask", subtask.getName());
        assertEquals("Description", subtask.getDescription());
        assertEquals(TaskStatus.NEW, subtask.getStatus());
        assertEquals(10, subtask.getEpicId());
        assertEquals(TaskType.SUBTASK, subtask.getType());
    }

    // Тест проверяет работу сеттеров класса Subtask
    @Test
    void testSubtaskSetters() {
        Subtask subtask = new Subtask(
                "Original",
                "Original Desc",
                1
        );

        subtask.setId(5);
        subtask.setName("Updated");
        subtask.setDescription("Updated Desc");
        subtask.setStatus(TaskStatus.DONE);
        subtask.setEpicId(20);

        assertEquals(5, subtask.getId());
        assertEquals("Updated", subtask.getName());
        assertEquals("Updated Desc", subtask.getDescription());
        assertEquals(TaskStatus.DONE, subtask.getStatus());
        assertEquals(20, subtask.getEpicId());
    }

    // Тест проверяет строковое представление подзадачи
    @Test
    void testSubtaskToString() {
        Subtask subtask = new Subtask(
                1,
                "Subtask",
                "Description",
                TaskStatus.NEW,
                10
        );

        String result = subtask.toString();

        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name='Subtask'"));
        assertTrue(result.contains("description='Description'"));
        assertTrue(result.contains("status=NEW"));
        assertTrue(result.contains("type=SUBTASK"));
        assertTrue(result.contains("epicId=10"));
    }

    // Тест проверяет, что Subtask не равен Epic с тем же id
    @Test
    void testSubtaskNotEqualToEpicWithSameId() {
        Subtask subtask = new Subtask(
                1,
                "Subtask",
                "Description",
                TaskStatus.NEW,
                100
        );

        Epic epic = new Epic(1, "Epic", "Description");

        assertNotEquals(subtask, epic);
        assertNotEquals(epic, subtask);
    }

    // Тест проверяет консистентность equals() и hashCode()
    @Test
    void testEqualsAndHashCodeConsistency() {
        Subtask subtask1 = new Subtask(
                1,
                "Task1",
                "Desc1",
                TaskStatus.NEW,
                10
        );

        Subtask subtask2 = new Subtask(
                1,
                "Task2",
                "Desc2",
                TaskStatus.DONE,
                20
        );

        if (subtask1.equals(subtask2)) {
            assertEquals(subtask1.hashCode(), subtask2.hashCode());
        }

        assertEquals(subtask1, subtask1);
        assertEquals(subtask2, subtask2);

        if (subtask1.equals(subtask2)) {
            assertTrue(subtask2.equals(subtask1));
        }
    }
}
