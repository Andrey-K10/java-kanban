package controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetHistory(exchange);
            } else {
                sendBadRequest(exchange, "Unsupported method");
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        String response = gson.toJson(history);
        sendSuccess(exchange, response);
    }
}