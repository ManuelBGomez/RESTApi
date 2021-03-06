package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AssessmentRepository extends MongoRepository<Assessment, String> {

    Page<Assessment> findAllByMovieId(String movieId, Pageable pageable);

    Page<Assessment> findAllByUserEmail(String userId, Pageable pageable);
}