package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Clase MovieController -> Url para llegar: /movies
 * Gestión de peticiones relacionadas con las películas y sus datos.
 */
@RestController
@RequestMapping("movies")
public class MovieController {
    private final MovieService movies;

    /**
     * Constructor de la clase
     * @param movies Instancia de la clase MovieService
     */
    @Autowired
    public MovieController(MovieService movies) {
        this.movies = movies;
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
     * @return Películas obtenidas a raíz de la búsqueda.
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    ) ResponseEntity<Page<Movie>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "keywords", defaultValue = "") List<String> keywords,
            @RequestParam(name = "genres", defaultValue = "") List<String> genres
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if(string.startsWith("+")){
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.of(movies.get(page, size, Sort.by(criteria), keywords, genres));
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
    ResponseEntity<Movie> get(@PathVariable("id") String id) {
        return ResponseEntity.of(movies.get(id));
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
    ResponseEntity<Movie> create(@Valid @RequestBody Movie movie){
        Optional<Movie> inserted = movies.create(movie);

        return ResponseEntity.created(URI.create("http://localhost:8080/movies/" + inserted.get().getId()))
                .body(inserted.get());
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
    ResponseEntity<Movie> update(@PathVariable("id") String id, @Valid @RequestBody Movie movie){
        Optional<Movie> updated = movies.update(id, movie);

        if(!updated.isPresent()){
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(updated.get());
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
    ResponseEntity delete(@PathVariable("id") String id){
        if(movies.delete(id)){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
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
    ResponseEntity<Assessment> addComment(@PathVariable("id") String id,
                                          @RequestBody Assessment assessment){
        Optional<Assessment> comment = movies.addComment(id, assessment);

        return comment.isPresent() ? ResponseEntity.created(URI.create("http://localhost:8080/movies/" +
                assessment.getMovie().getId() + "/comments/" + assessment.getId())).body(comment.get()) : ResponseEntity.notFound().build();
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
    ) ResponseEntity<Page<Assessment>> getComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("id") String id
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if(string.startsWith("+")){
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.of(movies.getComments(page, size, Sort.by(criteria), id));
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
    ResponseEntity<Assessment> modifyComment(@PathVariable("id") String movieId,
                                             @PathVariable("commentId") String commentId,
                                             @RequestBody Assessment assessment){
        Optional<Assessment> result = movies.modifyComment(movieId,commentId,assessment);

        if(result.isPresent()){
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(
            path = "{id}/comments/{commentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity deleteComment(@PathVariable("id") String movieId,
                                 @PathVariable("commentId") String commentId){

        if(movies.deleteComment(movieId, commentId)){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
