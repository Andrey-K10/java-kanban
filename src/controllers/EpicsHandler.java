package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import model.Epic;
import model.Subtask;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    if (path.equals("/epics")) {
                        handleGetAllEpics(exchange);
                    } else if (path.contains("/subtasks")) {
                        handleGetEpicSubtasks(exchange);
                    } else {
                        handleGetEpicById(exchange);
                    }
                    break;
                case "POST":
                    handleCreateOrUpdateEpic(exchange);
                    break;
                case "DELETE":
                    if (path.equals("/epics")) {
                        handleDeleteAllEpics(exchange);
                    } else {
                        handleDeleteEpicById(exchange);
                    }
                    break;
                default:
                    sendBadRequest(exchange, "Unsupported method");
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        String response = gson.toJson(epics);
        sendSuccess(exchange, response);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = parseIdFromPath(exchange.getRequestURI().getPath());
        if (idOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid epic ID");
            return;
        }

        try {
            Epic epic = taskManager.getEpicById(idOpt.get());
            if (epic == null) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(epic);
                sendSuccess(exchange, response);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length < 4) {
            sendBadRequest(exchange, "Invalid path");
            return;
        }

        try {
            int epicId = Integer.parseInt(parts[2]);
            List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epicId);
            String response = gson.toJson(subtasks);
            sendSuccess(exchange, response);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Invalid epic ID");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic.getId() == 0) {
                int id = taskManager.createEpic(epic);
                sendCreated(exchange, "{\"id\": " + id + "}");
            } else {
                taskManager.updateEpic(epic);
                sendCreated(exchange, "{\"message\": \"Epic updated\"}");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        taskManager.deleteAllEpics();
        sendSuccess(exchange, "{\"message\": \"All epics deleted\"}");
    }

    private void handleDeleteEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = parseIdFromPath(exchange.getRequestURI().getPath());
        if (idOpt.isEmpty()) {
            sendBadRequest(exchange, "Invalid epic ID");
            return;
        }

        try {
            taskManager.deleteEpicById(idOpt.get());
            sendSuccess(exchange, "{\"message\": \"Epic deleted\"}");
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}