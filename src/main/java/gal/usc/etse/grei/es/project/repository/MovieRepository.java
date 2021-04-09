package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Film;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de películas, usado para el acceso a la información de éstas
 * de la base de datos.
 *
 * @author Manuel Bendaña
 */
public interface MovieRepository extends MongoRepository<Film, String> {
    /**
     * Método que permite comprobar la existencia de una película por su título e id.
     * @param id El id de la película a comprobar.
     * @param title El título de la película a comprobar.
     * @return Si existe la película o no.
     */
    boolean existsByIdAndTitle(String id, String title);
}
