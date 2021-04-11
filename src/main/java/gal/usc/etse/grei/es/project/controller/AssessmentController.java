package gal.usc.etse.grei.es.project.controller;


import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.utilities.Constants;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    ResponseEntity<Assessment> addComment(@RequestBody @Valid Assessment assessment){
        //Intentamos añadir el comentario:
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
    ResponseEntity<Assessment> modifyComment(@PathVariable("commentId") String commentId,
                                             @RequestBody List<Map<String, Object>> updates){
        //Se intenta hacer la actualización y se devuelve el resultado:
        Assessment assessment = assessments.modifyComment(commentId, updates);

        //Se elaboran los enlaces:
        //A si mismo
        Link self = linkTo(methodOn(AssessmentController.class).modifyComment(commentId, updates)).withSelfRel();

        //A los comentarios de la película:
        Link filmComments = linkTo(methodOn(MovieController.class).getComments(0, 20, null, assessment.getMovie().getId()))
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
    @PreAuthorize("hasRole('ADMIN') or @assessmentService.isUserComment(principal, #commentId)")
    ResponseEntity<Object> deleteComment(@PathVariable("commentId") String commentId){
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
