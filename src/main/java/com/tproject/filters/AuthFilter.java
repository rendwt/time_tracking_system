package com.tproject.filters;

import com.tproject.services.impl.JWTServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebFilter(filterName = "AuthenticationFilter",
        urlPatterns = {"/test/*"} )
public class AuthFilter extends HttpFilter {

    private final List<String> ALLOWED_URL = List.of("/login");

    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (ALLOWED_URL.contains(pathInfo)){
            chain.doFilter(request, response);
        }
        else {
            String authorization = request.getHeader("Authorization");

            if (authorization == null || !authorization.matches("Bearer .+")){
                System.out.println("Auth token is absent");
                response.setContentType("application/json");
                response.setStatus(401);
                PrintWriter out = response.getWriter();
                out.println("Auth token is absent");
                return;
            }

            String token = authorization.replaceAll("(Bearer)", "").trim();

            JWTServiceImpl jwtService = JWTServiceImpl.getInstance();
            Jws<Claims> claims;

            //check token
            try {
                claims = jwtService.verifyUserToken(token);
            } catch (JwtException e) {
                System.out.println("Bad token");
                response.setContentType("application/json");
                response.setStatus(401);
                PrintWriter out = response.getWriter();
                out.println("Bad token");
                return;
            }
            chain.doFilter(request, response);
        }
    }
}
