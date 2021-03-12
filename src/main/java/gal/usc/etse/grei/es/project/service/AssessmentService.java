package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.*;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import gal.usc.etse.grei.es.project.utilities.PatchUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    //Referencia a la clase auxiliar PatchUtils:
    private final PatchUtils patchUtils;

    /**
     * Constructor de la clase
     * @param assessments Referencia al repositorio de comentarios
     * @param movies Referencia al servicio de películas
     * @param users Referencia al servicio de usuarios
     * @param patchUtils Objeto de la clase PatchUtils, para usar en la gestión de peticiones PATCH
     */
    public AssessmentService(AssessmentRepository assessments, MovieService movies, UserService users, PatchUtils patchUtils){
        this.assessments = assessments;
        this.movies = movies;
        this.users = users;
        this.patchUtils = patchUtils;
    }

    /**
     * Método que permite añadir un comentario a la película con el id especificado.
     * @param id Identificador de la película.
     * @param assessment Datos del comentario
     * @return La información del comentario ya introducida en la base de datos.
     * @throws InvalidDataException Excepción asociada a la introducción de datos incorrectos. En este caso
     *      a una película incorrecta o un usuario inexistente.
     */
    public Optional<Assessment> addComment(String id, Assessment assessment)
            throws InvalidDataException, AlreadyCreatedException, NoDataException {
        //Comprobamos que la película coincide con la pasada por la uri:
        if(!assessment.getMovie().getId().equals(id)) {
            throw new InvalidDataException(ErrorType.INVALID_INFO, "Movie URI id and assesment movie id don't match");
        }

        //Comprobamos que la película existe:
        if(!movies.existsById(id)){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "The specified film does not exists");
        }

        //Comprobamos si el usuario existe:
        if(!users.existsById(assessment.getUser().getEmail())){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "The specified user does not exists");
        }

        //Comprobamos si este usuario ya hizo un comentario de esa película:
        if(assessments.existsAssessmentByMovieIdAndUserEmail(id, assessment.getUser().getEmail())){
            throw new AlreadyCreatedException(ErrorType.EXISTING_DATA, "The specified user already has a comment in the specified film");
        }

        //Si llegamos a este punto, ejecutamos la inserción:
        return Optional.of(assessments.insert(assessment));
    }

    /**
     * Método que permite recuperar todos los comentarios asociados a una película.
     *
     * @param page página a recuperar.
     * @param size tamaño de la página.
     * @param sort criterios de ordenación.
     * @param id identificador de la película.
     * @return Los comentarios obtenidos para los criterios especificados.
     */
    public Optional<Page<Assessment>> getComments(int page, int size, Sort sort, String id) {
        //Creamos objeto de pageable para la búsqueda por páginas:
        Pageable request = PageRequest.of(page, size, sort);
        //Se intenta hacer la búsqueda:
        Page<Assessment> result = assessments.findAllByMovieId(id, request);
        //Se devuelve el optional del resultado:
        return Optional.of(result);
    }

    /**
     * Método que permite modificar los datos de un comentario de una película.
     *
     * @param movieId Identificador de la película de la que se quiere modificar un comentario.
     * @param commentId Identificador del comentario a modificar.
     * @param updates Datos a cambiar del comentario.
     * @return El comentario modificado.
     * @throws InvalidDataException Excepción lanzada en caso de pasar algún parámetro incorrecto.
     * @throws ForbiddenActionException Excepción lanzada por ejecutar alguna acción no permitida.
     * @throws NoDataException Excepción lanzada por no encontrarse algún dato (en este caso el comentario).
     * @throws InvalidFormatException Excepción lanzada por no pasar los datos a cambiar en el formato adecuado.
     */
    public Optional<Assessment> modifyComment(String movieId, String commentId, List<Map<String, Object>> updates) throws InvalidDataException, ForbiddenActionException, NoDataException, InvalidFormatException {
        //Validamos la petición realizada:
        for (Map<String, Object> update : updates) {
            //Comprobamos que el formato de la petición patch sea correcto:
            if (update.get("op") == null || update.get("path") == null || update.get("value") == null) {
                throw new InvalidDataException(ErrorType.INVALID_INFO, "You must specify operation, path and value in every update.");
            }
            //Comprobamos que no se intente modificar ni el id, ni el usuario ni la película:
            if(update.get("path").equals("/user")){
                throw new ForbiddenActionException(ErrorType.FORBIDDEN, "You cannot change user of the comment");
            }
            if(update.get("path").equals("/movie")){
                throw new ForbiddenActionException(ErrorType.FORBIDDEN, "You cannot change the comment's film");
            }
            if(update.get("path").equals("/id")){
                throw new ForbiddenActionException(ErrorType.FORBIDDEN, "You cannot change the comment's id");
            }
        }

        //Comprobamos que existe el comentario y que esté asociado a la película correcta:
        Assessment assessment = assessments.findById(commentId).orElseThrow(()->new NoDataException(ErrorType.UNKNOWN_INFO,
                "No assessment with the specified id"));

        if(assessment.getMovie()!=null && assessment.getMovie().getId().equals(movieId)){
            //Aplicamos modificaciones y actualizamos:
            return Optional.of(assessments.save(patchUtils.patch(assessment, updates)));
        } else {
            //Si no está asociado el comentario a la película de la URI se avisa de ello con una excepción:
            throw new InvalidDataException(ErrorType.INVALID_INFO, "Movie URI id and assessment movie id don't match");
        }
    }

    /**
     * Método que permite borrar un comentario:
     * @param movieId El id de la película de la que se quiere eliminar un comentario.
     * @param commentId El id del comentario a borrar.
     * @throws InvalidDataException Excepción lanzada en caso de haber datos incorrectos.
     */
    public void deleteComment(String movieId, String commentId) throws InvalidDataException, NoDataException {
        //Comprobamos existencia de la película, del comentario y su relación:
        if(!movies.existsById(movieId)){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "The specified film does not exists");
        }

        //Comprobamos que el comentario existe:
        Assessment assessment = assessments.findById(commentId).orElseThrow(()->
                new NoDataException(ErrorType.UNKNOWN_INFO, "The specified assessment does not exists"));

        //Comprobamos que el comentario está correctamente asociado a la película:
        if(!assessment.getMovie().getId().equals(movieId)){
            throw new InvalidDataException(ErrorType.INVALID_INFO,
                    "The specified comment is not related with the specified film.");
        }

        //Si se llega a este punto, se elimina el comentario:
        assessments.deleteById(commentId);
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
