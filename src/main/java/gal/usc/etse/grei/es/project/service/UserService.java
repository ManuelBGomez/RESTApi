package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.AlreadyCreatedException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidDataException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.NoDataException;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Clase UserService: contiene métodos relacionados mayoritariamente con los usuarios y sus datos.
 *
 * @author Manuel Bendaña
 */
@Service
public class UserService {
    //Referencias a las interfaces repository que necesitamos:
    private final UserRepository users;

    /**
     * Constructor de la clase
     *
     * @param users Referencia al repositorio de usuarios.
     */
    @Autowired
    public UserService(UserRepository users){
        this.users = users;
    }

    /**
     * Método que permite recuperar el usuario con el id pasado:
     * @param id El id del usuario por el que filtrar
     * @return Los datos del usuario con el id facilitado
     */
    public Optional<User> get(String id){
        return users.findById(id);
    }

    /**
     * Método que permite recuperar los usuarios que cumplan determinados criterios de búsqueda.
     *
     * @param page La página a recuperar
     * @param size Tamaño de la página
     * @param sort Parámetros de ordenación
     * @param name Nombre por el cual se hace la búsqueda
     * @param email Email por el cual se hace la búsqueda
     * @return Los datos de todos los usuarios que coinciden con los filtros introducidos.
     */
    public Optional<Page<User>> get(int page, int size, Sort sort, String name, String email){
        //Se crea el objeto pageable para bhacer la búsqueda por páginas.
        Pageable request = PageRequest.of(page, size, sort);

        //Se crea el exampleMatcher para preparar la búsqueda:
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        //Establecemos criterios de filtrado:
        Example<User> filter = Example.of(new User().setEmail(email).setName(name), matcher);

        //Se recuperan todos los resultados:
        Page<User> result  = users.findAll(filter, request);

        //Si está vacío, se devuelve un optional vacío:
        if(result.isEmpty())
            return Optional.empty();

        //Si no, eliminamos los campos que no nos interesan:
        result.forEach((it) -> {
            it.setEmail(null);
            it.setFriends(null);
        });

        //Finalmente, se devuelve el optional del resultado:
        return Optional.of(result);
    }

    /**
     * Método que permite crear un usuario.
     *
     * @param user EL usuario que se quiere crear.
     * @return Los datos del usuario, una vez ya insertado.
     * @throws AlreadyCreatedException excepción lanzada en caso de que el usuario estuviese creado.
     */
    public Optional<User> create(User user) throws AlreadyCreatedException {
        //Comprobamos si existe ya un usuario con el email pasado:
        if(users.existsById(user.getEmail())) {
            //Si ya existe, lanzamos una excepción:
            throw new AlreadyCreatedException(ErrorType.EXISTING_DATA,
                    "There is an existing user with the specified email.");
        } else {
            //Si no, insertamos el usuario y devolvemos los datos:
            return Optional.of(users.insert(user));
        }
    }

    /**
     * Método que permite borrar un usuario.
     *
     * @param userMail El email del usuario que se quiere borrar (es su identificador)
     * @throws NoDataException Excepción asociada a la no existencia del usuario que se quiere borrar.
     */
    public void delete(String userMail) throws NoDataException {
        //Comprobamos que el usuario existe:
        if(users.existsById(userMail)){
            //Se borra el usuario en caso de que existiese:
            users.deleteById(userMail);
        } else {
            //Si no existe, se lanza una excepción:
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "The specified user does not exist.");
        }
    }

    /**
     * Método que permite actualizar los datos de un usuario
     *
     * @param id El identificador del usuario a actualizar.
     * @param user Todos los datos del usuario
     * @return Los datos modificados, ya guardados en la base de datos.
     * @throws InvalidDataException Excepción lanzada si hay algo incorrecto.
     * @throws NoDataException Excepción lanzada si no se encuentra al usuario.
     */
    public Optional<User> update(String id, User user) throws InvalidDataException, NoDataException {
        //Empezamos comprobando que los ids coincidan:
        if(!user.getEmail().equals(id)) {
            throw new InvalidDataException(ErrorType.EXISTING_DATA, "The URI user id and the email don't match");
        }
        //Existe - Comprobamos que el campo birthday no se haya cambiado y si el usuario existe:
        User oldUser = users.findById(id).orElseThrow(() ->
                new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with that email."));
        //Comprobamos si las fechas son coincidentes:
        if(!user.getBirthday().equals(oldUser.getBirthday())) {
            throw new InvalidDataException(ErrorType.FORBIDDEN, "You cannot change the birthday info");
        }
        //Con las comprobaciones hechas, se puede actualizar el usuario:
        return Optional.of(users.save(user));
    }

    /**
     * Método que permite añadir un amigo a un usuario.
     *
     * @param userId El id del usuario a añadir.
     * @param newFriend Los datos del nuevo amigo.
     * @return El usuario completo con la lista de amigos añadida.
     * @throws InvalidDataException Excepción lanzada en caso de datos incorrectos (usuarios ya amigos).
     * @throws NoDataException Excepción lanzada en caso de datos desconocidos (usuario inexistente).
     */
    public Optional<User> addFriend(String userId, User newFriend) throws InvalidDataException, NoDataException {
        //Comprobamos que el usuario y el amigo no sean la misma persona:
        if(userId.equals(newFriend.getEmail())) {
            throw new InvalidDataException(ErrorType.INVALID_INFO, "An user cannot be friend of himself");
        }

        //Comprobamos que el usuario y el amigo existan en la db. Usuario:
        User user = users.findById(userId).orElseThrow(()->
                new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with that id"));

        //Comprobación amigo:
        if(!users.existsById(newFriend.getEmail())) {
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "Friend cannot be added: user does not exist");
        }

        //Comprobamos si el array de amigos del usuario está vacío (para crearlo):
        if(user.getFriends() == null){
            user.setFriends(new ArrayList<>());
        }

        //Comprobamos si el usuario y el posible amigo ya lo son:
        if(!user.getFriends().contains(newFriend)){
            //Si no lo son, añadimos nuevo amigo:
            user.getFriends().add(newFriend);
            //Guardamos los cambios y devolvemos el resultado:
            return Optional.of(users.save(user));
        } else {
            //Si ya lo son, se manda una excepción:
            throw new InvalidDataException(ErrorType.INVALID_INFO, "Both users already are friends");
        }
    }

    /**
     * Método que permite eliminar un amigo de un usuario.
     *
     * @param userId El id del usuario.
     * @param friendId El id del amigo a eliminar
     * @return El usuario completo sin el amigo.
     * @throws InvalidDataException Excepción lanzada si hay incoherencias
     * @throws NoDataException Excepción lanzada si algún dato no existe (usuario, amigo).
     */
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

    /**
     * Método que comprueba si un usuario existe en base a su id
     * @param userId El id del posible usuario
     * @return Un booleano que indica si el usuario existe o no.
     */
    public boolean existsById(String userId){
        return users.existsById(userId);
    }

}
