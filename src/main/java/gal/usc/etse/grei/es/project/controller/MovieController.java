package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidDataException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.NoResultException;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.utilities.AuxMethods;
import gal.usc.etse.grei.es.project.utilities.Constants;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Clase MovieController -> Url para llegar: /movies
 * Gestión de peticiones relacionadas con las películas y sus datos.
 *
 * @author Manuel Bendaña
 */
@RestController
@RequestMapping("movies")
public class MovieController {
    //Referencia a la clase MovieService:
    private final MovieService movies;
    //Referencia a la clase AssessmentService:
    private final AssessmentService assessments;

    /**
     * Constructor de la clase
     * @param movies Instancia de la clase MovieService
     * @param assessments Instancia de la clase AssessmentService
     */
    @Autowired
    public MovieController(MovieService movies, AssessmentService assessments) {
        this.movies = movies;
        this.assessments = assessments;
    }

    /**
     * Método: GET
     * Url para llegar: /movies
     * Objetivo: recuperar todas las películas en base a diferentes filtros.
     *
     * @param page La página a recuperar
     * @param size Tamaño de la página.
     * @param sort Parámetros de ordenación.
     * @param keywords Palabras clave por las que se puede realizar la búsqueda de películas.
     * @param genres Géneros por los que se puede realizar la búsqueda de películas.
     * @param cast Nombres de los miembros del cast por los que se puede realizar la búsqueda de peliculas.
     * @param crew Nombres de los miembros de crew por los que se puede realizar la búsqueda de películas.
     * @param producers Nombres de los productores por los que se puede realizar la búsqueda de películas.
     * @param day Día de cualquier mes por el que se puede realizar la búsqueda.
     * @param month Mes del año por el que se puede realizar la búsqueda.
     * @param year Año por el cual se puede realizar la búsqueda.
     * @return Películas obtenidas a raíz de la búsqueda. Si no hubiese ninguna, se devolverá un estado de error.
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    ) ResponseEntity get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "keywords", required = false) List<String> keywords,
            @RequestParam(name = "genres", required = false) List<String> genres,
            @RequestParam(name = "producers", required = false) List<String> producers,
            @RequestParam(name = "cast", required = false) List<String> cast,
            @RequestParam(name = "crew", required = false) List<String> crew,
            @RequestParam(name = "releaseDate.day", required = false) Integer day,
            @RequestParam(name = "releaseDate.month", required = false) Integer month,
            @RequestParam(name = "releaseDate.year", required = false) Integer year
    ) {
        //Transformamos la lista de criterios pasada como argumento para que puedan ser procesados en la consulta:
        List<Sort.Order> criteria = AuxMethods.getSortCriteria(sort);

        try {
            //Si la consulta devuelve resultados, se devolverán directamente:
            return ResponseEntity.of(movies.get(page, size, Sort.by(criteria), keywords, genres,
                    cast, crew, producers, day, month, year));
        } catch (NoResultException e) {
            //Si no, se enviará una respuesta personalizada:
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getErrorObject());
        }
    }

    /**
     * Método: GET
     * Url para llegar: /movies/{id}
     * Objetivo: recuperar los datos de la película con el id facilitado.
     *
     * @param id El id de la película cuyos datos se quieren recuperar.
     * @return Si el Id es válido, los datos de la película.
     */
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity get(@PathVariable("id") String id) {
        try {
            //Tratamos de recuperar la película:
            return ResponseEntity.of(movies.get(id));
        } catch (NoResultException e) {
            //Si no se consigue recuperar, capturamos la excepción lanzada y enviamos mensaje de error:
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getErrorObject());
        }
    }

    /**
     * Método: POST
     * Url para llegar: /movies
     * Objetivo: insertar la película que se facilita como parámetro.
     *
     * @param movie los datos de la película a insertar
     * @return Si la inserción se ha podido hacer, la nueva película y la url para acceder a ella.
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> create(@Valid @RequestBody Film movie) {
        try {
            //Tratamos de crear la película:
            Optional<Film> inserted = movies.create(movie);
            //Si se crea correctamente, devolvemos la información de la película creada.
            return ResponseEntity.created(URI.create(Constants.URL + "/movies/" + inserted.get().getId()))
                    .body(inserted.get());
        } catch (InvalidDataException e) {
            //Si se captura la excepción de datos inválidos, se devuelve una bad request.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorObject());
        }

    }

    /**
     * Método: PUT
     * Url para llegar: /movies/{id}
     * Objetivo: actualizar la película con el id pasado por url, y con los datos facilitados como parámetro.
     *
     * @param id El id de la película a actualizar
     * @param movie Datos de la película
     * @return Si la actualización se ha podido llevar a cabo, los datos de la película modificados.
     */
    @PutMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> update(@PathVariable("id") String id, @Valid @RequestBody Film movie){
        try {
            Optional<Film> updated = movies.update(id, movie);
            //Si se actualiza la película, se devuelve un estado OK:
            //En el cuerpo del mensaje irán los datos de la película.
            return ResponseEntity.ok(updated.get());
        } catch (InvalidDataException e) {
            //Si se captura una excepción de datos incorrectos, se ofrece con un estado bad request:
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorObject());
        }
    }

    /**
     * Método: DELETE
     * Url para llegar: /movies/{id}
     * Objetivo: borrar la película con el id facilitado vía url.
     *
     * @param id el id de la película a borrar.
     * @return no se devuelve contenido, pero sí un mensaje avisando del borrado correcto o del error, en caso de no
     *      encontrarse la película.
     */
    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> delete(@PathVariable("id") String id){
        try {
            //Se trata de borrar la película con el id especificado:
            movies.delete(id);
            //Se devuelve un estado noContent, dado que no tenemos nada que mostrar:
            return ResponseEntity.noContent().build();
        } catch (InvalidDataException e) {
            //En caso de no poderse ejecutar el borrado (lanzada excepción), se devuelve error:
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getErrorObject());
        }
    }

    /**
     * Método: POST
     * Url para llegar: /movies/{id}/comments
     * Objetivo: añadir un comentario para la película con el id indicado en la URL.
     *
     * @param id el id de la película sobre la cual se va a insertar un comentario.
     * @param assessment los datos del comentario a añadir, incluyendo el usuario que lo hace.
     * @return El comentario introducido en la base de datos.
     */
    @PostMapping(
            path = "{id}/comments",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> addComment(@PathVariable("id") String id,
                                          @Valid @RequestBody Assessment assessment){
        try {
            //Intentamos añadir el comentario:
            Optional<Assessment> comment = assessments.addComment(id, assessment);
            //Devolvemos un estado Created con los datos del comentario añadido:
            return ResponseEntity.created(URI.create(Constants.URL + "/movies/" +
                    assessment.getMovie().getId() + "/comments/" + assessment.getId())).body(comment.get());
        } catch (InvalidDataException e) {
            //Si se captura exepción, se manda un estado BAD REQUEST:
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorObject());
        }
    }

    /**
     * Método: GET
     * Url para llegar: /movies/{id}/comments
     * Objetivo: obtener todos los comentarios asociados a una película.
     *
     * @param page la página a recuperar
     * @param size el tamaño de cada página
     * @param sort criterios de ordenación
     * @param id identificador de la película
     * @return La página pedida de la lista de comentarios de la película, en caso de que la información
     *      facilitada sea correcta. Si no, un estado erróneo.
     */
    @GetMapping(
            path = "{id}/comments",
            produces = MediaType.APPLICATION_JSON_VALUE
    ) ResponseEntity getComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("id") String id
    ) {
        //Transformamos la lista de criterios pasada como argumento para que puedan ser procesados en la consulta:
        List<Sort.Order> criteria = AuxMethods.getSortCriteria(sort);

        try {
            //Se trata de hacer la búsqueda:
            return ResponseEntity.of(assessments.getComments(page, size, Sort.by(criteria), id));
        } catch (NoResultException e) {
            //En caso de tener una excepción asociada a la inexistencia de resultados, se devuelve error:
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getErrorObject());
        }
    }

    /**
     * Método: PUT
     * Url para llegar: /movies/{id}/comments/{commentId}
     * Objetivo: modificar el comentario cuyo id se indica en la URL, de la película cuyo id también
     *      se indica por esa vía.
     *
     * @param movieId El identificador de la película de la que se quiere modificar un comentario.
     * @param commentId El identificador del comentario que se quiere modificar.
     * @param assessment El comentario a modificar.
     * @return El comentario modificado, tal y como ha quedado almacenado.
     */
    @PutMapping(
            path = "{id}/comments/{commentId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> modifyComment(@PathVariable("id") String movieId,
                                             @PathVariable("commentId") String commentId,
                                             @RequestBody Assessment assessment){
        try {
            //Se trata de modificar el comentario:
            Optional<Assessment> result = assessments.modifyComment(movieId,commentId,assessment);
            //En caso de ejecución incorrecta, se da por hecho que la modificación se completó, se devuelve por ello
            //un estado correcto.
            return ResponseEntity.ok(result.get());
        } catch (InvalidDataException e) {
            //Si se captura una excepción asociada a información incorrecta, se manda un bad request.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorObject());
        }
    }

    /**
     * Método: DELETE
     * Url para llegar: /movies/{id}/comments/{commentId}
     * Objetivo: borrar el comentario cuyo id se indica en la URL, de la película cuyo id se indica
     *      por esa misma vía.
     *
     * @param movieId El id de la película de la que se quiere borrar un comentario.
     * @param commentId El comentario a borrar
     * @return Estado correcto si se borra correctamente, estado erróneo en otro caso.
     */
    @DeleteMapping(
            path = "{id}/comments/{commentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> deleteComment(@PathVariable("id") String movieId,
                                 @PathVariable("commentId") String commentId){
        try {
            //Se intenta borrar la película
            assessments.deleteComment(movieId, commentId);
            //Se devuelve una respuesta correcta vacía (si se llega a este punto se pudo ejecutar el borrado):
            return ResponseEntity.noContent().build();
        } catch (InvalidDataException e) {
            //Se devuelve un error en caso de capturar una excepción:
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorObject());
        }
    }
}
