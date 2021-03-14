package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

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

    /**
     * Método que permite recuperar una amistad por los usuarios que la componen.
     * @param user El usuario que estableció la amistad.
     * @param friend El amigo con el que se estableció.
     * @return Los datos de la amistad.
     */
    Optional<Friendship> getByUserAndFriend(String user, String friend);

    /**
     * Método que permite hacer la búsqueda de una amistad por usuario o amigo
     * @param user El id del usuario.
     * @param friend El id del amigo.
     * @return Página con las amistades correspondientes.
     */
    Optional<Page<Friendship>> getAllByUserOrFriend(String user, String friend, Pageable request);

    /**
     * Método que permite el borrado de una amistad por los ids de los usuarios:
     * @param user El id de uno de los usuarios.
     * @param friend El id del amigo.
     */
    void deleteByUserAndFriend(String user, String friend);
}
