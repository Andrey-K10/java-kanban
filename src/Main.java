import Model.Epic;
import Model.Status;
import Model.Subtask;
import Model.Task;
import Сontrollers.Managers;
import Сontrollers.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        /*
        Создаем следующие данные для теста:
        Задача 1 и Задача 2
        Эпик 3 с позадачей 4 и подзадачей 5
        Эпик 6 с подзадачей 7
         */

        // Создаем две задачи
        Task task1 = new Task(0, "Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task(0, "Задача 2", "Описание задачи 2", Status.NEW);
        int task1Id = manager.createTask(task1);
        int task2Id = manager.createTask(task2);

        // Эпик с двумя подзадачами
        Epic epic3 = new Epic(0, "Эпик 3", "Описание эпика 3");
        int epic3Id = manager.createEpic(epic3);

        Subtask subtask4 = new Subtask(0, "Подзадача 4", "Описание подзадачи 4", Status.NEW, epic3Id);
        Subtask subtask5 = new Subtask(0, "Подзадача 5", "Описание подзадачи 5", Status.NEW, epic3Id);
        int subtask4Id = manager.createSubtask(subtask4);
        int subtask5Id = manager.createSubtask(subtask5);

        // Создаем эпик с одной подзадачей
        Epic epic6 = new Epic(0, "Эпик 6", "Описание эпика 6");
        int epic6Id = manager.createEpic(epic6);

        Subtask subtask7 = new Subtask(0, "Подзадача 7", "Описание подзадачи 7", Status.NEW, epic6Id);
        int subtask7Id = manager.createSubtask(subtask7);

        // Распечатываем списки всех созданных задач, эпиков и подзадач
        System.out.println("\n-----------------------");
        System.out.println("Новые созданные объекты:");
        System.out.println("\nЗадачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nПодзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Тестирование
        System.out.println("\n-----------------------");
        System.out.println("Тестирование: изменение статусов");

        // Изменяем статусы созданных объектов
        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);

        subtask5.setStatus(Status.DONE);
        manager.updateSubtask(subtask5);

        subtask7.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask7);

        // Распечатываем обновленные списки
        System.out.println("\nОбновленные задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nОбновленные эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nОбновленные подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Проверяем, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач
        System.out.println("\nПроверка статусов:");
        System.out.println("Статус задачи 1: " + manager.getTaskById(task1Id).getStatus()); // IN_PROGRESS
        System.out.println("Статус эпика 3: " + manager.getEpicById(epic3Id).getStatus()); // IN_PROGRESS
        System.out.println("Статус эпика 6: " + manager.getEpicById(epic6Id).getStatus()); // IN_PROGRESS
        System.out.println("Статус подзадачи 5: " + manager.getSubtaskById(subtask5Id).getStatus()); // DONE


        // Удаляем задачу 2 и эпик с ИД 1
        System.out.println("\n-----------------------");
        System.out.println("Удаление");

        System.out.println("\nУдаляем задачу 2 и удаляем эпик 3");
        manager.deleteTaskById(task2Id);
        manager.deleteEpicById(epic3Id);


        // Распечатываем финальные списки
        System.out.println("\nФинальные задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nФинальные эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nФинальные подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        // Тест поиска по ИД
        System.out.println("\n-----------------------");
        System.out.println("Ищем задачу 1, эпик 6, и подзадачу 7 (которые мы не удаляли ранее):");

        System.out.println(manager.getTaskById(1));
        System.out.println(manager.getEpicById(6));
        System.out.println(manager.getSubtaskById(7));


        // Получение истории просмотров
        System.out.println("\nИстория просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

        System.out.println("\n-----------------------");
        System.out.println("Конец теста");

    }
}