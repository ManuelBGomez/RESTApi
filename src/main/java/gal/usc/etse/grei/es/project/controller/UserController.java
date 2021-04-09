package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.validation.createValidation;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.FriendService;
import gal.usc.etse.grei.es.project.utilities.AuxMethods;
import gal.usc.etse.grei.es.project.utilities.Constants;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Clase UserController -> Url para llegar: /users
 * Gestión de peticiones relacionadas con los usuarios de la red social
 */
@RestController
@RequestMapping("users")
public class UserController {
    private final UserService users;
    private final AssessmentService assessments;
    private final FriendService friends;
    //Referencia a un linkrelationprovider para los hateoas
    private final LinkRelationProvider relationProvider;

    /**
     * Constructor de la clase
     *
     * @param users Instancia de la clase UserService
     * @param assessments Instancia de la clase AssessmentService
     * @param friends Instancia de la clase FriendService
     * @param relationProvider Instancia de la clase LinkRelationProvider
     */
    @Autowired
    public UserController(UserService users, AssessmentService assessments,
                          FriendService friends, LinkRelationProvider relationProvider){
        this.users = users;
        this.assessments = assessments;
        this.friends = friends;
        this.relationProvider = relationProvider;
    }

    /**
     * Método: GET
     * Url para llegar: /users/{id}
     * Objetivo: recuperar los datos del usuario cuyo id es facilitado a través de la URL.
     * Permisos: administrador, el propio usuario o un amigo del usuario.
     * Enlaces devueltos: a sí mismo y al listado de todos los usuarios.
     *
     * @param id El identificador del usuario para recuperar la información.
     * @return Si el id es válido, los datos del usuario cuyo id ha sido facilitado como parámetro.
     */
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN') or #id == principal or @friendService.areFriends(#id, principal)")
    ResponseEntity<User> get(@PathVariable("id") String id) {
        //Hacemos la consulta:
        Optional<User> result = users.get(id);

        //Si hay resultado, preparamos los enlaces y devolvemos un resultado correcto:
        if(result.isPresent()){
            //Enlace a sí mismo:
            Link self = linkTo(methodOn(UserController.class).get(id)).withSelfRel();
            //Enlace a todos los usuarios:
            Link all = linkTo(methodOn(UserController.class).get(0, 20, null, null, null))
                    .withRel(relationProvider.getCollectionResourceRelFor(User.class));

            //Devolvemos la resupuesta con el resultado y los enlaces en la cabecera:
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(result.get());
        }

        //Si no ha habido resultado, devolvemos un estado incorrecto (not found):
        return ResponseEntity.notFound().build();
    }

    /**
     * Método: GET
     * Url para llegar: /users
     * Objetivo: recuperar los datos de todos los usuarios, filtrados por nombre y email.
     * Permisos: sólo usuarios logueados.
     * Enlaces devueltos: a sí mismo, a las páginas primera, siguiente, anterior y última, y a un
     *      recurso usuario concreto.
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
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<User>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email
    ) {
        //Recuperamos los criterios de ordenación:
        List<Sort.Order> criteria = AuxMethods.getSortCriteria(sort);

        //Recuperamos el resultado:
        Optional<Page<User>> result = users.get(page, size, Sort.by(criteria), name, email);

        //Si se encuentra resultado, se preparan enlaces y se devuelve un estado correcto:
        if(result.isPresent()){
            //Recuperamos el resultado y los datos de pageable:
            Page<User> data = result.get();
            Pageable metadata = data.getPageable();

            //Enlace a si mismo:
            Link self = linkTo(methodOn(UserController.class).get(page, size, sort, name, email))
                    .withSelfRel();

            //Enlace al primero:
            Link first = linkTo(methodOn(UserController.class)
                    .get(metadata.first().getPageNumber(), size, sort, name, email)
            ).withRel(IanaLinkRelations.FIRST);

            //Enlace al último (recuperamos el total de páginas y restamos 1):
            Link last = linkTo(methodOn(UserController.class)
                    .get(data.getTotalPages() - 1, size, sort, name, email)
            ).withRel(IanaLinkRelations.LAST);

            //Enlace al siguiente
            Link next = linkTo(methodOn(UserController.class)
                    .get(metadata.next().getPageNumber(), size, sort, name, email)
            ).withRel(IanaLinkRelations.NEXT);

            //Enlace al anterior (si no lo hay, al primer elemento):
            Link previous = linkTo(methodOn(UserController.class)
                    .get(metadata.previousOrFirst().getPageNumber(), size, sort, name, email)
            ).withRel(IanaLinkRelations.PREVIOUS);

            //Enlace a un recurso usuario solo:
            Link one = linkTo(methodOn(UserController.class).get(null))
                    .withRel(relationProvider.getItemResourceRelFor(User.class));

            //Devolvemos el resultado:
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .header(HttpHeaders.LINK, one.toString())
                    .body(data);
        }

        //Devolvemos la ResponseEntity not found si no se encontró nada (y no se entró al if previo):
        return ResponseEntity.notFound().build();
    }

    /**
     * Método: POST
     * Url para llegar: /users
     * Objetivo: crear un nuevo usuario con los datos facilitados.
     * Permisos: cualquiera.
     * Enlaces devueltos: al usuario creado y a la lista de todos los usuarios.
     *
     * @param user Los datos del nuevo usuario que se quiere insertar
     * @return Los datos del usuario insertado y la url para recuperar su información, o un estado erróneo
     * si es preciso.
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> create(@Validated(createValidation.class) @RequestBody User user){
        //Se intenta crear el usuario:
        Optional<User> inserted = users.create(user);
        //Si se llega aquí es que se ha finalizado el método. Devolveremos referencia al propio
        //usuario y a la lista de todos.
        //Enlace a sí mismo (usamos el email del usuario):
        Link self = linkTo(methodOn(UserController.class).get(inserted.get().getEmail())).withSelfRel();
        //Enlace a todos los usuarios:
        Link all = linkTo(methodOn(UserController.class).get(0, 20, null, null, null))
                .withRel(relationProvider.getCollectionResourceRelFor(User.class));

        //Se devuelve un estado creado, con la URI con la que se puede acceder a él:
        return ResponseEntity.created(URI.create(Constants.URL + "/users/" + inserted.get().getEmail()))
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(inserted.get());
    }

    /**
     * Método: DELETE
     * Url para llegar: /users/{id}
     * Objetivo: eliminar el usuario con el id facilitado
     * Permisos: única y exclusivamente el propio usuario.
     * Enlaces devueltos: a la lista de todos los usuarios.
     *
     * @param id el identificador (email) del usuario que se quiere borrar
     * @return estado correcto en caso de encontrar al usuario y borrarlo correctamente y estado Not Found
     *      en caso de no encontrarle.
     */
    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#id == principal")
    ResponseEntity<Object> delete(
                @PathVariable("id") String id
            ) {
        //Se intenta borrar el usuario:
        users.delete(id);
        //Si el método termina correctamente, preparamos el enlace a la lista de todos los usuarios
        //y lo devolvemos:
        Link all = linkTo(methodOn(UserController.class).get(0, 20, null, null, null))
                .withRel(relationProvider.getCollectionResourceRelFor(User.class));
        //Se devuelve un estado noContent (no hay nada que devolver):
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }

    /**
     * Método: PATCH
     * Url para llegar: /users/{id}
     * Objetivo: actualizar los datos del usuario con el id facilitado. No se podrá actualizar ni el mail ni la fecha de
     *          nacimiento.
     * Permisos: única y exclusivamente el propio usuario.
     * Enlaces devueltos: al propio usuario y a la lista de todos.
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
    @PreAuthorize("#id==principal")
    ResponseEntity<User> update(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> updates){
        //Intentamos hacer la actualización:
        Optional<User> result = users.update(id, updates);
        //Si el método termina correctamente, se preparan los enlaces y se devuelve un estado ok:
        //Enlace al propio usuario:
        Link self = linkTo(methodOn(UserController.class).get(result.get().getEmail())).withSelfRel();
        //Enlace a todos los usuarios:
        Link all = linkTo(methodOn(UserController.class).get(0, 20, null, null, null))
                .withRel(relationProvider.getCollectionResourceRelFor(User.class));
        //Se devuelve el estado addecuado:
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(result.get());
    }

    /**
     * Método: POST
     * Url para llegar: /users/{id}/friends
     * Objetivo: añadir un amigo al usuario con los datos especificados.
     * Permisos: única y exclusivamente el propio usuario.
     * Enlaces devueltos: a la amistad creada y a la lista de amistades del usuario.
     *
     * @param id El id del usuario al cual se le quiere añadir un amigo.
     * @param friendship Datos de la amistad.
     * @return estado correcto en caso de encontrar al usuario y al amigo, y haber añadido dicho amigo, si no, estado
     *          de error.
     */
    @PostMapping(
            path = "{id}/friends",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#id==principal")
    ResponseEntity<Friendship> addFriend(@PathVariable("id") String id,
                                   @Valid @RequestBody Friendship friendship){
        //Tratamos de devolver el estado adecuado si se crea la amistad:
        Friendship inserted = friends.addFriend(id, friendship);
        //Si el método termina correctamente, se preparan los enlaces y se devuelve un estado ok:
        //Enlace a la propia amistad:
        Link self = linkTo(methodOn(UserController.class).getFriendship(id, friendship.getFriend())).withSelfRel();
        //Enlace a todas las amistades de ese usuario:
        Link all = linkTo(methodOn(UserController.class).getUserFriendships(0, 20, null, id))
                .withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
        //Se devuelven los datos adecuados:
        return ResponseEntity.created(URI.create(Constants.URL + "/users/" + inserted.getUser() + "/friends/"
                + inserted.getFriend()))
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(inserted);
    }

    /**
     * Método: GET.
     * Url para llegar: /users/{id}/friends/{friendId}
     * Objetivo: recuperar los datos de una amistad.
     * Permisos: únicamente los implicados en la relación de amistad.
     * Pongo al primer usuario dado que el segundo es el amigo que corresponde.
     * Enlaces devueltos: a la amistad, la lista de todos los amigos del usuario, al perfil del
     *      usuario y al perfil del amigo.
     *
     * @param id El id de uno de los usuarios.
     * @param friendId El id del amigo.
     * @return Los datos de la amistad.
     */
    @GetMapping(
            path = "{id}/friends/{friendId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#id==principal")
    ResponseEntity<Friendship> getFriendship(@PathVariable("id") String id,
                                             @PathVariable("friendId") String friendId) {
        //Ejecutamos el método:
        Friendship friendship = friends.getFriendship(id,friendId);
        //Preparamos los enlaces (si hay errores, ya saltan excepciones que se manejan por otra vía):
        //Enlace a la propia amistad:
        Link self = linkTo(methodOn(UserController.class).getFriendship(friendship.getUser(), friendship.getFriend()))
                .withSelfRel();
        //Enlace a todas las amistades de ese usuario:
        Link all = linkTo(methodOn(UserController.class).getUserFriendships(0, 20, null,
                friendship.getUser())).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
        //Enlace al usuario:
        Link user = linkTo(methodOn(UserController.class).get(friendship.getUser()))
                .withRel(relationProvider.getItemResourceRelFor(User.class));
        //Enlace al amigo:
        Link friend = linkTo(methodOn(UserController.class).get(friendship.getFriend()))
                .withRel(relationProvider.getItemResourceRelFor(User.class));
        //Llamamos al método que corresponde para recuperar la información de la amistad.
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .header(HttpHeaders.LINK, user.toString())
                .header(HttpHeaders.LINK, friend.toString())
                .body(friendship);
    }

    /**
     * Método: GET.
     * Url para llegar: /users/{id}/friends
     * Objetivo: recuperar todos los amigos de un usuario determinado.
     * Permisos: única y exclusivamente el propio usuario.
     * Enlaces devueltos: a sí mismo y a las páginas primera, siguiente, anterior y última.
     *
     * @param page La página a recuperar.
     * @param size Tamaño de la página a recuperar.
     * @param sort Criterios de ordenación.
     * @param id Identificador del usuario para el cual se recuperarán sus amigos.
     * @return La página que corresponda con los datos de los amigos.
     */
    @GetMapping(
            path = "{id}/friends",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#id==principal")
    ResponseEntity<Page<Friendship>> getUserFriendships(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("id") String id
    ) {
        //Recuperamos criterios de ordenación:
        List<Sort.Order> criteria = AuxMethods.getSortCriteria(sort);
        //Intentamos hacer la búsqueda:
        Optional<Page<Friendship>> result = friends.getUserFriendships(page,size,Sort.by(criteria),id);

        //Si hay resultado, devolveremos estado correcto con todos los enlaces pedidos:
        if(result.isPresent()){
            //Recuperamos el resultado y los datos de pageable:
            Page<Friendship> data = result.get();
            Pageable metadata = data.getPageable();

            //Enlace a si mismo:
            Link self = linkTo(methodOn(UserController.class).getUserFriendships(page, size, sort, id))
                    .withSelfRel();

            //Enlace al primero:
            Link first = linkTo(methodOn(UserController.class)
                    .getUserFriendships(metadata.first().getPageNumber(), size, sort, id)
            ).withRel(IanaLinkRelations.FIRST);

            //Enlace al último (recuperamos el total de páginas y restamos 1):
            Link last = linkTo(methodOn(UserController.class)
                    .getUserFriendships(data.getTotalPages() - 1, size, sort, id)
            ).withRel(IanaLinkRelations.LAST);

            //Enlace al siguiente
            Link next = linkTo(methodOn(UserController.class)
                    .getUserFriendships(metadata.next().getPageNumber(), size, sort, id)
            ).withRel(IanaLinkRelations.NEXT);

            //Enlace al anterior (si no lo hay, al primer elemento):
            Link previous = linkTo(methodOn(UserController.class)
                    .getUserFriendships(metadata.previousOrFirst().getPageNumber(), size, sort, id)
            ).withRel(IanaLinkRelations.PREVIOUS);

            //Devolvemos el resultado:
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .body(data);
        }

        //No hay resultado: se devuelve un estado not found:
        return ResponseEntity.notFound().build();
    }

    /**
     * Método: DELETE
     * Url para llegar: /users/{id}/friends/{idFriend}
     * Objetivo: borrar el amigo que se facilita por url, del usuario cuyo id también se facilita por url.
     * Permisos: única y exclusivamente el propio usuario.
     * Enlaces devueltos: a la lista de todos los amigos del usuario que hace el borrado.
     *
     * @param id El identificador del usuario del cual se quiere eliminar un amigo.
     * @param idFriend El identificador del amigo que se quiere eliminar.
     * @return Un estado noContent si se pudo hacer la eliminación, el error adecuado en caso contrario.
     */
    @DeleteMapping(
            path = "{id}/friends/{idFriend}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#id==principal")
    ResponseEntity<Object> deleteFriend(@PathVariable("id") String id,
                                      @PathVariable("idFriend") String idFriend){
        //Se intenta hacer el borrado:
        friends.deleteFriend(id, idFriend);
        //Si termina el método, es que se ha borrado correctamente. Se prepara el enlace a la lista de todos los amigos
        //del usuario.
        Link all = linkTo(methodOn(UserController.class).getUserFriendships(0, 20, null, id))
                .withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
        //Si el método finaliza correctamente, se devuelve un noContent:
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }

    /**
     * Método: PATCH
     * Url para llegar: /users/{id}/friends/{friendId}
     * Objetivo: modificar la relación de amistad, de manera que se confirme la relación entre dos amigos.
     * Permisos: únicamente el amigo que quiere confirmar.
     * Enlaces devueltos: a la propia amistad, a todas las amistades del usuario, al amigo y al propio usuario.
     *
     * @param id El id del usuario al que alguien ha añadido como amigo.
     * @param friendId El id del amigo que añadió al usuario.
     * @param updates Las actualizaciones a realizar
     * @return El usuario actualizado sobre la base de datos y un estado correcto si salió bien, si no, estado de error.
     */
    @PatchMapping(
            path = "{id}/friends/{friendId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#id==principal and @friendService.hasToConfirm(#friendId, principal)")
    ResponseEntity<Friendship> updateFriendship(@PathVariable("id") String id, @PathVariable("friendId") String friendId,
                                                @RequestBody List<Map<String, Object>> updates){
        //Se intenta hacer la actualización:
        Friendship friendship = friends.updateFriendship(id, friendId, updates);
        //Si finaliza correctamente el método se sigue adelante creando los enlaces necesarios.
        //Enlace a la propia amistad:
        Link self = linkTo(methodOn(UserController.class).getFriendship(id, friendId))
                .withSelfRel();
        //Enlace a todas las amistades de ese usuario (EL QUE CONFIRMA):
        Link all = linkTo(methodOn(UserController.class).getUserFriendships(0, 20, null,
                id)).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
        //Enlace al usuario:
        Link user = linkTo(methodOn(UserController.class).get(id))
                .withRel(relationProvider.getItemResourceRelFor(User.class));
        //Enlace al amigo:
        Link friend = linkTo(methodOn(UserController.class).get(friendId))
                .withRel(relationProvider.getItemResourceRelFor(User.class));
        //Se devuelve estado ok con todos los enlaces y datos de la amistad:
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .header(HttpHeaders.LINK, user.toString())
                .header(HttpHeaders.LINK, friend.toString())
                .body(friendship);
    }


    /**
     * Método: GET
     * Url para llegar: /users/{id}/comments
     * Objetivo: recuperar los comentarios de un usuario.
     * Permisos: administrador, el propio usuario o un amigo del usuario.
     * Enlaces devueltos: al usuario, a la primera página, a la siguiente, a la anterior y a la última.
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
    @PreAuthorize("hasRole('ADMIN') or #userId ==principal or @friendService.areFriends(#userId, principal)")
    ResponseEntity<Page<Assessment>> getUserComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("id") String userId
    ) {
        //Se recuperan los criterios de ordenación:
        List<Sort.Order> criteria = AuxMethods.getSortCriteria(sort);

        //Se trata de hacer la búsqueda:
        Optional<Page<Assessment>> result = assessments.getUserComments(page, size, Sort.by(criteria), userId);

        if(result.isPresent()){
            Page<Assessment> data = result.get();
            Pageable metadata = data.getPageable();
            //Preparamos enlaces para devolver
            //Al usuario:
            Link user = linkTo(methodOn(UserController.class).get(userId))
                    .withRel(relationProvider.getItemResourceRelFor(User.class));
            //Enlace al primero:
            Link first = linkTo(methodOn(UserController.class)
                    .getUserComments(metadata.first().getPageNumber(), size, sort, userId)
            ).withRel(IanaLinkRelations.FIRST);
            //Enlace al último (recuperamos el total de páginas y restamos 1):
            Link last = linkTo(methodOn(UserController.class)
                    .getUserComments(data.getTotalPages() - 1, size, sort, userId)
            ).withRel(IanaLinkRelations.LAST);
            //Enlace al siguiente
            Link next = linkTo(methodOn(UserController.class)
                    .getUserComments(metadata.next().getPageNumber(), size, sort, userId)
            ).withRel(IanaLinkRelations.NEXT);
            //Enlace al anterior (si no lo hay, al primer elemento):
            Link previous = linkTo(methodOn(UserController.class)
                    .getUserComments(metadata.previousOrFirst().getPageNumber(), size, sort, userId)
            ).withRel(IanaLinkRelations.PREVIOUS);

            //Se devuelve la respuesta:
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, user.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .body(data);
        }

        //Si no se encuentra resultado, se devuelve un not found:
        return ResponseEntity.notFound().build();
    }
}
