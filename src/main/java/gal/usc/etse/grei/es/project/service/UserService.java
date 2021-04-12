package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.errorManagement.ErrorType;
import gal.usc.etse.grei.es.project.errorManagement.exceptions.*;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import gal.usc.etse.grei.es.project.utilities.PatchUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final AssessmentRepository assessments; //Usamos esto para evitar una dependencia circular.
    //Referencia a las clases servicio auxiliares:
    private final FriendshipService friends;
    //Referencia a la clase auxiliar PatchUtils:
    private final PatchUtils patchUtils;
    //Referencia al PasswordEncoder:
    private final PasswordEncoder encoder;

    /**
     * Constructor de la clase
     *
     * @param users Referencia al repositorio de usuarios.
     * @param patchUtils Objeto de la clase PatchUtils, para usar en la gestión de peticiones PATCH.
     * @param encoder Referencia al objeto de la clase PasswordEncoder, para poder codificar la contraseña.
     * @param friends Referencia al servicio de amigos.
     * @param assessments Referencia al servicio de comentarios.
     */
    @Autowired
    public UserService(UserRepository users, PatchUtils patchUtils, PasswordEncoder encoder,
                       FriendshipService friends, AssessmentRepository assessments){
        this.users = users;
        this.patchUtils = patchUtils;
        this.encoder = encoder;
        this.friends = friends;
        this.assessments = assessments;
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
            it.setRoles(null);
            it.setPassword(null);
        });

        //Finalmente, se devuelve el optional del resultado:
        return Optional.of(result);
    }

    /**
     * Método que permite crear un usuario.
     *
     * @param user EL usuario que se quiere crear.
     * @return Los datos del usuario, una vez ya insertado.
     */
    public Optional<User> create(User user) {
        //Comprobamos si existe ya un usuario con el email pasado:
        if(users.existsById(user.getEmail())) {
            //Si ya existe, lanzamos una excepción:
            throw new AlreadyCreatedException(ErrorType.EXISTING_DATA,
                    "There is an existing user with the specified email.");
        } else {
            //Si no, insertamos el usuario y devolvemos los datos:
            //Antes de nada se asigna el rol de usuario (independientemente de lo que llegue por la petición):
            ArrayList<String> initialRoles = new ArrayList<>();
            initialRoles.add("ROLE_USER");
            user.setRoles(initialRoles);
            //Modificamos la contraseña para guardarla codificada en la base de datos
            user.setPassword(encoder.encode(user.getPassword()));
            //Devolvemos sin indicar ni contraseña ni roles (aunque la contraseña vaya encriptada):
            return Optional.of(users.insert(user).setRoles(null).setPassword(null));
        }
    }

    /**
     * Método que permite borrar un usuario.
     *
     * @param userMail El email del usuario que se quiere borrar (es su identificador)
     */
    public void delete(String userMail){
        //Comprobamos que el usuario existe:
        if(users.existsById(userMail)){
            //Se borra el usuario en caso de que existiese:
            users.deleteById(userMail);
            //Vamos a borrar también las amistades de ese usuario:
            friends.deleteAllByUserOrFriend(userMail);
            //Finalmente, borraremos los comentarios realizados por ese usuario:
            assessments.deleteAllByUserEmail(userMail);
        } else {
            //Si no existe, se lanza una excepción:
            throw new NoDataException(ErrorType.UNKNOWN_INFO, "The specified user does not exist.");
        }
    }

    /**
     * Método que permite actualizar los datos de un usuario
     *
     * @param id El identificador del usuario a actualizar.
     * @param updates Actualizaciones a realizar.
     * @return Los datos modificados, ya guardados en la base de datos.
     */
    public Optional<User> update(String id, List<Map<String, Object>> updates) {
        //Comprobamos que ninguna operación afecte al parámetro email o birthday:
        for (Map<String, Object> update : updates) {
            //Comprobamos también que el formato sea correcto:
            if(update.get("op") == null || update.get("path") == null || update.get("value") == null){
                throw new InvalidDataException(ErrorType.INVALID_INFO, "You must specify operation, path and value");
            }
            if(update.get("path").equals("/email")){
                throw new InvalidFormatException(ErrorType.FORBIDDEN, "You cannot change the email of the user");
            }
            if(update.get("path").equals("/birthday")) {
                throw new InvalidFormatException(ErrorType.FORBIDDEN, "You cannot change the user's birthday");
            }
            if(update.get("path").equals("/roles")) {
                throw new InvalidFormatException(ErrorType.FORBIDDEN, "You cannot change the user's roles");
            }
        }

        //Hecho esto, recuperamos el usuario con el id pasado (si existe):
        User user = users.findById(id).orElseThrow(()->new NoDataException(ErrorType.UNKNOWN_INFO,
                "No user with the specified email"));

        //Aplicamos patch y guardamos el resultado:
        //EL resultado devuelto oculta roles y contraseña:
        return Optional.of(users.save(patchUtils.patch(user, updates)).setRoles(null).setPassword(null));
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
