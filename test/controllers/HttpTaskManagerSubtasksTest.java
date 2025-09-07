package tests;

import com.google.gson.Gson;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest extends BaseHttpTest {

    @Test
    public void testCreateSubtask() throws Exception {
        // Сначала создаем эпик
        Epic epic = new Epic(0, "Parent Epic", "Epic Description");
        int epicId = manager.createEpic(epic);

        // Создаем подзадачу
        Subtask subtask = new Subtask(0, "Test Subtask", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), epicId);

        Gson gson = taskServer.getGson();
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        // Проверяем, что подзадача добавилась в менеджер
        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("Test Subtask", subtasks.get(0).getName());
        assertEquals(epicId, subtasks.get(0).getEpicId());
    }

    @Test
    public void testCreateSubtaskWithInvalidEpic() throws Exception {
        // Пытаемся создать подзадачу с несуществующим эпиком
        Subtask subtask = new Subtask(0, "Test Subtask", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), 999);

        Gson gson = taskServer.getGson();
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}