package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movies;

    @Autowired
    public MovieService(MovieRepository movies) {
        this.movies = movies;
    }

    public Optional<Page<Movie>> get(int page, int size, Sort sort, List<String> keywords, List<String> genres) {
        Pageable request = PageRequest.of(page, size, sort);

        Page<Movie> result = movies.findAllByKeywordsAndGenres(keywords, genres, request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Movie> get(String id) {
        return movies.findById(id);
    }

    public Optional<Movie> create(Movie movie){
        return Optional.of(movies.insert(movie));
    }

    public Optional<Movie> update(String id, Movie movie){
        if(movies.existsById(id)){
            movie.setId(id);
            return Optional.of(movies.save(movie));
        } else {
            return Optional.empty();
        }
    }

    public boolean delete(String movieId){
        if(movies.existsById(movieId)){
            movies.deleteById(movieId);
            return true;
        } else {
            return false;
        }
    }
}
