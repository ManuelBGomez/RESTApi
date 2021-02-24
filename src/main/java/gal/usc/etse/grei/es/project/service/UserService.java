package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;

    @Autowired
    public UserService(UserRepository users){
        this.users = users;
    }

    public Optional<User> get(String id){
        return users.findById(id);
    }

    public Optional<Page<User>> get(int page, int size, Sort sort, String name, String email){
        Pageable request = PageRequest.of(page, size, sort);

        Page<User> result = users.findAllByNameAndId(name, email, request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
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

}
