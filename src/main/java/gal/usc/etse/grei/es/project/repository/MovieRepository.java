package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Film;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de películas, usado para el acceso a la información de éstas
 * de la base de datos.
 *
 * @author Manuel Bendaña
 */
public interface MovieRepository extends MongoRepository<Film, String> {}
