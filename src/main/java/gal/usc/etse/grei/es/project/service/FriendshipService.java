package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.AlreadyCreatedException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidDataException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidFormatException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.NoDataException;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.repository.FriendshipRepository;
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
public class FriendshipService {
    //Referencia al repositorio de amigos
    private final FriendshipRepository friends;
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
    public FriendshipService(FriendshipRepository friends, UserRepository users, PatchUtils patchUtils) {
        this.friends = friends;
        this.users = users;
        this.patchUtils = patchUtils;
    }

    /**
     * Método que permite añadir un amigo a un usuario.
     *
     * @param friendship Los datos del nuevo amigo.
     * @return La relación establecida.
     */
    public Friendship addFriend(Friendship friendship) {
        //Comprobamos que el usuario y el amigo no sean la misma persona:
        if(friendship.getUser().equals(friendship.getFriend())) {
            throw new InvalidDataException(ErrorType.INVALID_INFO, "An user cannot be friend of himself");
        }

        //Comprobamos que el usuario y el amigo existan en la db. Usuario:
        if(!users.existsById(friendship.getUser())){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with that id");
        }

        //Comprobación amigo:
        if(!users.existsById(friendship.getFriend())) {
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "Friend cannot be added: user does not exist");
        }

        //Comprobamos si el usuario y el posible amigo ya lo son (comprobamos el recíproco también):
        if(!friends.existsByUserAndFriend(friendship.getUser(), friendship.getFriend()) &&
                !friends.existsByUserAndFriend(friendship.getFriend(), friendship.getUser())) {
            //Si no lo son, añadimos nuevo amigo. Para ello asociamos todos los parámetros:
            //El id y la fecha desde la que son amigos, de momento, se asegura que estén a null
            friendship.setConfirmed(false).setId(null).setSince(null);
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
     * @return El ID de la amistad.
     */
    public Optional<Friendship> getFriendship(String id) {
        //Recuperamos la información de la amistad:
        return friends.findById(id);
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
     * @param id El id de la amistad.
     */
    public void deleteFriend(String id) {
        //Comprobamos que el id de la amistad sea válido:
        if(!friends.existsById(id)){
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with that id");
        } else {
            friends.deleteById(id);
        }
    }

    /**
     * Método que permite actualizar una amistad para confirmarla.
     *
     * @param friendshipId El id de la amistad
     * @param updates Las actualizaciones a realizar
     * @return El usuario actualizado sobre la base de datos.
     */
    public Friendship updateFriendship(String friendshipId, List<Map<String,Object>> updates){
        //Comprobamos que sólo haya una operación y que únicamente afecte al parámetro confirmed:
        if(updates.size() != 1){
            throw new InvalidFormatException(ErrorType.INVALID_INFO, "Only 1 modification for friendship confirmation allowed");
        }

        //Comprobamos el formato:
        if(updates.get(0).get("op") == null || updates.get(0).get("path") == null || updates.get(0).get("value") == null){
            throw new InvalidDataException(ErrorType.INVALID_INFO, "You must specify operation, path and value");
        }

        //Comprobamos que se quiera cambiar el estado a confirmed:
        if(updates.get(0).get("path").equals("/confirmed") && updates.get(0).get("value").equals(true)){
            //Recuperamos la amistad del id y comprobamos que no esté confirmada.
            Friendship friendship = friends.findById(friendshipId)
                    .orElseThrow(() -> new NoDataException(ErrorType.INVALID_INFO, "Friendship not found."));
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
                throw new AlreadyCreatedException(ErrorType.ALREADY_MODIFIED, "Friendship is already confirmed.");
            }
        } else {
            //Si se intenta modificar otro parámetro, se indica:
            throw new InvalidFormatException(ErrorType.FORBIDDEN, "Only 1 modification for friendship confirmation allowed");
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
     * Método que permite comprobar si un usuario es el que debe confirmar una amistad dados los ids correspondientes.
     * @param id Id de la amistad.
     * @param friend Amigo.
     * @return True si son amigos en un sentido, falso en caso contrario.
     */
    public Boolean hasToConfirm(String id, String friend) {
        //Comprobamos si el usuario es amigo en la amistad con el id indicado:
        return friends.existsByFriendAndId(friend, id);
    }

    /**
     * Método que permite borrar todas las amistades del usuario por el identificador de este.
     * @param userMail El email del usuario.
     */
    public void deleteAllByUserOrFriend(String userMail) {
        //Borramos todos los que tengan como usuario o como amigo a este usuario:
        friends.deleteAllByUserOrFriend(userMail, userMail);
    }

    /**
     * Método que comprueba si un usuario pertenece a una relación de amistad.
     * @param userId El identificador del usuario.
     * @param friendshipId El identificador de la amistad.
     * @return Si pertenece el usuario a la amistad.
     */
    public Boolean isInFriendship(String userId, String friendshipId){
        //Comprobamos si el usuario pertenece a la amistad:
        return friends.existsByUserAndId(userId, friendshipId) || friends.existsByFriendAndId(userId, friendshipId);
    }
}
