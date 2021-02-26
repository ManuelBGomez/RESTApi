package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.repository.CommentRepository;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movies;
    private final UserRepository users;
    private final CommentRepository comments;

    @Autowired
    public MovieService(MovieRepository movies, UserRepository users, CommentRepository comments) {
        this.movies = movies;
        this.users = users;
        this.comments = comments;
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

    public Optional<Assessment> addComment(String id, Assessment assessment) {
        //Comprobamos que la película existe:
        Optional<Movie> movie = movies.findById(id);
        if(movie.isPresent()){
            //Comprobamos que hay una valoración:
            if(assessment.getRating() != null){
                //Comprobamos si el usuario existe:
                if(users.existsById(assessment.getUser().getEmail())){
                    //Añadimos el comentario:
                    assessment.setMovie(new Movie().setId(movie.get().getId()).setTitle(movie.get().getTitle()));
                    return Optional.of(comments.insert(assessment));
                } else {
                    System.out.println("Usuario no existe");
                }
            } else {
                System.out.println("Campos sin cubrir");
            }
        } else {
            System.out.println("Película no válida");
        }
        return Optional.empty();
    }

    public Optional<Page<Assessment>> getComments(int page, int size, Sort sort, String id) {
        Pageable request = PageRequest.of(page, size, sort);

        Page<Assessment> result = comments.findAllByMovieId(id, request);

        if(result.isEmpty()){
            return Optional.empty();
        }

        return Optional.of(result);
    }

    public Optional<Assessment> modifyComment(String movieId, String commentId, Assessment assessment){
        //Comprobamos que la película existe:
        if(movies.existsById(movieId)){
            //Comprobamos que el assessment existe:
            Optional<Assessment> originalComment = comments.findById(commentId);
            if(originalComment.isPresent()){
                //Comprobamos que el comentario sea de la película:
                if(originalComment.get().getMovie().getId().equals(movieId)){
                    Assessment newComment = originalComment.get();
                    newComment.setRating(assessment.getRating());
                    newComment.setComment(assessment.getComment());
                    //Añadimos los cambios
                    return Optional.of(comments.save(newComment));
                } else {
                    System.out.println("El comentario no es de la película pasada");
                }
            } else {
                System.out.println("Comentario no existe");
            }
        } else {
            System.out.println("Película no existe");
        }
        return Optional.empty();
    }
}
