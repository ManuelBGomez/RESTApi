package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de usuarios, para acceso a la información de los usuarios
 * almacenados en la base de datos.
 *
 * @author Manuel Bendaña
 */
public interface UserRepository extends MongoRepository<User,String> {
    /**
     * Método que permite comprobar si un usuario existe por su nombre y email.
     * @param email El mail del usuario a comprobar.
     * @param name El nombre del usuario a comprobar.
     * @return SI existe o no el usuario.
     */
    boolean existsByEmailAndName(String email, String name);
}
