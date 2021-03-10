package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Person;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de personas.
 *
 * @author Manuel Bendaña.
 */
public interface PeopleRepository extends MongoRepository<Person, String> {}
