package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidDataException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.NoDataException;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.repository.FriendRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.stereotype.Service;


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

    /**
     * Constructor de la clase
     *
     * @param friends Referencia al friendRepository
     * @param users Referencia al userService
     */
    public FriendService(FriendRepository friends, UserRepository users) {
        this.friends = friends;
        this.users = users;
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

        //Comprobamos si el usuario y el posible amigo ya lo son:
        if(!friends.existsByUserAndFriend(userId, friendship.getFriend())){
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
    /*
    /**
     * Método que permite eliminar un amigo de un usuario.
     *
     * @param userId El id del usuario.
     * @param friendId El id del amigo a eliminar
     * @return El usuario completo sin el amigo.
     * @throws InvalidDataException Excepción lanzada si hay incoherencias
     * @throws NoDataException Excepción lanzada si algún dato no existe (usuario, amigo).
     *//*
    public Optional<User> deleteFriend(String userId, String friendId) throws InvalidDataException, NoDataException {
        //Comprobamos que el id de usuario sea válido:
        User resultUser = users.findById(userId).orElseThrow(()->
                new NoDataException(ErrorType.UNKNOWN_INFO, "User does not exists"));

        //Comprobamos que exista el amigo:
        User friendUser = users.findById(friendId).orElseThrow(()->
                new NoDataException(ErrorType.UNKNOWN_INFO, "Friend does not exists"));

        //Comprobamos con el nombre y el mail si los dos usuarios son amigos:
        //Vamos recorriendo el bucle hasta encontrar una coincidencia de emails entre el de un amigo y el buscado:
        if(resultUser.getFriends() != null) {
            //Sabiendo que no es null, haremos ese recorrido:
            for(User user: resultUser.getFriends()){
                if (user.getEmail().equals(friendUser.getEmail())) {
                    //Si lo son, eliminamos el amigo:
                    resultUser.getFriends().remove(user);
                    //Se devuelve el usuario tras los cambios.
                    return Optional.of(users.save(resultUser));
                }
            }
        }

        //En caso de no cumplirse lo anterior, se lanza una excepción:
        throw new InvalidDataException(ErrorType.INVALID_INFO, "Both users are not friends");
    }
    */
}
