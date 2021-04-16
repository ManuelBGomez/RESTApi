package gal.usc.etse.grei.es.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Clase que representa la autenticación de usuarios. Se añade para poder documentar el login
 * mediante swagger.
 *
 * Clase proporcionada por los profesores de la materia.
 */

@RestController
@RequestMapping("/login")
@Tag(name = "Authentication API", description = "Authentication operations")
public class AuthController {
    /**
     * Método que representa al inicio de sesión que se puede realizar en la aplicación.
     * @param userpass Credenciales de acceso.
     */
    @PostMapping()
    @Operation(
            operationId = "login",
            summary = "login",
            description = "Login with username and password to obtain a JWT token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Valid credentials - JWT token returned",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content
            )
    })
    public void login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User and password for authentication",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value= "{\"email\": \"test@test.com\", \"password\": \"test\"}"
                            )
                    )
            )
            @RequestBody Map<String, String> userpass
    ) { }
}
