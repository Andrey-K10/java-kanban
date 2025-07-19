package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void subtasksWithSameIdShouldBeEqual() { // "проверьте, что наследники класса Task равны друг другу, если равен их id;"
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description 1", Status.NEW, 10);
        Subtask subtask2 = new Subtask(1, "Subtask 2", "Description 2", Status.DONE, 20);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны.");
    }

}