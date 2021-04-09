package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.ForbiddenActionException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidDataException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.NoDataException;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.repository.FriendRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import gal.usc.etse.grei.es.project.utilities.PatchUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Clase FriendService: contiene métodos relacionados mayoritariamente con la gestión de las amistades.
 *
 * @author Manuel Bendaña
 */
@Service
public class FriendService {
    //Referencia al repositorio de amigos
    private final FriendRepository friends;
    //Referencia a otros servicios que serán utilizados desde éste:
    private final UserRepository users;
    //Referencia a la clase auxiliar PatchUtils:
    private final PatchUtils patchUtils;

    /**
     * Constructor de la clase
     *
     * @param friends Referencia al friendRepository
     * @param users Referencia al userService
     * @param patchUtils Objeto de la clase PatchUtils, para usar en la gestión de peticiones PATCH.
     */
    public FriendService(FriendRepository friends, UserRepository users, PatchUtils patchUtils) {
        this.friends = friends;
        this.users = users;
        this.patchUtils = patchUtils;
    }

    /**
     * Método que permite añadir un amigo a un usuario.
     *
     * @param userId El id del usuario a añadir.
     * @param friendship Los datos del nuevo amigo.
     * @return La relación establecida.
     * @throws InvalidDataException Excepción lanzada en caso de datos incorrectos (usuarios ya amigos).
     * @throws NoDataException Excepción lanzada en caso de datos desconocidos (usuario inexistente).
     */
    public Friendship addFriend(String userId, Friendship friendship) throws InvalidDataException, NoDataException {
        //Comprobamos que el usuario y el amigo no sean la misma persona:
        if(userId.equals(friendship.getFriend())) {
            throw new InvalidDataException(ErrorType.INVALID_INFO, "An user cannot be friend of himself");
        }

        //Comprobamos que el usuario y el amigo existan en la db. Usuario:
        if(!users.existsById(userId)){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with that id");
        }

        //Comprobación amigo:
        if(!users.existsById(friendship.getFriend())) {
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "Friend cannot be added: user does not exist");
        }

        //Comprobamos si el usuario y el posible amigo ya lo son (comprobamos el recíproco también):
        if(!friends.existsByUserAndFriend(userId, friendship.getFriend()) &&
                !friends.existsByUserAndFriend(friendship.getFriend(),userId)) {
            //Si no lo son, añadimos nuevo amigo. Para ello asociamos todos los parámetros:
            //El id y la fecha desde la que son amigos, de momento, se asegura que estén a null
            friendship.setConfirmed(false).setUser(userId).setId(null).setSince(null);
            //Guardamos los cambios y devolvemos el resultado:
            return friends.save(friendship);
        } else {
            //Si ya lo son, se manda una excepción:
            throw new InvalidDataException(ErrorType.INVALID_INFO, "Both users already are friends");
        }
    }

    /**
     * Método que permite recuperar los datos de una amistad.
     *
     * @param userId Id de uno de los miembros de la amistad.
     * @param friendId Id del otro de los miembros de la amistad.
     * @return El ID de la amistad.
     * @throws NoDataException excepción asociada a no encontrar el resultado buscado.
     */
    public Friendship getFriendship(String userId, String friendId) throws NoDataException{
        //Comprobamos que el usuario exista:
        if(!users.existsById(userId)){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with that id");
        }

        //Comprobamos que el amigo exista:
        if(!users.existsById(friendId)) {
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "Unknown friend id");
        }

        //Recuperamos la información de amistad: primero intentamos buscar la amistad en un sentido, luego en el otro y
        //si no es encuentra, se lanza una excepción.
        return friends.getByUserAndFriend(userId, friendId)
                .orElseGet(()->friends.getByUserAndFriend(friendId,userId)
                        .orElseThrow(()->new NoDataException(ErrorType.INVALID_INFO,
                                "Not found friendship between that users")));
    }

    /**
     * Método que permite recuperar las amistades de un usuario determinado.
     * @param page Página a recuperar.
     * @param size Tamaño de la página a recuperar.
     * @param sort Criterios de ordenación.
     * @param userId Identificador del usuario para el cual se quieren recuperar sus amistades.
     * @return Las amistades según los filtros aplicados.
     */
    public Optional<Page<Friendship>> getUserFriendships(int page, int size, Sort sort, String userId){
        //Colocamos los criterios de paginación en un objeto Pageable:
        Pageable request = PageRequest.of(page, size, sort);
        //Devolvemos el resultado de la búsqueda. Buscamos donde el usuario o el amigo tengan el id de este usuario:
        return friends.getAllByUserOrFriend(userId, userId, request);
    }


    /**
     * Método que permite eliminar un amigo de un usuario.
     *
     * @param userId El id del usuario.
     * @param friendId El id del amigo a eliminar
     * @throws InvalidDataException Excepción lanzada si hay incoherencias
     * @throws NoDataException Excepción lanzada si algún dato no existe (usuario, amigo).
     */
    public void deleteFriend(String userId, String friendId) throws InvalidDataException, NoDataException {
        //Comprobamos que el id de usuario sea válido:
        if(!users.existsById(userId)){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with that id");
        }

        //Comprobamos que exista el amigo:
        //Comprobamos que el amigo exista:
        if(!users.existsById(friendId)) {
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "Unknown friend id");
        }

        //Comprobamos con el nombre y el mail si los dos usuarios son amigos:
        if(friends.existsByUserAndFriend(userId, friendId)){
            friends.deleteByUserAndFriend(userId, friendId);
        //Probamos en el sentido contrario:
        } else if (friends.existsByUserAndFriend(friendId, userId)){
            friends.deleteByUserAndFriend(friendId, userId);
        //Si de ninguna manera son amigos, se lanza una excepción:
        } else {
            throw new NoDataException(ErrorType.INVALID_INFO, "No friendship found between two users");
        }
    }

    /**
     * Método que permite actualizar una amistad para confirmarla.
     *
     * @param userId El id del usuario al que alguien ha añadido como amigo.
     * @param friendId El id del amigo que añadió al usuario.
     * @param updates Las actualizaciones a realizar
     * @return El usuario actualizado sobre la base de datos.
     */
    public Friendship updateFriendship(String userId, String friendId, List<Map<String,Object>> updates){
        //Comprobamos que sólo haya una operación y que únicamente afecte al parámetro confirmed:
        if(updates.size() != 1){
            throw new ForbiddenActionException(ErrorType.INVALID_INFO, "Only 1 modification for friendship confirmation allowed");
        }

        //Comprobamos el formato:
        if(updates.get(0).get("op") == null || updates.get(0).get("path") == null || updates.get(0).get("value") == null){
            throw new InvalidDataException(ErrorType.INVALID_INFO, "You must specify operation, path and value");
        }

        //Comprobamos que se quiera cambiar el estado a confirmed:
        if(updates.get(0).get("path").equals("/confirmed") && updates.get(0).get("value").equals(true)){
            //Recuperamos la amistad del id y comprobamos que no esté confirmada.
            //Como esta acción sólo la podrá hacer el amigo, entendemos que quien hizo la solicitud es el usuario
            //que se pasa como friendId:
            Friendship friendship = friends.getByUserAndFriend(friendId, userId)
                    .orElseThrow(() -> new NoDataException(ErrorType.INVALID_INFO,
                            "Not found friendship from " + friendId + " to " + userId + "."));
            //A continuación, se comprueba que la solicitud este sin confirmar:
            if(!friendship.getConfirmed()){
                friendship = patchUtils.patch(friendship, updates);
                //Añadimos la fecha actual como fecha:
                Calendar calendar = Calendar.getInstance();
                //Usamos para ello un objeto calendar:
                calendar.setTime(new java.util.Date(System.currentTimeMillis()));
                //El mes empieza con índice 0:
                friendship.setSince(new Date(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.YEAR)));
                //Guardamos la información en la base de datos:
                return friends.save(friendship);
            } else {
                //Si la amistad ya está confirmada, se indica:
                throw new InvalidDataException(ErrorType.INVALID_INFO, "Friendship is already confirmed.");
            }
        } else {
            //Si se intenta modificar otro parámetro, se indica:
            throw new ForbiddenActionException(ErrorType.FORBIDDEN, "Only 1 modification for friendship confirmation allowed");
        }
    }

    /**
     * Método que permite comprobar si dos usuarios son amigos entre ellos.
     * @param user1 Primer usuario.
     * @param user2 Segundo usuario.
     * @return True si son amigos, falso en caso contrario.
     */
    public Boolean areFriends(String user1, String user2) {
        //Comprobamos si los dos usuarios tienen amistad (recordemos que puede ir en los dos sentidos):
        return friends.existsByUserAndFriend(user1, user2) || friends.existsByUserAndFriend(user2, user1);
    }

    /**
     * Método que permite comprobar si dos usuarios son amigos entre ellos,solamente en un sentido.
     * @param user1 Primer usuario.
     * @param user2 Segundo usuario.
     * @return True si son amigos en un sentido, falso en caso contrario.
     */
    public Boolean hasToConfirm(String user1, String user2) {
        //Comprobamos si los dos usuarios tienen amistad en un sentido:
        return friends.existsByUserAndFriend(user1, user2);
    }

    /**
     * Método que permite borrar todas las amistades del usuario por el identificador de este.
     * @param userMail El email del usuario.
     */
    public void deleteAllByUserOrFriend(String userMail) {
        //Borramos todos los que tengan como usuario o como amigo a este usuario:
        friends.deleteAllByUserOrFriend(userMail, userMail);
    }
}
