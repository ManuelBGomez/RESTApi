package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidDataException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.NoResultException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Clase AssessmentService: métodos relacionados con los comentarios de las películas.
 *
 * @author Manuel Bendaña
 */
@Service
public class AssessmentService {
    //Referencias a las interfaces repository que necesitamos:
    private final AssessmentRepository assessments;
    //Referencias a clases service auxiliares:
    private final MovieService movies;
    private final UserService users;

    /**
     * Constructor de la clase
     * @param assessments Referencia al repositorio de comentarios
     */
    public AssessmentService(AssessmentRepository assessments, MovieService movies, UserService users){
        this.assessments = assessments;
        this.movies = movies;
        this.users = users;
    }


    /**
     * Método que permite añadir un comentario a la película con el id especificado.
     * @param id Identificador de la película.
     * @param assessment Datos del comentario
     * @return La información del comentario ya introducida en la base de datos.
     * @throws InvalidDataException Excepción asociada a la introducción de datos incorrectos. En este caso
     *      a una película incorrecta o un usuario inexistente.
     */
    public Optional<Assessment> addComment(String id, Assessment assessment) throws InvalidDataException {
        //Comprobamos que la película existe:
        Optional<Film> movie = movies.getById(id);
        if(movie.isPresent()){
            //Comprobamos si el usuario existe:
            if(users.existsById(assessment.getUser().getEmail())){
                //Añadimos el comentario, añadiendo antes los datos de la película:
                assessment.setMovie(new Film().setId(movie.get().getId()).setTitle(movie.get().getTitle()));
                return Optional.of(assessments.insert(assessment));
                //En caso de errores, lanzamos sendas excepciones:
            } else {
                throw new InvalidDataException(ErrorType.UNKNOWN_INFO, "The specified user does not exists");
            }
        } else {
            throw new InvalidDataException(ErrorType.UNKNOWN_INFO, "The specified film does not exists");
        }
    }

    /**
     * Método que permite recuperar todos los comentarios asociados a una película.
     *
     * @param page página a recuperar.
     * @param size tamaño de la página.
     * @param sort criterios de ordenación.
     * @param id identificador de la película.
     * @return Los comentarios obtenidos para los criterios especificados.
     * @throws NoResultException excepción asociada a la inexistencia de resultados.
     */
    public Optional<Page<Assessment>> getComments(int page, int size, Sort sort, String id) throws NoResultException {
        //Creamos objeto de pageable para la búsqueda por páginas:
        Pageable request = PageRequest.of(page, size, sort);
        //Se intenta hacer la búsqueda:
        Page<Assessment> result = assessments.findAllByMovieId(id, request);
        //Lanzamos excepción asociada a la inexistencia de resultados en caso de resultado vacío:
        if(result.isEmpty()){
            throw new NoResultException(ErrorType.NO_RESULT,
                    "No comments found for the specified film with the specified criteria.");
        }
        //Si no, se devuelve el optional del resultado:
        return Optional.of(result);
    }

    /**
     * Método que permite modificar los datos de un comentario de una película.
     *
     * @param movieId Identificador de la película de la que se quiere modificar un comentario.
     * @param commentId Identificador del comentario a modificar.
     * @param assessment Datos del comentario.
     * @return El comentario modificado.
     * @throws InvalidDataException Excepción lanzada en caso de pasar algún parámetro incorrecto.
     */
    public Optional<Assessment> modifyComment(String movieId, String commentId, Assessment assessment) throws InvalidDataException {
        //Comprobamos que la película existe:
        if(movies.existsById(movieId)){
            //Comprobamos que el assessment existe:
            Optional<Assessment> originalComment = assessments.findById(commentId);
            if(originalComment.isPresent()){
                //Comprobamos que el comentario sea de la película:
                if(originalComment.get().getMovie().getId().equals(movieId)){
                    //Comprobamos también que el comentario coincida con el del id:
                    if(assessment.getId() != null && assessment.getId().equals(commentId)){
                        //Añadimos los cambios si se cumplieron todos los criterios:
                        return Optional.of(assessments.save(assessment));
                    } else {
                        throw new InvalidDataException(ErrorType.INVALID_INFO,
                                "URL comment id and consumed id don't match");
                    }
                } else {
                    throw new InvalidDataException(ErrorType.INVALID_INFO,
                            "The comment id does not correspond with the film id");
                }
            } else {
                throw new InvalidDataException(ErrorType.UNKNOWN_INFO, "There is no comment with the specified ID");
            }
        } else {
            throw new InvalidDataException(ErrorType.UNKNOWN_INFO, "There is no film with the specified ID");
        }
    }

    /**
     * Método que permite borrar un comentario:
     * @param movieId El id de la película de la que se quiere eliminar un comentario.
     * @param commentId El id del comentario a borrar.
     * @throws InvalidDataException Excepción lanzada en caso de haber datos incorrectos.
     */
    public void deleteComment(String movieId, String commentId) throws InvalidDataException {
        //Comprobamos existencia de la película, del comentario y su relación:
        if(movies.existsById(movieId)){
            Optional<Assessment> commentInfo = assessments.findById(commentId);
            if(commentInfo.isPresent()){
                if(commentInfo.get().getMovie().getId().equals(movieId)){
                    //Si se llega a este punto, se elimina el comentario:
                    assessments.deleteById(commentId);
                    //Si hay errores en las comprobaciones anteriores, se lanzan sendas excepciones.
                } else {
                    throw new InvalidDataException(ErrorType.INVALID_INFO,
                            "The specified comment is not related with the specified film.");
                }
            } else {
                throw new InvalidDataException(ErrorType.UNKNOWN_INFO,
                        "The specified comment does not exists");
            }
        } else {
            throw new InvalidDataException(ErrorType.UNKNOWN_INFO,
                    "The specified film does not exists");
        }
    }

    /**
     * Método que permite recuperar los comentarios de un usuario.
     *
     * @param page La página a recuperar.
     * @param size Tamaño de la página.
     * @param sort Criterios de ordenación.
     * @param userId Identificador del usuario.
     * @return Los comentarios asociados al usuario correspondiente.
     */
    public Optional<Page<Assessment>> getUserComments(int page, int size, Sort sort, String userId){
        Pageable request = PageRequest.of(page, size, sort);
        //Ejecutamos la búsqueda:
        Page<Assessment> result = assessments.findAllByUserEmail(userId, request);
        //Devolvemos el optional.
        return Optional.of(result);
    }
}
