package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidDataException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.NoDataException;
import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Clase MovieService: métodos relacionados con las películas.
 *
 * @author Manuel Bendaña
 */
@Service
public class MovieService {
    //Referencias a las interfaces repository que necesitamos en esta clase:
    private final MovieRepository movies;
    private final MongoTemplate mongoTemplate;

    /**
     * Constructor de la clase
     * @param movies Referencia al MovieRepository
     * @param mongoTemplate Referencia a MongoTemplate, para la consulta de películas.
     */
    @Autowired
    public MovieService(MovieRepository movies, MongoTemplate mongoTemplate) {
        this.movies = movies;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Método que permite recuperar los datos de todas las películas.
     *
     * @param page Página a recuperar.
     * @param size Tamaño de la página.
     * @param sort Parámetros de ordenación.
     * @param keywords Lista de palabras clave por las cuales se puede realizar la búsqueda.
     * @param genres Lista de géneros por los cuales se puede realizar la búsqueda.
     * @param cast Lista de los nombres de los miembros del cast por los que se puede realizar la búsqueda de películas.
     * @param crew Lista de nombres de los miembros del crew por los que se puede realizar la búsqueda de películas.
     * @param producers Lista de nombres de los productores por los que se puede realizar la búsqueda de películas.
     * @param day Día de cualquier mes por el que se puede realizar la búsqueda de películas.
     * @param month Mes del año por el que se puede realizar la búsqueda.
     * @param year Año por el cual se puede realizar la búsqueda de películas.
     * @return Lista de películas (formato optional) obtenidas por la búsqueda.
     */
    public Optional<Page<Film>> get(int page, int size, Sort sort, List<String> keywords,
                                    List<String> genres, List<String> cast, List<String> crew,
                                    List<String> producers, Integer day, Integer month, Integer year) {
        //Creamos un objeto de Pageable para poder hacer la búsqueda por páginas:
        Pageable request = PageRequest.of(page, size, sort);

        //Establecemos criterios de búsqueda. En primer lugar, existencia de id:
        Criteria criteria = Criteria.where("_id").exists(true);

        //A partir de ahí, vamos añadiendo criterios en función de todos los que se fuesen facilitando:
        if(keywords != null) criteria.and("keywords").all(keywords);
        if(genres != null) criteria.and("genres").all(genres);
        if(cast != null) criteria.and("cast.name").all(cast);
        if(crew != null) criteria.and("crew.name").all(crew);
        if(producers != null) criteria.and("producers.name").all(producers);
        if(day != null) criteria.and("releaseDate.day").is(day);
        if(month != null) criteria.and("releaseDate.month").is(month);
        if(year != null) criteria.and("releaseDate.year").is(year);

        //Se crea un primer objeto query que devuelva únicamente los resultados de la página que corresponda.
        Query query = Query.query(criteria).with(request);
        //Se incluyen solamente los campos pedidos:
        query.fields().include("_id", "title", "overview", "genres", "releaseDate", "resources");

        //Se hace otro objeto query que nos devuelva todos los resultados, sin tener la paginación en cuenta.
        Query countQuery = Query.query(criteria);

        //Se devuelve el resultado (haciendo todas  las querys necesarias con el mongoTemplate
        return Optional.of(PageableExecutionUtils.getPage(mongoTemplate.find(query, Film.class), request,
                ()->mongoTemplate.count(countQuery, Film.class)));
    }


    /**
     * Método que permite recuperar los datos de la película con el id pasado como parámetro.
     *
     * @param id El id de la película a recuperar
     * @return Los datos de la película con el id facilitado (si se encuentra).
     */
    public Optional<Film> get(String id) {
        //Se recupera la película con el id pasado:
        return movies.findById(id);
    }

    /**
     * Método que permite insertar una nueva película en la base de datos.
     * @param movie Los datos de la película a insertar.
     * @return Los datos de la película una vez insertados, incluyendo el id.
     * @throws InvalidDataException Excepción lanzada en caso de que se facilite alguna información incorrecta.
     */
    public Optional<Film> create(Film movie) throws InvalidDataException {
        //Comprobamos que la película haya llegado sin un id:
        if(movie.getId() == null || movie.getId().isEmpty()){
            //Si es así, se devuelve un optional con los datos de la película insertada.
            return Optional.of(movies.insert(movie));
        } else {
            //Si no, se lanza una excepción:
            throw new InvalidDataException(ErrorType.INVALID_INFO, "The id is automatically generated on insert.");
        }
    }

    /**
     * Método que permite actualizar los datos de una película.
     * @param id El identificador de la película en cuestión.
     * @param movie Los datos de la película para actualizar
     * @return La película una vez actualizada en la Base de Datos.
     * @throws InvalidDataException Excepción lanzada en caso de que haya información incorrecta.
     */
    public Optional<Film> update(String id, Film movie) throws InvalidDataException, NoDataException {
        //Comprobamos que el id de la película existe:
        if(movies.existsById(id)){
            //Verificamos que el id de la película pasada coincida con el de la URL:
            if(movie.getId() != null && movie.getId().equals(id)){
                return Optional.of(movies.save(movie));
            } else {
                //Si no coinciden los IDs, se lanza una excepción:
                throw new InvalidDataException(ErrorType.INVALID_INFO, "URI movie ID and provided movie ID don't match");
            }
        } else {
            //Si no hay película con el id pasado, se lanza otra excepción:
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no film with the specified id");
        }
    }

    /**
     * Método que permite borrar la película con el id especificado.
     * @param movieId El identificador de la película a borrar
     * @throws InvalidDataException Excepción lanzada en caso de haber problemas en el borrado.
     */
    public void delete(String movieId) throws NoDataException {
        //Se comprueba si existe la película que se quiere borrar:
        if(movies.existsById(movieId)){
            //Si existe, se borra la película:
            movies.deleteById(movieId);
        } else {
            //Si no, se lanza una excepción indicando que no se ha encontrado película
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "No film found with the specified ID.");
        }
    }

    /**
     * Método que permite comprobar si una película existe en base a su id.
     * @param movieId El id de la película considerada.
     * @return Un booleano que indica si la película existe o no.
     */
    public boolean existsById(String movieId){
        return movies.existsById(movieId);
    }
}
