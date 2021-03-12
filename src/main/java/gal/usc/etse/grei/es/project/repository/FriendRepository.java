package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Friendship;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de amistades: permite el acceso a la información de las amistades en la base
 * de datos.
 *
 * @author Manuel Bendaña
 */
public interface FriendRepository extends MongoRepository<Friendship, String> {
    /**
     * Método que permite comprobar si existe una amistad por los ids del usuario.
     * @param user El id del usuario
     * @param friend El id de la amistad
     * @return Booleano que determina si existe la amistad.
     */
    boolean existsByUserAndFriend(String user, String friend);
}
