package com.example.webapp.authorisation;

import com.example.webapp.model.ErrorResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.naming.AuthenticationException;
import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class BasicAuth {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private BasicAuthEntryPoint basicAuthEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        /* This function will be used to encode the user's password using
        BCrypt algorithm*/

        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {

        /* This function is used to get the user authorities and credentials
         * for user access verification*/
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("select username, psswrd, enabled "
                        + "from webapp "
                        + "where username = ?")
                .authoritiesByUsernameQuery("select username, authority "
                        + "from webapp "
                        + "where username = ?");
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() throws Exception{
        return (web) -> web.ignoring().requestMatchers("/images/**","/js/**","/webjars/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        /* This function is used to set authentication requirements for particular
         * endpoints. Endpoints that are unauthenticated/ public are also included
         * in the function*/

        http.csrf().disable().authorizeRequests()
                .requestMatchers("/healthz")
                .permitAll()
                .and()
                .authorizeRequests()
                .requestMatchers("/v1/user")
                .permitAll()
                .and()
                .authorizeRequests()
                .requestMatchers("/v1/product/{productIdStr}")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .authenticationEntryPoint(basicAuthEntryPoint);
        http.addFilterAfter(new CustomFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

}
