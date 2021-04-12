package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.FriendshipService;
import gal.usc.etse.grei.es.project.utilities.Constants;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Clase FriendshipController -> Url para llegar: /friendships.
 * Gestión de las peticiones relativas a las amistades de usuarios.
 *
 * @author Manuel Bendaña
 */
@RestController
@RequestMapping("friendships")
public class FriendshipController {
    private final FriendshipService friends;
    //Referencia a un linkrelationprovider para los hateoas
    private final LinkRelationProvider relationProvider;

    /**
     * Constructor de la clase
     *
     * @param friends Instrancia de la clase FriendService
     * @param relationProvider Instancia de la clase LinkRelationProvider
     */
    public FriendshipController(FriendshipService friends, LinkRelationProvider relationProvider) {
        this.friends = friends;
        this.relationProvider = relationProvider;
    }

    /**
     * Método: POST
     * Url para llegar: /friendships
     * Objetivo: añadir un amigo al usuario con los datos especificados.
     * Permisos: única y exclusivamente el propio usuario.
     * Enlaces devueltos: a la amistad creada y a la lista de amistades del usuario.
     *
     * @param friendship Datos de la amistad.
     * @return estado correcto en caso de encontrar al usuario y al amigo, y haber añadido dicho amigo, si no, estado
     *          de error.
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#friendship.user==principal")
    ResponseEntity<Friendship> addFriend(@Valid @RequestBody Friendship friendship){
        //Tratamos de devolver el estado adecuado si se crea la amistad:
        Friendship inserted = friends.addFriend(friendship);
        //Si el método termina correctamente, se preparan los enlaces y se devuelve un estado ok:
        //Enlace a la propia amistad:
        Link self = linkTo(methodOn(FriendshipController.class).getFriendship(friendship.getId())).withSelfRel();
        //Enlace a todas las amistades de ese usuario:
        Link all = linkTo(methodOn(UserController.class).getUserFriendships(0, 20, null, friendship.getUser()))
                .withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
        //Se devuelven los datos adecuados:
        return ResponseEntity.created(URI.create(Constants.URL + "/friendships/"
                + inserted.getId()))
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(inserted);
    }

    /**
     * Método: GET.
     * Url para llegar: /friendships/{id}
     * Objetivo: recuperar los datos de una amistad.
     * Permisos: únicamente los implicados en la relación de amistad.
     * Pongo al primer usuario dado que el segundo es el amigo que corresponde.
     * Enlaces devueltos: a la amistad, la lista de todos los amigos del usuario, al perfil del
     *      usuario y al perfil del amigo.
     *
     * @param id El id de la amistad.
     * @return Los datos de la amistad.
     */
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@friendshipService.isInFriendship(principal, #id)")
    ResponseEntity<Friendship> getFriendship(@PathVariable("id") String id) {
        //Ejecutamos el método:
        Optional<Friendship> friendship = friends.getFriendship(id);
        if(friendship.isPresent()){
            Friendship res = friendship.get();
            //Preparamos los enlaces (si hay errores, ya saltan excepciones que se manejan por otra vía):
            //Enlace a la propia amistad:
            Link self = linkTo(methodOn(FriendshipController.class).getFriendship(res.getId()))
                    .withSelfRel();
            //Enlace a todas las amistades de este usuario:
            Link all = linkTo(methodOn(UserController.class).getUserFriendships(0, 20, null,
                    SecurityContextHolder.getContext().getAuthentication().getName()))
                    .withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
            //Enlace al usuario:
            Link user = linkTo(methodOn(UserController.class).get(res.getUser()))
                    .withRel(relationProvider.getItemResourceRelFor(User.class));
            //Enlace al amigo:
            Link friend = linkTo(methodOn(UserController.class).get(res.getFriend()))
                    .withRel(relationProvider.getItemResourceRelFor(User.class));
            //Llamamos al método que corresponde para recuperar la información de la amistad.
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .header(HttpHeaders.LINK, user.toString())
                    .header(HttpHeaders.LINK, friend.toString())
                    .body(res);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Método: DELETE
     * Url para llegar: /friendships/{id}
     * Objetivo: borrar el amigo que se facilita por url, del usuario cuyo id también se facilita por url.
     * Permisos: única y exclusivamente el propio usuario.
     * Enlaces devueltos: a la lista de todos los amigos del usuario que hace el borrado.
     *
     * @param id El identificador del usuario del cual se quiere eliminar un amigo.
     * @return Un estado noContent si se pudo hacer la eliminación, el error adecuado en caso contrario.
     */
    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@friendshipService.isInFriendship(principal, #id)")
    ResponseEntity<Object> deleteFriend(@PathVariable("id") String id){
        //Se intenta hacer el borrado:
        friends.deleteFriend(id);
        //Si termina el método, es que se ha borrado correctamente. Se prepara el enlace a la lista de todos los amigos
        //del usuario.
        Link all = linkTo(methodOn(UserController.class).getUserFriendships(0, 20, null,
                SecurityContextHolder.getContext().getAuthentication().getName()))
                .withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
        //Si el método finaliza correctamente, se devuelve un noContent:
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }

    /**
     * Método: PATCH
     * Url para llegar: /friendships/{id}
     * Objetivo: modificar la relación de amistad, de manera que se confirme la relación entre dos amigos.
     * Permisos: únicamente el amigo que quiere confirmar.
     * Enlaces devueltos: a la propia amistad, a todas las amistades del usuario, al amigo y al propio usuario.
     *
     * @param id El id de la amistad.
     * @return El usuario actualizado sobre la base de datos y un estado correcto si salió bien, si no, estado de error.
     */
    @PatchMapping(
            path = "{id}",
            consumes = "application/json-patch+json",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@friendshipService.hasToConfirm(#id, principal)")
    ResponseEntity<Friendship> updateFriendship(@PathVariable("id") String id,
                                                @RequestBody List<Map<String, Object>> updates){
        //Se intenta hacer la actualización:
        Friendship friendship = friends.updateFriendship(id, updates);
        //Si finaliza correctamente el método se sigue adelante creando los enlaces necesarios.
        //Enlace a la propia amistad:
        Link self = linkTo(methodOn(FriendshipController.class).getFriendship(id))
                .withSelfRel();
        //Enlace a todas las amistades de ese usuario (EL QUE CONFIRMA):
        Link all = linkTo(methodOn(UserController.class).getUserFriendships(0, 20, null,
                SecurityContextHolder.getContext().getAuthentication().getName())).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
        //Enlace al usuario:
        Link user = linkTo(methodOn(UserController.class).get(friendship.getUser()))
                .withRel(relationProvider.getItemResourceRelFor(User.class));
        //Enlace al amigo:
        Link friend = linkTo(methodOn(UserController.class).get(friendship.getFriend()))
                .withRel(relationProvider.getItemResourceRelFor(User.class));
        //Se devuelve estado ok con todos los enlaces y datos de la amistad:
        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .header(HttpHeaders.LINK, user.toString())
                .header(HttpHeaders.LINK, friend.toString())
                .body(friendship);
    }


}
