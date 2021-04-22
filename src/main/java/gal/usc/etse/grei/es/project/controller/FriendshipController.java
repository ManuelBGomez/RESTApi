package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.errorManagement.ErrorObject;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.FriendshipService;
import gal.usc.etse.grei.es.project.utilities.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "Friendships API", description = "User friendships related operations")
@SecurityRequirement(name = "JWT")
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
    @Autowired
    public FriendshipController(FriendshipService friends, LinkRelationProvider relationProvider) {
        this.friends = friends;
        this.relationProvider = relationProvider;
    }

    /**
     * Método: POST
     * Url para llegar: /friendships
     * Objetivo: añadir un amigo al usuario con los datos especificados.
     * Permisos: única y exclusivamente el propio usuario - el que manda la solicitud! -.
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
    @Operation(
            operationId = "createFriendship",
            summary = "Create a new friendship",
            description = "Create a new friendship between two users. Only the user that makes the request is allowed " +
                    "to create it, and two users can only have one friendship relation between them."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Friendship created correctly",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format in some parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One of the users not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The requested users already are friends",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Friendship> addFriend(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Friendship data for creation",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"user\": \"test@test.com\"," +
                                            "\"friend\": \"test2@test.com\"}"
                            )
                    )
            )
            @Valid @RequestBody Friendship friendship
    ){
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
    @Operation(
            operationId = "getFriendship",
            summary = "Get friendship by id",
            description = "Get friendship information using its id. To get it, you must be one of the members of " +
                    "the requested friendship."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Friendship details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Friendship> getFriendship(
            @Parameter(name = "id", description = "Friendship id", example = "6075b1f9866a2401c582f898")
            @PathVariable("id") String id
    ) {
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
    @Operation(
            operationId = "deleteFriendship",
            summary = "Delete friendship by id",
            description = "Delete friendship information using its id. To do this, you must be one of the members of " +
                    "the requested friendship."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Friendship deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Object> deleteFriend(
            @Parameter(name = "id", description = "Friendship id", example = "6075b1f9866a2401c582f898")
            @PathVariable("id") String id
    ){
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
    @Operation(
            operationId = "updateFriendship",
            summary = "Update friendship to confirm it",
            description = "Update friendship information with the confirmation of this friendship by the user that received it " +
                    "(the one who didn't created it), that will be the only allowed user to do this."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified friendship correctly (confirmed)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format in some parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Friendship is already confirmed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "419",
                    description = "Token Expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Invalid format of JSON patch",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    ResponseEntity<Friendship> updateFriendship(
            @Parameter(name = "id", description = "Friendship id", example = "6075b1f9866a2401c582f898")
            @PathVariable("id") String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Modifications to movie list",
                    content = @Content(
                            mediaType = "application/json-patch+json",
                            examples = @ExampleObject(
                                    value = "[{\"op\": \"replace\", \"path\": \"/confirmed\", \"value\": true}]"
                            )
                    )
            )
            @RequestBody List<Map<String, Object>> updates
    ){
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
