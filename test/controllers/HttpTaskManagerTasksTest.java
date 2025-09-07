package tests;

import com.google.gson.Gson;
import model.Status;
import model.Task;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest extends BaseHttpTest {

    @Test
    public void testGetAllTasksWhenEmpty() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void testCreateTask() throws Exception {
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        Gson gson = taskServer.getGson();
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("\"id\""));

        // Проверяем, что задача добавилась в менеджер
        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getName());
    }

    @Test
    public void testGetTaskById() throws Exception {
        // Сначала создаем задачу
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    public void testGetTaskByIdNotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateTask() throws Exception {
        // Сначала создаем задачу
        Task task = new Task(0, "Original Task", "Original Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(task);

        // Обновляем задачу
        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description", Status.IN_PROGRESS,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        Gson gson = taskServer.getGson();
        String taskJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        // Проверяем, что задача обновилась
        Task taskFromManager = manager.getTaskById(taskId);
        assertEquals("Updated Task", taskFromManager.getName());
        assertEquals(Status.IN_PROGRESS, taskFromManager.getStatus());
    }

    @Test
    public void testDeleteTask() throws Exception {
        // Сначала создаем задачу
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        // Проверяем, что задача удалилась
        assertNull(manager.getTaskById(taskId));
        assertEquals(0, manager.getAllTasks().size());
    }

    @Test
    public void testDeleteAllTasks() throws Exception {
        // Сначала создаем несколько задач
        Task task1 = new Task(0, "Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task(0, "Task 2", "Description 2", Status.NEW,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        manager.createTask(task1);
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        // Проверяем, что все задачи удалились
        assertEquals(0, manager.getAllTasks().size());
    }

    @Test
    public void testCreateTaskWithTimeConflict() throws Exception {
        // Сначала создаем задачу
        LocalDateTime startTime = LocalDateTime.now();
        Task task1 = new Task(0, "Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(60), startTime);
        manager.createTask(task1);

        // Пытаемся создать задачу с пересекающимся временем
        Task task2 = new Task(0, "Task 2", "Description 2", Status.NEW,
                Duration.ofMinutes(30), startTime.plusMinutes(30));

        Gson gson = taskServer.getGson();
        String taskJson = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Task has time interactions"));
    }
}