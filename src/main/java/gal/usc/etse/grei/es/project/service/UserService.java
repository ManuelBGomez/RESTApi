package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.CommentRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;
    private final CommentRepository comments;

    @Autowired
    public UserService(UserRepository users, CommentRepository comments){
        this.users = users;
        this.comments = comments;
    }

    public Optional<User> get(String id){
        return users.findById(id);
    }

    public Optional<Page<User>> get(int page, int size, Sort sort, String name, String email){
        Pageable request = PageRequest.of(page, size, sort);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        //Establecemos criterios de filtrado:
        Example<User> filter = Example.of(new User().setEmail(email).setName(name), matcher);

        Page<User> result  = users.findAll(filter, request);


        if(result.isEmpty())
            return Optional.empty();

        result.forEach((it) -> {
            it.setEmail(null);
            it.setFriends(null);
        });

        return Optional.of(result);
    }

    public Optional<User> create(User user){
        if(users.existsById(user.getEmail())) {
            return Optional.empty();
        } else {
            return Optional.of(users.insert(user));
        }
    }

    public boolean delete(String userMail){
        if(users.existsById(userMail)){
            users.deleteById(userMail);
            return true;
        } else {
            return false;
        }
    }

    public Optional<User> update(String id, User user){
        //Comprobamos que el id del usuario existe en la bd:
        if(users.existsById(id)){
            //Existe - Comprobamos que el campo birthday no se haya cambiado:
            User oldUser = users.findById(id).get();
            //Comprobamos si las fechas son coincidentes:
            if(user.getBirthday().equals(oldUser.getBirthday())){
                user.setEmail(id);
                return Optional.of(users.save(user));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
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
