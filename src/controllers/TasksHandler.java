package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    if (path.equals("/tasks")) {
                        handleGetAllTasks(exchange);
                    } else {
                        handleGetTaskById(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateTask(exchange);
                    break;
                case "DELETE":
                    if (path.equals("/tasks")) {
                        handleDeleteAllTasks(exchange);
                    } else {
                        handleDeleteTaskById(exchange);
                    }
                    break;
                default:
                    sendBadRequest(exchange, "Unsupported method");
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        String response = gson.toJson(tasks);
        sendSuccess(exchange, response);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = parseIdFromPath(exchange.getRequestURI().getPath());
        if (idOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid task ID");
            return;
        }

        try {
            Task task = taskManager.getTaskById(idOpt.get());
            if (task == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(task);
                sendSuccess(exchange, response);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);

            if (task.getId() == 0) {
                int id = taskManager.createTask(task);
                sendCreated(exchange, "{\"id\": " + id + "}");
            } else {
                taskManager.updateTask(task);
                sendCreated(exchange, "{\"message\": \"Task updated\"}");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllTasks();
        sendSuccess(exchange, "{\"message\": \"All tasks deleted\"}");
    }

    private void handleDeleteTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = parseIdFromPath(exchange.getRequestURI().getPath());
        if (idOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid task ID");
            return;
        }

        try {
            taskManager.deleteTaskById(idOpt.get());
            sendSuccess(exchange, "{\"message\": \"Task deleted\"}");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}