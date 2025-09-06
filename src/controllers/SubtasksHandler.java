package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import model.Subtask;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    if (path.equals("/subtasks")) {
                        handleGetAllSubtasks(exchange);
                    } else {
                        handleGetSubtaskById(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateSubtask(exchange);
                    break;
                case "DELETE":
                    if (path.equals("/subtasks")) {
                        handleDeleteAllSubtasks(exchange);
                    } else {
                        handleDeleteSubtaskById(exchange);
                    }
                    break;
                default:
                    sendBadRequest(exchange, "Unsupported method");
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        String response = gson.toJson(subtasks);
        sendSuccess(exchange, response);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = parseIdFromPath(exchange.getRequestURI().getPath());
        if (idOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid subtask ID");
            return;
        }

        try {
            Subtask subtask = taskManager.getSubtaskById(idOpt.get());
            if (subtask == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(subtask);
                sendSuccess(exchange, response);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask.getId() == 0) {
                int id = taskManager.createSubtask(subtask);
                sendCreated(exchange, "{\"id\": " + id + "}");
            } else {
                taskManager.updateSubtask(subtask);
                sendCreated(exchange, "{\"message\": \"Subtask updated\"}");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Epic not found")) {
                sendNotFound(exchange);
            } else {
                sendHasInteractions(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllSubtasks();
        sendSuccess(exchange, "{\"message\": \"All subtasks deleted\"}");
    }

    private void handleDeleteSubtaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = parseIdFromPath(exchange.getRequestURI().getPath());
        if (idOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid subtask ID");
            return;
        }

        try {
            taskManager.deleteSubtaskById(idOpt.get());
            sendSuccess(exchange, "{\"message\": \"Subtask deleted\"}");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}