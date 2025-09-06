package tests;

import com.google.gson.Gson;
import model.Epic;
import model.Status;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest extends BaseHttpTest {

    @Test
    public void testCreateEpic() throws Exception {
        Epic epic = new Epic(0, "Test Epic", "Test Description");

        Gson gson = taskServer.getGson();
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        // Проверяем, что эпик добавился в менеджер
        List<Epic> epics = manager.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals("Test Epic", epics.get(0).getName());
        assertEquals(Status.NEW, epics.get(0).getStatus());
    }

    @Test
    public void testGetEpicSubtasks() throws Exception {
        // Сначала создаем эпик и подзадачу
        Epic epic = new Epic(0, "Test Epic", "Test Description");
        int epicId = manager.createEpic(epic);

        // Тестируем получение подзадач эпика
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body()); // Должен быть пустой массив, так как подзадач нет
    }
}