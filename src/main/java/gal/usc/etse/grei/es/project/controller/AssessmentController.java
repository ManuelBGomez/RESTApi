package gal.usc.etse.grei.es.project.controller;


import gal.usc.etse.grei.es.project.errorManagement.ErrorObject;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.model.validation.createValidation;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Clase AssessmentController -> Uri para llegar: /assessments
 * Gestión de peticiones específicas de comentarios.
 *
 * @author Manuel Bendaña
 */
@RestController
@Tag(name = "Comments API", description = "User comments related operations")
@SecurityRequirement(name = "JWT")
@RequestMapping("comments")
public class AssessmentController {

    private final AssessmentService assessments;
    //Referencia a un linkrelationprovider para los hateoas
    private final LinkRelationProvider relationProvider;

    /**
     * Constructor de la clase
     * @param assessments Referencia al servicio de comentarios.
     * @param relationProvider Referencia al objeto LinkRelationProvider
     */
    @Autowired
    public AssessmentController(AssessmentService assessments, LinkRelationProvider relationProvider) {
        this.assessments = assessments;
        this.relationProvider = relationProvider;
    }

    /**
     * Método: POST
     * Url para llegar: /comments
     * Objetivo: añadir un comentario para la película indicada en la petición.
     * Permisos: todos los usuarios logueados
     * Enlaces devueltos: a la película y a su lista de comentarios
     *
     * @param assessment los datos del comentario a añadir, incluyendo el usuario que lo hace.
     * @return El comentario introducido en la base de datos.
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "createComment",
            summary = "Create a new comment for a movie",
            description = "Create a new comment for any movie. All authenticated users will have permissions to do this," +
                    " but only one comment per user in every movie is allowed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created correctly",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Film of the comment",
                                    description = "HATEOAS Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "All comments",
                                    description = "HATEOAS All Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Created comment",
                                    description = "Created comment location",
                                    schema = @Schema(type = "Location")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format in some parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie or user not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The requested user already made a comment for the requested movie",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Assessment> addComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Comment data for creation",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"rating\": \"5\"," +
                                            "\"user\":{\"email\": \"test@test.com\"}," +
                                            "\"movie\":{\"id\": \"99968\"}," +
                                            "\"comment\": \"Test comment!\"}"
                            )
                    )
            )
            @RequestBody @Validated(createValidation.class) Assessment assessment
    ){
        //Intentamos añadir el comentario:
        //Se recupera la información de autenticación y se sustituye el nombre:
        Authentication authInfo = SecurityContextHolder.getContext().getAuthentication();
        assessment.setUser(new User().setEmail(authInfo.getName()));
        Assessment comment = assessments.addComment(assessment);
        //Preparamos enlaces para devolver
        //A la pelicula:
        Link film = linkTo(methodOn(MovieController.class).get(comment.getMovie().getId()))
                .withRel(relationProvider.getItemResourceRelFor(Film.class));
        //A los comentarios de la película:
        Link all = linkTo(methodOn(MovieController.class).getComments(0, 20, null, comment.getMovie().getId()))
                .withRel(relationProvider.getCollectionResourceRelFor(Assessment.class));
        //Devolvemos un estado Created con los datos del comentario añadido y los enlaces
        return ResponseEntity.created(URI.create(Constants.URL + "/comments/" + assessment.getId()))
                .header(HttpHeaders.LINK, film.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(comment);
    }

    /**
     * Método: PATCH
     * Url para llegar: /comments/{commentId}
     * Objetivo: modificar el comentario cuyo id se indica en la URL, con los datos indicados en el body.
     * Permisos: exclusivamente el autor del comentario.
     * Enlaces devueltos: al comentario, a la lista de comentarios de la película y a la lista de comentarios
     *      del usuario.
     *
     * @param commentId El identificador del comentario que se quiere modificar.
     * @param updates El comentario a modificar.
     * @return El comentario modificado, tal y como ha quedado almacenado.
     */
    @PatchMapping(
            path = "{commentId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = "application/json-patch+json"
    )
    @PreAuthorize("@assessmentService.isUserComment(principal, #commentId)")
    @Operation(
            operationId = "updateComment",
            summary = "Update comment information",
            description = "Update comment for a movie, giving its id via url and the requested modifications in jsonPatch format." +
                    " Only the comment author will be allowed to update its comment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment updated correctly",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self comment",
                                    description = "HATEOAS Self Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Film comments",
                                    description = "HATEOAS Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "User comments",
                                    description = "HATEOAS Link",
                                    schema = @Schema(type = "Link")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format in some parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Invalid format of JSON patch",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Assessment> modifyComment(
            @Parameter(name = "id", description = "Comment to modify id", example = "607416fb7e2a243f8c0c6c0c")
            @PathVariable("commentId") String commentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Modifications to movie list",
                    content = @Content(
                            mediaType = "application/json-patch+json",
                            examples = @ExampleObject(
                                    value = "[{\"op\": \"replace\", \"path\": \"/rating\", \"value\": 5}]"
                            )
                    )
            )
            @RequestBody List<Map<String, Object>> updates
    ){
        //Se intenta hacer la actualización y se devuelve el resultado:
        Assessment assessment = assessments.modifyComment(commentId, updates);

        //Se elaboran los enlaces:
        //A si mismo:
        Link self = linkTo(methodOn(AssessmentController.class).modifyComment(commentId, updates)).withSelfRel();

        //A los comentarios de la película:
        Link filmComments = linkTo(methodOn(MovieController.class).getComments(0, 20, null,
                assessment.getMovie().getId()))
                .withRel(relationProvider.getCollectionResourceRelFor(Assessment.class));

        //A la lista de comentarios del usuario:
        Link userComments = linkTo(methodOn(UserController.class).getUserComments(0, 20, null,
                assessment.getUser().getEmail()))
                .withRel(relationProvider.getCollectionResourceRelFor(Assessment.class));

        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, filmComments.toString())
                .header(HttpHeaders.LINK, userComments.toString())
                .body(assessment);
    }

    /**
     * Método: DELETE
     * Url para llegar: comments/{commentId}
     * Objetivo: borrar el comentario cuyo id se indica en la URL.
     * Permisos: el autor del comentario o un administrador.
     * Enlaces devueltos: a la lista de comentarios de la película y a la del usuario.
     *
     * @param commentId El comentario a borrar
     * @return Estado correcto si se borra correctamente, estado erróneo en otro caso.
     */
    @DeleteMapping(
            path = "{commentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "deleteComment",
            summary = "Delete comment by id",
            description = "Delete a comment made by an user for a movie, giving its id." +
                    " Only the comment author and users with admin permissions will be allowed to do this."
    )
    @PreAuthorize("hasRole('ADMIN') or @assessmentService.isUserComment(principal, #commentId)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Comment deleted correctly",
                    content = @Content,
                    headers = {
                            @Header(
                                    name = "Film comments",
                                    description = "HATEOAS Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "User comments",
                                    description = "HATEOAS Link",
                                    schema = @Schema(type = "Link")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Object> deleteComment(
            @Parameter(name = "id", description = "Comment to delete id", example = "607416fb7e2a243f8c0c6c0c")
            @PathVariable("commentId") String commentId
    ){
        //Recuperamos el usuario y la película antes (para luego poder devolver los enlaces):
        String user = assessments.getUserId(commentId);
        String movieId = assessments.getMovieId(commentId);
        //Se intenta borrar la película
        assessments.deleteComment(commentId);

        //Se preparan los enlaces:
        //A los comentarios de la película:
        Link filmComments = linkTo(methodOn(MovieController.class).getComments(0, 20, null, movieId))
                .withRel(relationProvider.getCollectionResourceRelFor(Assessment.class));

        //A la lista de comentarios del usuario:
        Link userComments = linkTo(methodOn(UserController.class).getUserComments(0, 20, null, user))
                .withRel(relationProvider.getCollectionResourceRelFor(Assessment.class));

        //Se devuelve una respuesta correcta vacía (si se llega a este punto se pudo ejecutar el borrado):
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, filmComments.toString())
                .header(HttpHeaders.LINK, userComments.toString())
                .build();
    }
}
