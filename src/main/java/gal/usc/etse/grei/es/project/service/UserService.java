package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.AlreadyCreatedException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.InvalidDataException;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.NoDataException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.CommentRepository;
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
    private final CommentRepository comments;

    @Autowired
    public UserService(UserRepository users, CommentRepository comments){
        this.users = users;
        this.comments = comments;
    }

    /**
     * Método que permite recuperar el usuario con el id pasado:
     * @param id
     * @return
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
        if(user.getEmail().equals(id)){
            //Comprobamos que el id del usuario existe en la bd:
            if(users.existsById(id)){
                //Existe - Comprobamos que el campo birthday no se haya cambiado:
                User oldUser = users.findById(id).get();
                //Comprobamos si las fechas son coincidentes:
                if(user.getBirthday().equals(oldUser.getBirthday())){
                    //Con las comprobaciones hechas, se puede actualizar el usuario:
                    return Optional.of(users.save(user));
                    //Si no se cumplen las condiciones, se lanzan excepciones:
                } else {
                    throw new InvalidDataException(ErrorType.FORBIDDEN, "You cannot change the birthday info");
                }
            } else {
                throw new NoDataException(ErrorType.UNKNOWN_INFO, "There is no user with that email.");
            }
        } else {
            throw new InvalidDataException(ErrorType.EXISTING_DATA, "The URI user id and the email don't match");
        }
    }

    public Optional<User> addFriend(String userId, User newFriend){
        //Validamos campos:
        if(newFriend != null && newFriend.getEmail() != null
                && newFriend.getName() != null && !newFriend.getName().isEmpty()){
            //Comprobamos que el usuario y el amigo no sean la misma persona:
            if(!userId.equals(newFriend.getEmail())){
                //Comprobamos que el usuario y el amigo existan en la db:
                Optional<User> optUser = users.findById(userId);
                if(optUser.isPresent()) {
                    if(users.existsById(newFriend.getEmail())){
                        //Recuperamos el usuario:
                        User user = optUser.get();
                        //Comprobamos si el array de amigos está vacío (para crearlo):
                        if(user.getFriends() == null){
                            user.setFriends(new ArrayList<>());
                        }
                        //Comprobamos si el usuario y el posible amigo ya lo son:
                        if(!user.getFriends().contains(newFriend)){
                            user.getFriends().add(newFriend);
                            //Guardamos los cambios y devolvemos el resultado:
                            return Optional.of(users.save(user));
                        } else {
                            System.out.println("Ya son amigos");
                        }
                    } else {
                        System.out.println("No existe posible amigo con ese mail");
                    }
                } else {
                    System.out.println("No existe ningun usuario con ese id");
                }
            } else {
                System.out.println("Misma persona");
            }
        } else {
            System.out.println("Validacion erronea");
        }
        return Optional.empty();
    }

    public Optional<User> deleteFriend(String userId, String friendId) {
        //Comprobamos que el id de usuario sea válido:
        Optional<User> user = users.findById(userId);
        if(user.isPresent()){
            //Comprobamos que exista el amigo:
            Optional<User> friend = users.findById(friendId);
            if(friend.isPresent()){
                //Comprobamos si son amigos:
                User resultUser = user.get();
                User friendUser = friend.get();
                //Comprobamos con el nombre y el mail:
                if(resultUser.getFriends().contains(new User(friendUser.getEmail(), friendUser.getName(),
                        null, null, null, null))) {
                    //Eliminamos el amigo:
                    resultUser.getFriends().removeIf(user1 -> {
                        return user1.getEmail().equals(friendUser.getEmail());
                    });
                    return Optional.of(users.save(resultUser));
                }
            }
        } else {
            System.out.println("Id incorrecto");
        }
        return Optional.empty();
    }

    public Optional<Page<Assessment>> getUserComments(int page, int size, Sort sort, String userId){
        Pageable request = PageRequest.of(page, size, sort);
        //Ejecutamos la búsqueda:
        Page<Assessment> result = comments.findAllByUserEmail(userId, request);
        //Si no hay resultados se devuelve un elemento vacío:
        if(result.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(result);
        }
    }
}
