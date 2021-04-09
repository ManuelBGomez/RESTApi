package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.validation.createValidation;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.utilities.AuxMethods;
import gal.usc.etse.grei.es.project.utilities.Constants;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
    //Referencia a un linkrelationprovider para los hateoas
    private final LinkRelationProvider relationProvider;

    /**
     * Constructor de la clase
     * @param movies Instancia de la clase MovieService
     * @param assessments Instancia de la clase AssessmentService
     * @param relationProvider Instancia de la clase LinkRelationProvider
     */
    @Autowired
    public MovieController(MovieService movies, AssessmentService assessments, LinkRelationProvider relationProvider) {
        this.movies = movies;
        this.assessments = assessments;
        this.relationProvider = relationProvider;

    }

    /**
     * Método: GET
     * Url para llegar: /movies
     * Objetivo: recuperar todas las películas en base a diferentes filtros.
     * Permisos: todos los usuarios logueados.
     * Enlaces devueltos: a sí mismo, a la primera página, a la siguiente, a la anterior y última, y a un
     *      recurso concreto.
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
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<Film>> get(
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

        //Recuperamos el listado de películas:
        Optional<Page<Film>> result = movies.get(page, size, Sort.by(criteria), keywords, genres,
                cast, crew, producers, day, month, year);

        //Si hay resultado se preparan los links y se devuelven:
        if(result.isPresent()){
            //Recuperamos el resultado y los datos de pageable:
            Page<Film> data = result.get();
            Pageable metadata = data.getPageable();

            //Enlace a si mismo:
            Link self = linkTo(methodOn(MovieController.class)
                    .get(page, size, sort, keywords, genres, producers, cast, crew, day, month, year)
            ).withSelfRel();

            //Enlace al primero:
            Link first = linkTo(methodOn(MovieController.class)
                    .get(metadata.first().getPageNumber(), size, sort, keywords, genres, producers,
                            cast, crew, day, month, year)
            ).withRel(IanaLinkRelations.FIRST);

            //Enlace al último (recuperamos el total de páginas y restamos 1):
            Link last = linkTo(methodOn(MovieController.class)
                    .get(data.getTotalPages() - 1, size, sort, keywords, genres, producers,
                            cast, crew, day, month, year)
            ).withRel(IanaLinkRelations.LAST);

            //Enlace al siguiente
            Link next = linkTo(methodOn(MovieController.class)
                    .get(metadata.next().getPageNumber(), size, sort, keywords, genres, producers,
                            cast, crew, day, month, year)
            ).withRel(IanaLinkRelations.NEXT);

            //Enlace al anterior (si no lo hay, al primer elemento):
            Link previous = linkTo(methodOn(MovieController.class)
                    .get(metadata.previousOrFirst().getPageNumber(), size, sort, keywords, genres, producers,
                            cast, crew, day, month, year)
            ).withRel(IanaLinkRelations.PREVIOUS);

            //Enlace a un recurso:
            Link one = linkTo(methodOn(MovieController.class).get(null))
                    .withRel(relationProvider.getItemResourceRelFor(Film.class));

            //Devolvemos la respuesta con todos los enlaces creados:
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .header(HttpHeaders.LINK, one.toString())
                    .body(data);
        }

        //Si no, se devolverá un not found:
        return ResponseEntity.notFound().build();
    }

    /**
     * Método: GET
     * Url para llegar: /movies/{id}
     * Objetivo: recuperar los datos de la película con el id facilitado.
     * Permisos: todos los usuarios logueados.
     * Enlaces devueltos: a sí mismo y a la lista de todas las películas.
     *
     * @param id El id de la película cuyos datos se quieren recuperar.
     * @return Si el Id es válido, los datos de la película.
     */
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Film> get(@PathVariable("id") String id) {
        //Tratamos de recuperar la película:
        Optional<Film> result = movies.get(id);
        if(result.isPresent()){
            //Si hay resultado se crean los links y se devuelven:
            //A sí mismo:
            Link self = linkTo(methodOn(MovieController.class).get(id)).withSelfRel();
            //A todas:
            Link all = linkTo(methodOn(MovieController.class).get(0, 20, null, null, null,
                    null, null, null, null, null, null))
                    .withRel(relationProvider.getCollectionResourceRelFor(Film.class));
            //Las devolvemos:
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(result.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Método: POST
     * Url para llegar: /movies
     * Objetivo: insertar la película que se facilita como parámetro.
     * Permisos: sólo los administradores.
     * Enlaces devueltos: a la película creada y a la lista de todas las películas.
     *
     * @param movie los datos de la película a insertar
     * @return Si la inserción se ha podido hacer, la nueva película y la url para acceder a ella.
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Film> create(@Valid @RequestBody Film movie) {
        //Tratamos de crear la película:
        Optional<Film> inserted = movies.create(movie);
        //Si se crea correctamente, devolvemos la información de la película creada.
        //Preparamos los enlaces a devolver:
        //A sí mismo:
        Link self = linkTo(methodOn(MovieController.class).get(inserted.get().getId())).withSelfRel();
        //A todas:
        Link all = linkTo(methodOn(MovieController.class).get(0, 20, null, null, null,
                null, null, null, null, null, null))
                .withRel(relationProvider.getCollectionResourceRelFor(Film.class));
        //Devolvemos también los enlaces creados:
        return ResponseEntity.created(URI.create(Constants.URL + "/movies/" + inserted.get().getId()))
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(inserted.get());
    }


    /**
     * Método: PATCH
     * Url para llegar: /movies/{id}
     * Objetivo: actualizar la película con el id pasado por url, y con los datos facilitados como parámetro.
     * Permisos: sólo los administradores.
     * Enlaces devueltos: a sí mismo y a la lista de todas las películas.
     *
     * @param id El id de la película a actualizar
     * @param updates Datos a actualizar
     * @return Si la actualización se ha podido llevar a cabo, los datos de la película modificados.
     */
    @PatchMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Film> update(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> updates) {
        //Se intenta hacer la actualización y se devuelve el resultado:
        Optional<Film> result = movies.update(id, updates);
        //A sí mismo:
        Link self = linkTo(methodOn(MovieController.class).get(result.get().getId())).withSelfRel();
        //A todas:
        Link all = linkTo(methodOn(MovieController.class).get(0, 20, null, null, null,
                null, null, null, null, null, null))
                .withRel(relationProvider.getCollectionResourceRelFor(Film.class));
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(result.get());
    }

    /**
     * Método: DELETE
     * Url para llegar: /movies/{id}
     * Objetivo: borrar la película con el id facilitado vía url.
     * Permisos: sólo los administradores.
     * Enlaces devueltos: solo a la lista de todas las películas.
     *
     * @param id el id de la película a borrar.
     * @return no se devuelve contenido, pero sí un mensaje avisando del borrado correcto o del error, en caso de no
     *      encontrarse la película.
     */
    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Object> delete(@PathVariable("id") String id){
        //Se trata de borrar la película con el id especificado:
        movies.delete(id);
        //Se prepara el único enlace a devolver, el de todas las películas:
        Link all = linkTo(methodOn(MovieController.class).get(0, 20, null, null, null,
                null, null, null, null, null, null))
                .withRel(relationProvider.getCollectionResourceRelFor(Film.class));
        //Se devuelve un estado noContent, dado que no tenemos nada que mostrar:
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }

    /**
     * Método: GET
     * Url para llegar: /movies/{id}/comments
     * Objetivo: obtener todos los comentarios asociados a una película.
     * Permisos: todos los usuarios logueados
     * Enlaces devueltos: a la película, a la primera página, a la siguiente, a la anterior y a la última.
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
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<Assessment>> getComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("id") String id
    ) {
        //Transformamos la lista de criterios pasada como argumento para que puedan ser procesados en la consulta:
        List<Sort.Order> criteria = AuxMethods.getSortCriteria(sort);

        //Buscamos:
        Optional<Page<Assessment>> result = assessments.getComments(page, size, Sort.by(criteria), id);

        //Si hay resultado, preparamos enlaces para devolver y devolvemos ok:
        if(result.isPresent()){
            Page<Assessment> data = result.get();
            Pageable metadata = data.getPageable();
            //Preparamos enlaces para devolver
            //A la pelicula:
            Link film = linkTo(methodOn(MovieController.class).get(id))
                    .withRel(relationProvider.getItemResourceRelFor(Film.class));
            //Enlace al primero:
            Link first = linkTo(methodOn(MovieController.class)
                    .getComments(metadata.first().getPageNumber(), size, sort, id)
            ).withRel(IanaLinkRelations.FIRST);

            //Enlace al último (recuperamos el total de páginas y restamos 1):
            Link last = linkTo(methodOn(MovieController.class)
                    .getComments(data.getTotalPages() - 1, size, sort, id)
            ).withRel(IanaLinkRelations.LAST);

            //Enlace al siguiente
            Link next = linkTo(methodOn(MovieController.class)
                    .getComments(metadata.next().getPageNumber(), size, sort, id)
            ).withRel(IanaLinkRelations.NEXT);

            //Enlace al anterior (si no lo hay, al primer elemento):
            Link previous = linkTo(methodOn(MovieController.class)
                    .getComments(metadata.previousOrFirst().getPageNumber(), size, sort, id)
            ).withRel(IanaLinkRelations.PREVIOUS);

            //Se devuelve la respuesta:
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, film.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .body(data);
        }

        //Si no ha habido resultado, se devuelve un not found:
        return ResponseEntity.notFound().build();
    }


}
