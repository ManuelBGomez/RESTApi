package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.errorManagement.exceptions.*;
import gal.usc.etse.grei.es.project.model.validation.createValidation;
import gal.usc.etse.grei.es.project.model.validation.friendValidation;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.utilities.AuxMethods;
import gal.usc.etse.grei.es.project.utilities.Constants;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Clase UserController -> Url para llegar: /users
 * Gestión de peticiones relacionadas con los usuarios de la red social
 */
@RestController
@RequestMapping("users")
public class UserController {
    private final UserService users;
    private final AssessmentService assessments;

    /**
     * Constructor de la clase
     *
     * @param users Instancia de la clase UserService
     */
    @Autowired
    public UserController(UserService users, AssessmentService assessments){
        this.users = users;
        this.assessments = assessments;
    }


    /**
     * Método: GET
     * Url para llegar: /users/{id}
     * Objetivo: recuperar los datos del usuario cuyo id es facilitado a través de la URL.
     *
     * @param id El identificador del usuario para recuperar la información.
     * @return Si el id es válido, los datos del usuario cuyo id ha sido facilitado como parámetro.
     */
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> get(@PathVariable("id") String id) {
        return ResponseEntity.of(users.get(id));
    }

    /**
     * Método: GET
     * Url para llegar: /users
     * Objetivo: recuperar los datos de todos los usuarios, filtrados por nombre y email.
     *
     * @param page La página a recuperar
     * @param size Tamaño de la página.
     * @param sort Parámetros de ordenación.
     * @param name Nombre por el cual hacer la busqueda
     * @param email Email por el cual hacer la busqueda
     * @return Los datos de todos los usuarios que coinciden con los filtros introducidos.
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    ) ResponseEntity<Page<User>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email
    ) {
        //Recuperamos los criterios de ordenación:
        List<Sort.Order> criteria = AuxMethods.getSortCriteria(sort);
        //Devolvemos la ResponseEntity adecuada (ok si hay resultados, not found si no los hay):
        return ResponseEntity.of(users.get(page, size, Sort.by(criteria), name, email));
    }


    /**
     * Método: POST
     * Url para llegar: /users
     * Objetivo: crear un nuevo usuario con los datos facilitados.
     *
     * @param user Los datos del nuevo usuario que se quiere insertar
     * @return Los datos del usuario insertado y la url para recuperar su información, o un estado erróneo
     * si es preciso.
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> create(@Validated(createValidation.class) @RequestBody User user){
        //Se intenta crear el usuario:
        Optional<User> inserted = users.create(user);
        //Se devuelve un estado creado, con la URI con la que se puede acceder a él:
        return ResponseEntity.created(URI.create(Constants.URL + "/users/" + inserted.get().getEmail()))
                .body(inserted.get());
    }

    /**
     * Método: DELETE
     * Url para llegar: /users/{id}
     * Objetivo: eliminar el usuario con el id facilitado
     *
     * @param id el identificador (email) del usuario que se quiere borrar
     * @return estado correcto en caso de encontrar al usuario y borrarlo correctamente y estado Not Found
     *      en caso de no encontrarle.
     */
    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> delete(
                @PathVariable("id") String id
            ) {
        //Se intenta borrar el usuario:
        users.delete(id);
        //Si se consigue, se devuelve un estado noContent (no hay nada que devolver):
        return ResponseEntity.noContent().build();
    }

    /**
     * Método: PATCH
     * Url para llegar: /users/{id}
     * Objetivo: actualizar los datos del usuario con el id facilitado. No se podrá actualizar ni el mail ni la fecha de
     *          nacimiento.
     *
     * @param id El id del usuario cuyos demás datos se quieren actualizar.
     * @param updates Las actualizaciones que se deben realizar.
     * @return estado correcto en caso de encontrar al usuario y haberlo actualizado. Si no, se devolverá algún estado
     *          de error.
     */
    @PatchMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> update(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> updates){
        //Intentamos hacer la actualización:
        Optional<User> result = users.update(id, updates);
        //Se devuelve un estado ok si se ha ejecutado correctamente:
        return ResponseEntity.ok().body(result.get());
    }

    /**
     * Método: POST
     * Url para llegar: /users/{id}/friends
     * Objetivo: añadir un amigo al usuario con los datos especificados.
     *
     * @param id El id del usuario al cual se le quiere añadir un amigo.
     * @param newFriend Los datos del nuevo usuario.
     * @return estado correcto en caso de encontrar al usuario y al amigo, y haber añadido dicho amigo, si no, estado
     *          de error.
     */
    @PostMapping(
            path = "{id}/friends",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> addFriend(@PathVariable("id") String id,
                                     @Validated(friendValidation.class) @RequestBody User newFriend){
            //Llamamos al método de la clase de usuarios:
            Optional<User> result = users.addFriend(id, newFriend);
            //Devovlemos respuesta ok con los datos si ha ido bien la ejecución:
            return ResponseEntity.ok(result.get());
    }

    /**
     * Método: DELETE
     * Url para llegar: /users/{id}/friends/{idFriend}
     * Objetivo: borrar el amigo que se facilita por url, del usuario cuyo id también se facilita por url.
     *
     * @param id El identificador del usuario del cual se quiere eliminar un amigo.
     * @param idFriend El identificador del amigo que se quiere eliminar.
     * @return Los datos del usuario tras la eliminación si se pudo hacer, o un estado de error.
     */
    @DeleteMapping(
            path = "{id}/friends/{idFriend}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Object> deleteFriend(@PathVariable("id") String id,
                                      @PathVariable("idFriend") String idFriend){
        //Se intenta hacer el borrado:
        Optional<User> result = users.deleteFriend(id, idFriend);
        return ResponseEntity.ok(result.get());
    }

    /**
     * Método: GET
     * Url para llegar: /users/{id}/comments
     * Objetivo: recuperar los comentarios de un usuario.
     *
     * @param page Página a recuperar
     * @param size Tamaño de la página
     * @param sort Criterios de ordenación
     * @param userId Identificador del usuario para el que se devolverán los comentarios
     * @return los comentarios asociados al usuario correspondiente, o un estado not found si no los hay.
     */
    @GetMapping(
            path = "{id}/comments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Assessment>> getUserComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("id") String userId
    ) {
        //Se recuperan los criterios de ordenación:
        List<Sort.Order> criteria = AuxMethods.getSortCriteria(sort);

        //Se hace la búsqueda y se devuelve el estado apropiado (por eso se usa of):
        return ResponseEntity.of(assessments.getUserComments(page, size, Sort.by(criteria), userId));
    }

}
