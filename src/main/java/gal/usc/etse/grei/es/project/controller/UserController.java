package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Clase UserController -> Url para llegar: /users
 * Gestión de peticiones relacionadas con los usuarios de la red social
 */
@RestController
@RequestMapping("users")
public class UserController {
    private final UserService users;

    /**
     * Constructor de la clase
     *
     * @param users Instancia de la clase UserService
     */
    @Autowired
    public UserController(UserService users){
        this.users = users;
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
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if(string.startsWith("+")){
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.of(users.get(page, size, Sort.by(criteria), name, email));
    }


    /**
     * Método: POST
     * Url para llegar: /users
     * Objetivo: crear un nuevo usuario con los datos facilitados.
     *
     * @param user Los datos del nuevo usuario que se quiere insertar
     * @return Los datos del usuario insertado y la url para recuperar su información
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> create(@Valid @RequestBody User user){
        Optional<User> inserted = users.create(user);

        if(!inserted.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            return ResponseEntity.created(URI.create("http://localhost:8080/movies/" + inserted.get().getEmail()))
                    .body(inserted.get());
        }
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
    ResponseEntity delete(
                @PathVariable("id") String id
            ) {
        if(users.delete(id)){
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Método: PUT
     * Url para llegar: /users/{id}
     * Objetivo: actualizar los datos del usuario con el id facilitado. No se podrá actualizar ni el mail ni la fecha de
     *          nacimiento.
     *
     * @param id El id del usuario cuyos demás datos se quieren actualizar.
     * @param user Los datos del usuario a modificar.
     * @return estado correcto en caso de encontrar al usuario y haberlo actualizado. Si no, se devolverá algún estado
     *          de error.
     */
    @PutMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> update(@PathVariable("id") String id, @Valid @RequestBody User user){
        Optional<User> result = users.update(id, user);

        if(result.isPresent()){
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.notFound().build();
        }
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
    ResponseEntity<User> addFriend(@PathVariable("id") String id, @RequestBody User newFriend){

        Optional<User> result = users.addFriend(id, newFriend);

        if(result.isPresent()){
            return ResponseEntity.ok(result.get());
        } else return ResponseEntity.notFound().build();
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
    ResponseEntity<User> deleteFriend(@PathVariable("id") String id,
                                      @PathVariable("idFriend") String idFriend){

        Optional<User> result = users.deleteFriend(id, idFriend);

        if(result.isPresent()){
            return ResponseEntity.ok(result.get());
        } else return ResponseEntity.notFound().build();
    }

}
