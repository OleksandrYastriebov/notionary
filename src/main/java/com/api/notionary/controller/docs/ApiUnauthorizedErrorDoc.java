package com.api.notionary.controller.docs;

import com.api.notionary.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.api.notionary.util.constants.Constant.JSON_CONTENT_TYPE;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing",
                content = @Content(mediaType = JSON_CONTENT_TYPE,
                        examples = @ExampleObject(value = "{" +
                                "\"statusCode\": 401," +
                                "\"errorMessage\": \"Full authentication is required to access this resource.\"," +
                                "\"message\": \"Invalid Email or Password.\"," +
                                "\"details\": \"/api/{action}\"}"))),
})
public @interface ApiUnauthorizedErrorDoc {
}
