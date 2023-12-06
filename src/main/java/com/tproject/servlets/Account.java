package com.tproject.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tproject.dao.UserDao;
import com.tproject.dao.impl.UserDaoImpl;
import com.tproject.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;


@WebServlet("/hidden")
public class Account extends HttpServlet {
    private static final UserDao<User, Integer> USER_DAO = UserDaoImpl.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Optional<String> auth = readCookie("auth", req);
        if (auth.isPresent()) {
            Collection<User> users = USER_DAO.getAll();
            String json = objectMapper.writeValueAsString(users);
            resp.setContentType("application/json");
            PrintWriter printWriter = resp.getWriter();
            printWriter.write(json);
            printWriter.close();

        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access");
        }
    }

    public Optional<String> readCookie(String key, HttpServletRequest req) {
        return Arrays.stream(req.getCookies())
                .filter(c -> key.equals(c.getName()))
                .map(Cookie::getValue)
                .findAny();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String reqBody = req.getReader().lines().reduce("", String::concat);
        User editedUser = objectMapper.readValue(reqBody, User.class);
        resp.setContentType("application/json");
        USER_DAO.updateUser(editedUser);
    }
}
