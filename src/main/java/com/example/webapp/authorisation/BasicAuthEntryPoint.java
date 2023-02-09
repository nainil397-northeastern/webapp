package com.example.webapp.authorisation;

import com.example.webapp.model.ErrorResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.h2.util.json.JSONObject;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.io.PrintWriter;


@Component
public class BasicAuthEntryPoint extends BasicAuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException {
        /* This function generates basic auth token and is used for user
         * credential validation*/

        PrintWriter out = response.getWriter();

        response.addHeader("WWW-Authenticate", "Basic realm= + getRealmName() + ");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponseModel errorResponse = new ErrorResponseModel();
        errorResponse.setErr("Unauthorized");
        errorResponse.setStatus(401);
        errorResponse.setMessage("Invalid credentials. User access denied.");

        ObjectMapper objMapper = new ObjectMapper();
        String jsonString = objMapper.writeValueAsString(errorResponse);
        out.print(jsonString);
        out.flush();
    }

    @Override
    public void afterPropertiesSet(){
        setRealmName("spring");
        super.afterPropertiesSet();
    }


}
