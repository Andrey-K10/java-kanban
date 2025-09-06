package tests;

import controllers.HttpTaskServer;
import controllers.Managers;
import controllers.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseHttpTest {
    protected TaskManager manager;
    protected HttpTaskServer taskServer;

    @BeforeEach
    public void setUp() throws Exception {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();

        // Очищаем данные перед каждым тестом
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }
}