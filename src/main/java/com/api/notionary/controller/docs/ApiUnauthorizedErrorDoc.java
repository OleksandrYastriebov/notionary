package com.api.notionary.controller.docs;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = @ExampleObject(value = "{" +
                                "\"statusCode\": 401," +
                                "\"errorMessage\": \"Full authentication is required to access this resource.\"," +
                                "\"message\": \"Invalid Email or Password.\"," +
                                "\"details\": \"/api/{action}\"}"))),
})
public @interface ApiUnauthorizedErrorDoc {
}
