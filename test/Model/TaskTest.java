package Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() { // "проверьте, что экземпляры класса Task равны друг другу, если равен их id"
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(1, "Task 2", "Description 2", Status.IN_PROGRESS);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны.");
    }
}