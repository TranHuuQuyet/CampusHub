package com.campushub.common.security;

import com.campushub.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;    
import org.springframework.http.HttpStatus; 
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.http.MediaType;
import java.io.PrintWriter;
import javax.print.attribute.standard.Media;
import org.springframework.stereotype.Component;

@Component  
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override 
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        //Step 1: Status
        res.setStatus(HttpStatus.UNAUTHORIZED.value());

        //Step 2: Content Type
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");

        //Step 3: Body (ApiErrorResponse)
        ApiErrorResponse body = ApiErrorResponse.of (
            HttpStatus.UNAUTHORIZED.value(),
            "UNAUTHORIZED",
            "Authentication is required to access this resource"
        );
        //Step 4: Serialize to Json and send it to the client   
        objectMapper.writeValue(res.getOutputStream(), body);

    }

}
