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

import javax.print.attribute.standard.Media;
import javax.validation.Valid;
import javax.xml.stream.events.Comment;
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

    @PostMapping(
            path = "{id}/comments",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Assessment> addComment(@PathVariable("id") String id,
                                          @RequestBody Assessment assessment){
        Optional<Assessment> comment = movies.addComment(id, assessment);

        return comment.isPresent() ? ResponseEntity.created(URI.create("http://localhost:8080/movies/" +
                assessment.getMovie().getId() + "/comments")).body(comment.get()) : ResponseEntity.notFound().build();
    }

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
}
