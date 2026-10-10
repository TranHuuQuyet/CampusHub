package com.campushub.common.security;


import com.campushub.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;    
import org.springframework.http.HttpStatus; 
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override 
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException{
        //Step 1 Status
        res.setStatus(HttpStatus.FORBIDDEN.value());
        
        //Step 2: ContentType
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");

        //Step 3: Body (ApiErrorResponse)
        ApiErrorResponse body = ApiErrorResponse.of(
            HttpStatus.FORBIDDEN.value(),
            "FORBIDDEN",
            "You do not have permission to access this resource"
        );
        //Step 4: Serialize to Json and send it to the client
        objectMapper.writeValue(res.getOutputStream(), body);
    }
}