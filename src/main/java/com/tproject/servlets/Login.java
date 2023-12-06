package com.tproject.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tproject.dto.UserDto;
import com.tproject.entity.Credentials;
import com.tproject.services.impl.UserServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/loginOLD")
public class Login extends HttpServlet {

    private static final String AUTH_COOKIE = "auth";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cookie userNameCookieRemove = new Cookie(AUTH_COOKIE, "");
        userNameCookieRemove.setMaxAge(0);
        resp.addCookie(userNameCookieRemove);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String reqBody = req.getReader().lines().reduce("",String::concat);
        Credentials anonymous = objectMapper.readValue(reqBody, Credentials.class);
        System.out.println(reqBody);
        resp.setContentType("application/json");
//        try {
            System.out.println(anonymous.toString());
            System.out.println(UserServiceImpl.getInstance());

            Optional<UserDto> credentials = Optional.of(
                    UserServiceImpl.getInstance().findUserByUsername(anonymous.getUsername()));

            System.out.println("doPost=====================" + credentials.get().getUsername());


            if (DigestUtils.sha256Hex(anonymous.getPassword()).equals(credentials.get().getPassword())) {
                //auth cookie
                String authCookie = DigestUtils.sha256Hex(anonymous.getUsername());
                Cookie authUiCookie = new Cookie(AUTH_COOKIE, authCookie);
                resp.addCookie(authUiCookie);
                authUiCookie.setDomain(req.getContextPath());

            } else {
                //error
            }

//        } catch (NonExistentEntityException ex) {
////            LOGGER.log(Level.WARNING, ex.getMessage());
//        }
    }
}
