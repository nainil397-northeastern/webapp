package com.example.webapp.auth;

import com.example.webapp.controller.UserAccountController;
import com.example.webapp.model.ErrorResponseModel;
import com.example.webapp.service.UserAccountServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class MyBasicAuthEntryPoint extends BasicAuthenticationEntryPoint {

    public StatsDClient statsd = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    private final Logger logger = LoggerFactory.getLogger(MyBasicAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException {
        /* This function generates basic auth token and is used for user
        * credential validation*/

       if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/user/")){

            statsd.incrementCounter("endpoint.get.v1.user");
            logger.info("Encountered endpoint : (GET) /v1/user/userId");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("PUT") && ((HttpServletRequest)request).getRequestURI().contains("/v1/user/")){

            statsd.incrementCounter("endpoint.put.v1.user");
            logger.info("Encountered endpoint : (PUT) /v1/user/userId");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image")){

            statsd.incrementCounter("endpoint.post.v1.product.image");
            logger.info("Encountered endpoint : (POST) /v1/product/productId/image");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image/")){

            statsd.incrementCounter("endpoint.get.v1.product.image");
            logger.info("Encountered endpoint : (GET) /v1/product/productId/image/imageId");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image")){

            statsd.incrementCounter("endpoint.get.v1.product.images");
            logger.info("Encountered endpoint : (GET) /v1/product/productId/image");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("DELETE") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image/")){

            statsd.incrementCounter("endpoint.delete.v1.product.image");
            logger.info("Encountered endpoint : (DELETE) /v1/product/productId/image/imageId");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product")){

            statsd.incrementCounter("endpoint.post.v1.product");
            logger.info("Encountered endpoint : (POST) /v1/product");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("PUT") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.put.v1.product");
            logger.info("Encountered endpoint : (PUT) /v1/product/productId");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("PATCH") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.patch.v1.product");
            logger.info("Encountered endpoint : (PATCH) /v1/product/productId");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("DELETE") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")) {

            statsd.incrementCounter("endpoint.delete.v1.product");
            logger.info("Encountered endpoint : (DELETE) /v1/product/productId");
            logger.error("Invalid credentials. Error encountered");
            logger.info("Returned invalid credentials error. Application idle.");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/healthz")){

            statsd.incrementCounter("endpoint.get.v1.healthz");
            logger.info("Encountered endpoint : (GET) /v1/healthz");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/user")){

            statsd.incrementCounter("endpoint.post.v1.user");
            logger.info("Encountered endpoint : (POST) /v1/user");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.get.v1.product");
            logger.info("Encountered endpoint : (GET) /v1/product/productId");

        }else{
            int i = 0;
        }

        PrintWriter out = response.getWriter();

        logger.error("Error encountered. User credentials invalid. Username or password incorrect");

        response.addHeader("WWW-Authenticate", "Basic realm= + getRealmName() + ");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponseModel errorResponse = new ErrorResponseModel();
        errorResponse.setErr("Unauthorized");
        errorResponse.setStatus(401);
        errorResponse.setMessage("Invalid credentials. Access denied.");

        ObjectMapper objMapper = new ObjectMapper();
        String jsonString = objMapper.writeValueAsString(errorResponse);

        logger.info("Returned invalid credentials error. Application idle.");

        out.print(jsonString);
        out.flush();
    }

    @Override
    public void afterPropertiesSet(){
        setRealmName("spring");
        super.afterPropertiesSet();
    }
}
