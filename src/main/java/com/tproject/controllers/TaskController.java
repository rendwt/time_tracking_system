package com.tproject.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tproject.annotations.Controller;
import com.tproject.annotations.HttpMethod;
import com.tproject.annotations.RequestMapping;
import com.tproject.dto.TaskDto;
import com.tproject.exception.CustomSQLException;
import com.tproject.services.impl.JWTServiceImpl;
import com.tproject.services.impl.TaskServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

@Controller
public class TaskController {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final TaskServiceImpl taskService = TaskServiceImpl.getInstance();

    private HttpServletResponse sendError(int errorCode, String errorReason, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(errorCode);
        PrintWriter out = resp.getWriter();
        out.println(errorReason);
        return resp;
    }

    private boolean validateTaskDto(TaskDto taskDto) {

        return taskDto != null && taskDto.getHours() == 0 && !taskDto.getDate().isEqual(null) && !taskDto.getDescription().isEmpty();
    }

    @RequestMapping(url = "/list", method = HttpMethod.GET)
    public HttpServletResponse getAllTasks(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            Jws<Claims> jws = JWTServiceImpl.getInstance().verifyUserToken(req.getHeader("Authorization"));
            String username = jws.getBody().get("user", String.class);

            Collection<TaskDto> tasks = taskService.getAllTasks(username);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();

            if (tasks.isEmpty())
                out.println("[]");
            else
                out.println(jsonMapper.writeValueAsString(tasks));

            return resp;
        }  catch (JwtException e) {
            return sendError(401, "Unauthorized: Invalid token", resp);
        }catch (CustomSQLException e) {
            return sendError(500, e.getMessage(), resp);
        }
    }

    @RequestMapping(url = "/list", method = HttpMethod.DELETE)
    public HttpServletResponse deleteTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int taskId = Integer.parseInt(req.getParameter("id"));

            if (taskService.deleteTask(taskId))
                resp.setStatus(204);
            return resp;
        } catch (CustomSQLException e) {
            return sendError(500, e.getMessage(), resp);
        }
    }

    @RequestMapping(url = "/task", method = HttpMethod.GET)
    public HttpServletResponse getTaskById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int taskId = Integer.parseInt(req.getParameter("id"));

            TaskDto task = taskService.getTaskById(taskId);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.println(jsonMapper.writeValueAsString(task));
            return resp;
        } catch (CustomSQLException e) {
            return sendError(500, e.getMessage(), resp);
        }
    }

    @RequestMapping(url = "/task", method = HttpMethod.POST)
    public HttpServletResponse addTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String taskData = req.getReader().lines().reduce("", String::concat);
            JsonNode jsonNode = jsonMapper.readTree(taskData);
            TaskDto newTask = jsonMapper.treeToValue(jsonNode, TaskDto.class);

            if (validateTaskDto(newTask)) {
                if (taskService.createTask(newTask).isPresent()) {
                    resp.setStatus(201);
                }
                return resp;
            } else {
                return sendError(400, "Invalid task data", resp);
            }
        } catch (CustomSQLException e) {
            return sendError(500, e.getMessage(), resp);
        }
    }

    @RequestMapping(url = "/task", method = HttpMethod.PUT)
    public HttpServletResponse updateTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String taskIdParam = req.getParameter("id");

            if (taskIdParam != null) {
                String taskData = req.getReader().lines().reduce("", String::concat);
                TaskDto updatedTask = jsonMapper.readValue(taskData, TaskDto.class);
                if (validateTaskDto(updatedTask)) {
                    taskService.updateTask(updatedTask);
                    resp.setStatus(200);
                } else {
                    return sendError(400, "Invalid task data", resp);
                }
                return resp;
            } else {
                return sendError(400, "Task ID not specified", resp);
            }
        } catch (CustomSQLException e) {
            return sendError(500, e.getMessage(), resp);
        }
    }
}
