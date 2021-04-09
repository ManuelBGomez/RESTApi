package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de comentarios, para acceder a la información de los comentarios
 * almacenada en la base de datos.
 *
 * @author Manuel Bendaña
 */
public interface AssessmentRepository extends MongoRepository<Assessment, String> {

    /**
     * Recuperar todas las películas por el id:
     * @param movieId El id de la película.
     * @param pageable Parámetros de paginación.
     * @return La página que corresponda con los comentarios de la película.
     */
    Page<Assessment> findAllByMovieId(String movieId, Pageable pageable);

    /**
     * Recuperar todas las películas por el email del usuario:
     * @param userId El id del usuario (que es lo mismo que el email).
     * @param pageable Parámetros de paginación.
     * @return La página que corresponda con los comentarios del usuario.
     */
    Page<Assessment> findAllByUserEmail(String userId, Pageable pageable);

    /**
     * Método que permite determinar si existe un comentario para una película
     * de un usuario específico.
     * @param movieId El id de la película.
     * @param userId El id del usuario (email).
     * @return True si existe, false en caso contrario.
     */
    boolean existsAssessmentByMovieIdAndUserEmail(String movieId, String userId);

    /**
     * Método que permite borrar todos los comentarios del usuario con el email pasado:
     * @param userMail El email del usuario cuyos comentarios se borrarán
     */
    void deleteAllByUserEmail(String userMail);

    /**
     * Método que permite borrar todos los comentarios del usuario con el id pasado:
     * @param movieId El identificador de la película cuyos comentarios se borrarán
     */
    void deleteAllByMovieId(String movieId);
}
