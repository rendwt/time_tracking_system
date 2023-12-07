package com.tproject.servlets;


import com.tproject.annotations.HttpMethod;
import com.tproject.handlers.HandlerMethodHolder;
import com.tproject.handlers.HttpHandler;
import com.tproject.handlers.HttpMapping;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/server/*")
public class Dispatcher extends HttpServlet {

    HandlerMethodHolder controllerHolder = HandlerMethodHolder.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doHandle(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doHandle(request,response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doHandle(request,response);

    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doHandle(request,response);
    }

    private void doHandle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        HttpHandler handler;
        HttpMapping mapping = HttpMapping.builder().path(pathInfo).method(HttpMethod.valueOf(request.getMethod())).build();
        handler = controllerHolder.getControllerMap().get(mapping);
        if (handler == null) {
            handleNotFoundResponce(response);
            return;
        }
        try {
            handler.getMethod().invoke(handler.getHandlerObject(),request,response);
        } catch (ReflectiveOperationException e) {
            throw new IOException(e);
        }

    }

    private HttpServletResponse handleNotFoundResponce(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print("404");
        return response;
    }
}
