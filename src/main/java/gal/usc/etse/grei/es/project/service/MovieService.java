package gal.usc.etse.grei.es.project.service;

import com.mongodb.BasicDBList;
import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.repository.CommentRepository;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public Optional<Page<Film>> get(int page, int size, Sort sort, List<String> keywords,
                                    List<String> genres, List<String> cast, List<String> crew,
                                    List<String> producers, Integer day, Integer month, Integer year) {
        Pageable request = PageRequest.of(page, size, sort);

        List<Producer> possibleProducers = producers != null ? new ArrayList<>() : null;
        List<Cast> possibleCast = cast != null ? new ArrayList<>() : null;
        List<Crew> possibleCrew = crew != null ? new ArrayList<>() : null;

        if(producers != null) producers.forEach((member) -> possibleProducers.add(new Producer().setName(member)));
        if(cast != null) cast.forEach((member) -> possibleCast.add((Cast) new Cast().setName(member)));
        if(crew != null) crew.forEach((member) -> possibleCrew.add((Crew) new Crew().setName(member)));

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withMatcher("keywords",
                        matcher1 -> matcher1.transform(source ->
                                Optional.of(((BasicDBList) source.get()).iterator().next())).exact().ignoreCase())
                .withMatcher("genres",
                        matcher1 -> matcher1.transform(source ->
                                Optional.of(((BasicDBList) source.get()).iterator().next())).exact().ignoreCase())
                .withMatcher("crew",
                        matcher1 -> matcher1.transform(source ->
                                Optional.of(((BasicDBList) source.get()).iterator().next())).exact().ignoreCase())
                .withMatcher("cast",
                        matcher1 -> matcher1.transform(source ->
                                Optional.of(((BasicDBList) source.get()).iterator().next())).exact().ignoreCase())
                .withMatcher("producers",
                        matcher1 -> matcher1.transform(source ->
                                Optional.of(((BasicDBList) source.get()).iterator().next())).exact().ignoreCase());

        Example<Film> filter = Example.of(new Film().setGenres(genres).setKeywords(keywords)
                .setCast(possibleCast).setCrew(possibleCrew).setProducers(possibleProducers)
                .setReleaseDate(new Date().setDay(day).setMonth(month).setYear(year)), matcher);

        Page<Film> result = movies.findAll(filter, request);

        if(result.isEmpty())
            return Optional.empty();

        result.forEach((it) -> {
            it.setTagline(null).setCollection(null).setKeywords(null).setProducers(null).setCrew(null)
                    .setCast(null).setBudget(null).setStatus(null).setRuntime(null).setRevenue(null);
        });

        return Optional.of(result);
    }

    public Optional<Film> get(String id) {
        return movies.findById(id);
    }

    public Optional<Film> create(Film movie){
        return Optional.of(movies.insert(movie));
    }

    public Optional<Film> update(String id, Film movie){
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
        Optional<Film> movie = movies.findById(id);
        if(movie.isPresent()){
            //Comprobamos que hay una valoración:
            if(assessment.getRating() != null){
                //Comprobamos si el usuario existe:
                if(users.existsById(assessment.getUser().getEmail())){
                    //Añadimos el comentario:
                    assessment.setMovie(new Film().setId(movie.get().getId()).setTitle(movie.get().getTitle()));
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

    public boolean deleteComment(String movieId, String commentId){
        //Comprobamos existencia de la película, del comentario y su relación:
        if(movies.existsById(movieId)){
            Optional<Assessment> commentInfo = comments.findById(commentId);
            if(commentInfo.isPresent()){
                if(commentInfo.get().getMovie().getId().equals(movieId)){
                    //Eliminamos el comentario:
                    comments.deleteById(commentId);
                    //Devolvemos un valor true para confirmar borrado:
                    return true;
                } else {
                    System.out.println("Pelicula y comentario no relacionados");
                }
            } else {
                System.out.println("Comentario no existe");
            }
        } else {
            System.out.println("Película no existe");
        }
        return false;
    }
}
