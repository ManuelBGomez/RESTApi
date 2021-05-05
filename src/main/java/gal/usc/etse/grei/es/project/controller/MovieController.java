package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.errorManagement.ErrorObject;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.utilities.AuxMethods;
import gal.usc.etse.grei.es.project.utilities.Constants;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.MovieService;
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
@Tag(name = "Movie API", description = "Movie related operations")
@SecurityRequirement(name="JWT")
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
    @Operation(
            operationId = "getAllMovies",
            summary = "Get all movies details",
            description = "Get all the details for all movies,  using diferent filters and pageable. To get " +
                    "them, you must be authenticated."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movies details",
                    headers = {
                            @Header(
                                    name = "Self movie page",
                                    description = "HATEOAS Self Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "First movie page",
                                    description = "HATEOAS First Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Last movie page",
                                    description = "HATEOAS Last Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Next movie page",
                                    description = "HATEOAS Next Link (if necessary)",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Previous movie page",
                                    description = "HATEOAS Previous Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "One movie",
                                    description = "HATEOAS One Link",
                                    schema = @Schema(type = "Link")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No comments found",
                    content = @Content
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
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Page<Film>> get(
            @Parameter(name = "page", description = "Page number to get", example = "1")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(name = "size", description = "Size of the page", example = "15")
            @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(name = "sort", description = "Sort criteria", example = "+releaseDate")
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @Parameter(name = "keywords", description = "Movie keywords to perform search", example = "deathcore")
            @RequestParam(name = "keywords", required = false) List<String> keywords,
            @Parameter(name = "genres", description = "Movie genres to perform search", example = "action")
            @RequestParam(name = "genres", required = false) List<String> genres,
            @Parameter(name = "producers", description = "Movie producer names to perform search", example = "International")
            @RequestParam(name = "producers", required = false) List<String> producers,
            @Parameter(name = "cast", description = "Movie cast member names to perform search", example = "Alan Tang")
            @RequestParam(name = "cast", required = false) List<String> cast,
            @Parameter(name = "crew", description = "Movie crew member names to perform search", example = "Yang Tao")
            @RequestParam(name = "crew", required = false) List<String> crew,
            @Parameter(name = "releaseDate.day", description = "Day of month of the releaseDate to perform search", example = "21")
            @RequestParam(name = "releaseDate.day", required = false) Integer day,
            @Parameter(name = "releaseDate.month", description = "Month of the releaseDate to perform search", example = "1")
            @RequestParam(name = "releaseDate.month", required = false) Integer month,
            @Parameter(name = "releaseDate.year", description = "Year of the releaseDate to perform search", example = "2021")
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

            //Enlace al anterior (si no lo hay, al primer elemento):
            Link previous = linkTo(methodOn(MovieController.class)
                    .get(metadata.previousOrFirst().getPageNumber(), size, sort, keywords, genres, producers,
                            cast, crew, day, month, year)
            ).withRel(IanaLinkRelations.PREVIOUS);

            //Enlace a un recurso:
            Link one = linkTo(methodOn(MovieController.class).get(null))
                    .withRel(relationProvider.getItemResourceRelFor(Film.class));

            //Hacemos el enlace al siguiente (si es necesario):
            if(metadata.next().getPageNumber() < data.getTotalPages()) {
                //Enlace al siguiente
                Link next = linkTo(methodOn(MovieController.class)
                        .get(metadata.next().getPageNumber(), size, sort, keywords, genres, producers,
                                cast, crew, day, month, year)
                ).withRel(IanaLinkRelations.NEXT);
                //La respuesta contendría en ese caso todos los enlaces:
                //Devolvemos la respuesta con todos los enlaces creados:
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, one.toString())
                        .body(data);
            } else {
                //Se devuelve la respuesta sin enlace al siguiente:
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, one.toString())
                        .body(data);
            }
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
    @Operation(
            operationId = "getOneMovie",
            summary = "Get details of one movie",
            description = "Get all the details from one movie given by its id. To get them, " +
                    "you must be authenticated. Results are pageable."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The movie details",
                    headers = {
                            @Header(
                                    name = "Self movie",
                                    description = "HATEOAS Self Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "All movies",
                                    description = "HATEOAS All Link",
                                    schema = @Schema(type = "Link")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
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
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Film> get(
            @Parameter(name="id", description = "The id of the movie to fetch", example="744687")
            @PathVariable("id") String id
    ) {
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
    @Operation(
            operationId = "createMovie",
            summary = "Create new movie",
            description = "Create a new movie introducing his information. To create it, "+
                    "you must have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Movie created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self movie",
                                    description = "HATEOAS Self Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "All movies",
                                    description = "HATEOAS All Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Created movie",
                                    description = "Created movie location",
                                    schema = @Schema(type = "Location")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid format of movie",
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
                    responseCode = "401",
                    description = "Bad token",
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
    ResponseEntity<Film> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Movie data for creation",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"title\":\"Película de ejemplo\"," +
                                            "\"overview\":\"Descripción detallada\"," +
                                            "\"releaseDate\": {\"day\": 21,\"month\": 1,\"year\": 2021}," +
                                            "\"crew\":[{\"name\": \"J. J. Hopkins\",\"birthday\": " +
                                                    "{\"day\": 12,\"month\": 12,\"year\": 1997}}]}"
                            )
                    )
            )
            @Valid @RequestBody Film movie
    ) {
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
            consumes = "application/json-patch+json"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "updateMovie",
            summary = "Update movie information",
            description = "Update information of a movie given by its id. Modifications will be specified en JsonPatch format. " +
                    "To be allowed to do this, "+
                    "you must have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Updated movie correctly",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self movie",
                                    description = "HATEOAS Self Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "All movies",
                                    description = "HATEOAS All Link",
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
                    description = "Movie not found",
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
    ResponseEntity<Film> update(
            @Parameter(name = "id", description = "Movie to update id", example = "744687")
            @PathVariable("id") String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Modifications to movie list",
                    content = @Content(
                            mediaType = "application/json-patch+json",
                            examples = @ExampleObject(
                                    value = "[{\"op\": \"add\", \"path\": \"/releaseDate\", \"value\": {\"day\": 12, \"month\": 12, \"year\": 2021}}]"
                            )
                    )
            )
            @RequestBody List<Map<String, Object>> updates
    ) {
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
    @Operation(
            operationId = "deleteMovie",
            summary = "Delete one movie by id",
            description = "Delete all movie information using its id. To be allowed to do this, "+
                    "you must have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Deleted movie correctly",
                    content = @Content,
                    headers = {
                            @Header(
                                    name = "All movies",
                                    description = "HATEOAS All Link",
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
                    description = "Movie not found",
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
    ResponseEntity<Object> delete(
            @Parameter(name = "id", description = "Movie to delete id", example = "744687")
            @PathVariable("id") String id
    ){
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
    @Operation(
            operationId = "getMovieComments",
            summary = "Get all movie comments",
            description = "Get all comments that different users made of a movie specified by its id. To get this " +
                    "information, you must be authenticated. Results are pageable."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Get comments details",
                    headers = {
                            @Header(
                                    name = "Movie from comment",
                                    description = "HATEOAS Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "First movie comments page",
                                    description = "HATEOAS First Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Last movie comments page",
                                    description = "HATEOAS Last Link",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Next movie comments page",
                                    description = "HATEOAS Next Link (if necessary)",
                                    schema = @Schema(type = "Link")
                            ),
                            @Header(
                                    name = "Previous movie comments page",
                                    description = "HATEOAS Previous Link",
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
                    description = "Movie or its comments not found",
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
    ResponseEntity<Page<Assessment>> getComments(
            @Parameter(name = "page", description = "Page number to get", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(name = "size", description = "Size of the page", example = "10")
            @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(name = "sort", description = "Sort criteria", example = "+comment")
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @Parameter(name="id", description = "Movie id which comments will be retrieved", example="744687")
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

            //Enlace al anterior (si no lo hay, al primer elemento):
            Link previous = linkTo(methodOn(MovieController.class)
                    .getComments(metadata.previousOrFirst().getPageNumber(), size, sort, id)
            ).withRel(IanaLinkRelations.PREVIOUS);

            //Hacemos el enlace al siguiente (si es necesario):
            if(metadata.next().getPageNumber() < data.getTotalPages()) {
                Link next = linkTo(methodOn(MovieController.class)
                        .getComments(metadata.next().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.NEXT);
                //La respuesta contendría en ese caso todos los enlaces:
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, film.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .body(data);
            } else {
                //Se devuelve la respuesta sin enlace al siguiente:
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, film.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .body(data);
            }
        }

        //Si no ha habido resultado, se devuelve un not found:
        return ResponseEntity.notFound().build();
    }
}
