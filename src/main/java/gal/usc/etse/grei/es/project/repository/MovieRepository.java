package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MovieRepository extends MongoRepository<Movie, String> {

    @Query(value = "{\"$and\": [{\"keywords\": {\"$all\": ?0}}, {\"genres\": {\"$all\" : ?1}}]}\n",
            fields="{id : 1, title : 1, overview : 1, genres : 1, releaseDate : 1, resources : 1}")
    Page<Movie> findAllByKeywordsAndGenres(List<String> keywords, List<String> genres, Pageable pageable);
}
