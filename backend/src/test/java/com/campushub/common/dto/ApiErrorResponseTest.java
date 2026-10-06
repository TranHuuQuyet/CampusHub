package com.campushub.common.dto;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class ApiErrorResponseTest {
    @Test 
    void shouldCreateErrorResponseWithoutFields() {
        ApiErrorResponse response = new ApiErrorResponse(400, "RESOURCE_NOT_FOUND", "User not found", null);    

        //assert
        assertEquals(400, response.getStatus());
        assertEquals("RESOURCE_NOT_FOUND", response.getError());
        assertEquals("User not found", response.getMessage());
        assertNull(response.getFields());
        assertNotNull(response.getTimestamp());
    }
    @Test 
    void shouldCreateErrorResponseWithFields() {
        Map<String, String> fields = Map.of("username", "Username is required");
        ApiErrorResponse response = new ApiErrorResponse(400, "BAD_REQUEST", "Validation failed", fields);

        //assert
        assertEquals(400, response.getStatus());
        assertEquals("BAD_REQUEST", response.getError());
        assertEquals("Validation failed", response.getMessage());
        assertEquals(fields, response.getFields());
        assertNotNull(response.getTimestamp());
    }
    @Test
    void shouldCreateErrorResponseUsingOfMethod() {
        //act
        ApiErrorResponse response = ApiErrorResponse.of(400, "VALIDATION_ERROR", "Validation failed", Map.of("username", "Username is required"));
        
        //assert
        assertEquals(400, response.getStatus());
        assertEquals("VALIDATION_ERROR", response.getError());
        assertEquals("Validation failed", response.getMessage());
        assertNotNull(response.getFields());
        assertEquals(1 , response.getFields().size());
        assertEquals("Username is required", response.getFields().get("username"));
        assertNotNull(response.getTimestamp());
    }
}