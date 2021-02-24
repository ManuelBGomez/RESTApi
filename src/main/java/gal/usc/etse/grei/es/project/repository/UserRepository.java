package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User,String> {

    @Query(value = "{$and :["
            + "?#{ [0] == null ? { $where : 'true'} : { 'name' : [0] } },"
            + "?#{ [1] == null ? { $where : 'true'} : { '_id' : [1] } },"
            + "]}",
            fields="{name : 1, country : 1, birthday : 1, picture : 1}")
    Page<User> findAllByNameAndId(String name, String id, Pageable pageable);
}
