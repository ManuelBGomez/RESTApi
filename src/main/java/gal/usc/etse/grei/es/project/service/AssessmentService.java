package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.*;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.User;
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
    private final FriendService friends;
    //Referencia a la clase auxiliar PatchUtils:
    private final PatchUtils patchUtils;

    /**
     * Constructor de la clase
     * @param assessments Referencia al repositorio de comentarios
     * @param movies Referencia al servicio de películas
     * @param users Referencia al servicio de usuarios
     * @param friends Referencia al servicio de amigos
     * @param patchUtils Objeto de la clase PatchUtils, para usar en la gestión de peticiones PATCH
     */
    public AssessmentService(AssessmentRepository assessments, MovieService movies,
                             UserService users, FriendService friends, PatchUtils patchUtils){
        this.assessments = assessments;
        this.movies = movies;
        this.users = users;
        this.patchUtils = patchUtils;
        this.friends = friends;
    }

    /**
     * Método que permite añadir un comentario a la película con el id especificado.
     * @param assessment Datos del comentario
     * @return La información del comentario ya introducida en la base de datos.
     * @throws InvalidDataException Excepción asociada a la introducción de datos incorrectos. En este caso
     *      a una película incorrecta o un usuario inexistente.
     */
    public Assessment addComment(Assessment assessment)
            throws InvalidDataException, AlreadyCreatedException, NoDataException {

        //Comprobamos que la película existe (ID y titulo):
        if(!movies.existsByIdAndTitle(assessment.getMovie().getId(), assessment.getMovie().getTitle())){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no film with the specified id and title");
        }

        //Comprobamos si el usuario existe:
        if(!users.existsByIdAndName(assessment.getUser().getEmail(), assessment.getUser().getName())){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with the specified email and name.");
        }

        //Comprobamos si este usuario ya hizo un comentario de esa película:
        if(assessments.existsAssessmentByMovieIdAndUserEmail(assessment.getMovie().getId(), assessment.getUser().getEmail())){
            throw new AlreadyCreatedException(ErrorType.EXISTING_DATA, "The specified user already has a comment in the specified film");
        }

        //Si llegamos a este punto, ejecutamos la inserción:
        return assessments.insert(assessment);
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
     * @param commentId Identificador del comentario a modificar.
     * @param updates Datos a cambiar del comentario.
     * @return El comentario modificado.
     * @throws InvalidDataException Excepción lanzada en caso de pasar algún parámetro incorrecto.
     * @throws ForbiddenActionException Excepción lanzada por ejecutar alguna acción no permitida.
     * @throws NoDataException Excepción lanzada por no encontrarse algún dato (en este caso el comentario).
     * @throws InvalidFormatException Excepción lanzada por no pasar los datos a cambiar en el formato adecuado.
     */
    public Assessment modifyComment(String commentId, List<Map<String, Object>> updates)
            throws InvalidDataException, ForbiddenActionException, NoDataException {
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
            //Comprobamos que el rating, si se quiere cambiar, esté entre 1 y 5:
            if(update.get("path").equals("/rating")){
                int val = 0;
                //Intentamos recuperar el valor y forzar cast a entero:
                try {
                    val = (int) update.get("value");
                } catch(ClassCastException ex) {
                    //Si no se puede convertir a entero, se manda una excepción:
                    throw new InvalidDataException(ErrorType.INVALID_INFO, "Rating must be an Integer");
                }
                //Si el rating no está entre 1 y 5, se manda también una excepción:
                if(val < 1 || val > 5) throw new InvalidDataException(ErrorType.INVALID_INFO, "Rating must be between" +
                        " 1 and 5.");
            }
        }

        //Comprobamos que existe el comentario y que esté asociado a la película correcta:
        Assessment assessment = assessments.findById(commentId).orElseThrow(()->new NoDataException(ErrorType.UNKNOWN_INFO,
                "No assessment with the specified id"));

        //Aplicamos modificaciones y actualizamos:
        return assessments.save(patchUtils.patch(assessment, updates));
    }

    /**
     * Método que permite borrar un comentario:
     * @param commentId El id del comentario a borrar.
     * @throws InvalidDataException Excepción lanzada en caso de haber datos incorrectos.
     */
    public void deleteComment(String commentId) throws NoDataException {
        //Comprobamos existencia del comentario:
        if(!assessments.existsById(commentId)) {
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "The specified assessment does not exists");
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


    /**
     * Método que permite determinar si un comentario pertenece a un usuario o a un amigo suyo.
     *
     * @param userId El id del usuario especificado.
     * @param assessmentId El id del comentario.
     * @return True si es así, false si no.
     */
    public boolean isUserOrFriendComment(String userId, String assessmentId){
        //Recuperamos el comentario completo. Si no existiese, se lanza un not found:
        Assessment assessment = assessments.findById(assessmentId)
                .orElseThrow(()->new NoDataException(ErrorType.UNKNOWN_INFO, "No assessment with the specified id"));
        //Recuperado el comentario, comprobamos si el usuario coincide:
        User user = assessment.getUser();
        //Devolvemos si el comentario del usuario o de un amigo del mismo. Comprobamos que el usuario no sea null (no debería):
        return user != null && (userId.equals(assessment.getUser().getEmail()) || friends.areFriends(userId, user.getEmail()));
    }

    /**
     * Método que permite determinar si un comentario pertenece a un usuario.
     *
     * @param userId El id del usuario especificado.
     * @param assessmentId El id del comentario.
     * @return True si es así, false si no.
     */
    public boolean isUserComment(String userId, String assessmentId){
        //Recuperamos el comentario completo. Si no existiese, se lanza un not found:
        Assessment assessment = assessments.findById(assessmentId)
                .orElseThrow(()->new NoDataException(ErrorType.UNKNOWN_INFO, "No assessment with the specified id"));
        //Recuperado el comentario, comprobamos si el usuario coincide:
        User user = assessment.getUser();
        //Devolvemos si el comentario del usuario. Comprobamos que el usuario no sea null (no debería):
        return user != null && userId.equals(assessment.getUser().getEmail());
    }

    /**
     * Método que recupera el usuario asociado a un comentario
     * @param commentId El id del comentario
     * @return El id del usuario autor del comentario
     */
    public String getUserId(String commentId) {
        //Recuperamos el comentario y devolvemos el usuario:
        Optional<Assessment> assessment = assessments.findById(commentId);
        return assessment.isPresent() ? assessment.get().getUser().getEmail() : "";
    }


    /**
     * Método que recupera el id de la película asociada a un comentario
     * @param commentId El id del comentario
     * @return El id de la película asociada al comentario
     */
    public String getMovieId(String commentId) {
        //Recuperamos el comentario y devolvemos el usuario:
        Optional<Assessment> assessment = assessments.findById(commentId);
        return assessment.isPresent() ? assessment.get().getMovie().getId() : "";
    }
}
