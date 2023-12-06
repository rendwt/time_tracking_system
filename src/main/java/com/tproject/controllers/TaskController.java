package com.tproject.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tproject.annotations.Controller;
import com.tproject.annotations.HttpMethod;
import com.tproject.annotations.RequestMapping;
import com.tproject.dto.TaskDto;
import com.tproject.exception.CustomSQLException;
import com.tproject.services.impl.TaskServiceImpl;

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

    @RequestMapping(url = "/list", method = HttpMethod.GET)
    public HttpServletResponse getAllTasks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int userId = Integer.parseInt(req.getParameter("user_id"));
            Collection<TaskDto> tasks = taskService.getAllTasks(userId);
            resp.setContentType("application/json");
            PrintWriter out = resp.getWriter();
            out.println(jsonMapper.writeValueAsString(tasks));
            return resp;
        } catch (CustomSQLException e) {
            return sendError(500, e.getMessage(), resp);
        }
    }

    @RequestMapping(url = "/list", method = HttpMethod.DELETE)
    public HttpServletResponse deleteTask(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int taskId = Integer.parseInt(req.getParameter("id"));
            taskService.deleteTask(taskId);
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
            TaskDto newTask = jsonMapper.readValue(taskData, TaskDto.class);

            if(taskService.createTask(newTask).isPresent())
                resp.setStatus(201);
            return resp;
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

                taskService.updateTask(updatedTask);

                return resp;
            } else {
                return sendError(400, "Task ID not specified", resp);
            }
        } catch (CustomSQLException e) {
            return sendError(500, e.getMessage(), resp);
        }
    }
}
