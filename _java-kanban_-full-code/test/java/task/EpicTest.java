package task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Тесты для класса Epic
class EpicTest {

    // Проверка, что новый эпик не содержит подзадач
    @Test
    void epicShouldHaveEmptySubtaskListAfterCreation() {
        Epic epic = new Epic("Epic", "Description");

        assertNotNull(epic.getSubtaskIds(),
                "Список подзадач не должен быть null");

        assertTrue(epic.getSubtaskIds().isEmpty(),
                "Список подзадач нового эпика должен быть пустым");
    }

    // Проверка наследования логики equals по id
    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic(1, "Epic 1", "Description");
        Epic epic2 = new Epic(1, "Epic 2", "Description");

        assertEquals(epic1, epic2,
                "Эпики с одинаковым id должны быть равны");
    }

    // Проверка защиты от дублирования id подзадач
    @Test
    void addSubtaskIdShouldNotAddDuplicateIds() {
        Epic epic = new Epic("Epic", "Description");

        epic.addSubtaskId(1);
        epic.addSubtaskId(1);
        epic.addSubtaskId(1);

        assertEquals(1, epic.getSubtaskIds().size(),
                "ID должен быть добавлен только один раз");

        assertTrue(epic.getSubtaskIds().contains(1),
                "Список должен содержать ID 1");
    }

    // Проверка корректного удаления id подзадачи
    @Test
    void removeSubtaskIdShouldRemoveSpecifiedId() {
        Epic epic = new Epic("Epic", "Description");

        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        assertEquals(3, epic.getSubtaskIds().size(),
                "Должно быть 3 ID");

        epic.removeSubtaskId(2);

        assertEquals(2, epic.getSubtaskIds().size(),
                "После удаления должно остаться 2 ID");

        assertTrue(epic.getSubtaskIds().contains(1),
                "Список должен содержать ID 1");

        assertFalse(epic.getSubtaskIds().contains(2),
                "Список не должен содержать ID 2");

        assertTrue(epic.getSubtaskIds().contains(3),
                "Список должен содержать ID 3");
    }

    // Проверка полной очистки списка подзадач
    @Test
    void clearSubtaskIdsShouldRemoveAllIds() {
        Epic epic = new Epic("Epic", "Description");

        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        epic.addSubtaskId(3);

        assertEquals(3, epic.getSubtaskIds().size(),
                "До очистки должно быть 3 ID");

        epic.clearSubtaskIds();

        assertTrue(epic.getSubtaskIds().isEmpty(),
                "После очистки список должен быть пустым");
    }
}
