package com.tproject.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tproject.annotations.Controller;
import com.tproject.annotations.HttpMethod;
import com.tproject.annotations.RequestMapping;
import com.tproject.entity.Credentials;
import com.tproject.services.impl.JWTServiceImpl;
import com.tproject.services.impl.UserServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class AuthController {
    private static final String AUTH_COOKIE = "auth";

    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final UserServiceImpl userService = UserServiceImpl.getInstance();

    private HttpServletResponse sendError(int errorCode, String errorReason, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(errorCode);
        PrintWriter out = resp.getWriter();
        out.println(errorReason);
        return resp;
    }


    @RequestMapping(url = "/login", method = HttpMethod.POST)
    public HttpServletResponse login(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Credentials anonymous = jsonMapper.readValue(req.getReader(), Credentials.class);

        if (userService.verifyUser(anonymous.getUsername(),anonymous.getPassword())) {
            JWTServiceImpl jwtService = JWTServiceImpl.getInstance();
            String token = jwtService.buildUserToken(anonymous);

            resp.setContentType("application/json");
            resp.setHeader("Access-Control-Expose-Headers", "Authorization");
            resp.setHeader("Authorization", "Bearer " + token);
            return resp;

        } else {
            System.out.println("=========Login and/or password is incorrect===========");
            sendError(401, "Login and/or password is incorrect", resp);
        }
        return resp;
    }
}
