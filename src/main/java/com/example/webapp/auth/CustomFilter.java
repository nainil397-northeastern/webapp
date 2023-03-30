package com.example.webapp.auth;

import com.example.webapp.controller.UserAccountController;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.GenericFilterBean;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.IOException;

public class CustomFilter  extends GenericFilterBean {

    @Value("${publish.metrics}")
    boolean publishMetrics;

    @Value("${publish.service.hostname}")
    String metricsServiceHostname;

    @Value("${publish.service.port}")
    int metricsServicePort;

    @Value("${publish.service.prefix}")
    String metricsServicePrefix;

    public StatsDClient statsd = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    private final Logger logger = LoggerFactory.getLogger(CustomFilter.class);

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/user/")){

            statsd.incrementCounter("endpoint.get.v1.user");
            logger.info("Entered: GET /v1/user/userId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("PUT") && ((HttpServletRequest)request).getRequestURI().contains("/v1/user/")){

            statsd.incrementCounter("endpoint.put.v1.user");
            logger.info("Entered: PUT /v1/user/userId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product")){

            statsd.incrementCounter("endpoint.post.v1.product");
            logger.info("Entered: POST /v1/product");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("PUT") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.put.v1.product");
            logger.info("Entered: PUT /v1/product/productId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("PATCH") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.patch.v1.product");
            logger.info("Entered: PATCH /v1/product/productId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("DELETE") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.delete.v1.product");
            logger.info("Entered: DELETE /v1/product/productId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image")){

            statsd.incrementCounter("endpoint.post.v1.product.image");
            logger.info("Entered: POST /v1/product/productId/image");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image/")){

            statsd.incrementCounter("endpoint.get.v1.product.image");
            logger.info("Entered: GET /v1/product/productId/image/imageId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image")){

            statsd.incrementCounter("endpoint.get.v1.product.images");
            logger.info("Entered: GET /v1/product/productId/image");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("DELETE") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image/")){

            statsd.incrementCounter("endpoint.delete.v1.product.image");
            logger.info("Entered: DELETE /v1/product/productId/image/imageId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product")){

            statsd.incrementCounter("endpoint.post.v1.product");
            logger.info("Entered: POST /v1/product");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("PUT") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.put.v1.product");
            logger.info("Entered: PUT /v1/product/productId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("PATCH") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.patch.v1.product");
            logger.info("Entered: PATCH /v1/product/productId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("DELETE") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")) {

            statsd.incrementCounter("endpoint.delete.v1.product");
            logger.info("Entered: DELETE /v1/product/productId");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/healthz")){

            statsd.incrementCounter("endpoint.get.v1.healthz");
            logger.info("Entered: GET /v1/healthz");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/user")){

            statsd.incrementCounter("endpoint.post.v1.user");
            logger.info("Entered: POST /v1/user");

        }else if(((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")){

            statsd.incrementCounter("endpoint.get.v1.product");
            logger.info("Entered: GET /v1/product/productId");

        }else{
            int i = 0;
        }

if((((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/user/")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("PUT") && ((HttpServletRequest)request).getRequestURI().contains("/v1/user/")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image/")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("GET") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("DELETE") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/") && ((HttpServletRequest)request).getRequestURI().contains("/image/")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("POST") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("PUT") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("PATCH") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/")) ||
                (((HttpServletRequest)request).getMethod().equalsIgnoreCase("DELETE") && ((HttpServletRequest)request).getRequestURI().contains("/v1/product/"))){
            logger.info("User successfully authenticated.");
        }



        chain.doFilter(request, response);
    }
}
