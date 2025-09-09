package controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetPrioritized(exchange);
            } else {
                sendBadRequest(exchange, "Unsupported method");
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        List<Task> prioritized = taskManager.getPrioritizedTasks();
        String response = gson.toJson(prioritized);
        sendSuccess(exchange, response);
    }
}