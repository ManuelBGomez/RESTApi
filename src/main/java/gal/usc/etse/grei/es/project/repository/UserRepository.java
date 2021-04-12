package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de usuarios, para acceso a la información de los usuarios
 * almacenados en la base de datos.
 *
 * @author Manuel Bendaña
 */
public interface UserRepository extends MongoRepository<User,String> {}
