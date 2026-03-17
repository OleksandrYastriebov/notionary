package com.api.wishoria.controller.docs;

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
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = @ExampleObject(value = "{" +
                                "\"statusCode\": 403," +
                                "\"errorMessage\": \"You don't have permissions.\"," +
                                "\"message\": \"Business rule violation.\"," +
                                "\"details\": \"/api/{action}\"}")))
})
public @interface ApiForbiddenErrorDoc {
}
